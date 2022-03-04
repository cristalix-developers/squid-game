package me.func.day.play.tug

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.Arcade
import me.func.accept.AcceptLose
import me.func.SquidGame
import me.func.app
import me.func.battlepass.BattlePassUtil
import me.func.battlepass.quest.QuestType
import me.func.day.Day
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.ModHelper
import me.func.protocol.GlowColor
import me.func.user.User
import me.func.util.Music
import net.minecraft.server.v1_12_R1.EnumMoveType
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import ru.cristalix.core.util.UtilEntity
import java.util.*
import kotlin.math.abs

class TugOfWar(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val duration = 1 * 60 + 20
    override val description = "Зацепите соперника, чтобы скинуть его!"
    override val title = "Перетягивание"

    private val spawn = game.map.getLabel("day3").toCenterLocation()
    private val center = game.map.getLabel("tug-center").toCenterLocation()
    private val teams = game.map.getLabels("tug")
        .map { TugTeam(UUID.randomUUID(), it.toCenterLocation(), mutableSetOf()) }
        .associateBy { it.uuid }
    private val bonus = game.map.getLabels("tug-bonus").map { it.toCenterLocation().add(0.0, 1.0, 0.0) }
    private var gameStarted = false

    private val hook = item {
        type = Material.FISHING_ROD
        text("§bЗацепите соперника")
        nbt("Unbreakable", 1)
    }

    init {
        spawn.yaw = -180f
    }

    override fun join(player: Player) {
        player.teleport(spawn)
        if (game.timer.time > 0)
            startPersonal(player)
    }

    override fun tick(time: Int): Int {
        if (!game.timer.stop) {
            teams.values.forEach { team ->
                val pose = team.gear.headPose
                pose.y += Math.PI / 14
                team.gear.headPose = pose

                val location = team.gear.location
                val dx = (center.x - location.x) / duration / 17
                (team.gear as CraftArmorStand).handle.move(EnumMoveType.SELF, dx, 0.0, 0.0,)

                team.players.filter { abs(center.x - it.player!!.location.x) + 2 > abs(center.x - location.x) }
                    .forEach { it.player?.velocity = Vector(dx * 50, 0.05, 0.0) }
            }
        }
        return time
    }

    override fun registerHandlers(context: EventContext) {
        fork = context

        game.map.getLabels("tug-manager").forEach { Workers.TRIANGLE.spawn(it, "§e§lНАЖМИТЕ") }
        game.map.getLabels("tug-circle").forEach { Workers.CIRCLE.spawn(it, "§eСледуйте к мосту") }

        fork.on<EntityDamageEvent> { isCancelled = true }
        fork.on<EntityDamageByEntityEvent> { cancelled = true }

        val upVector = Vector(0.0, 0.2, 0.0)
        val downVector = Vector(0.0, -0.5, 0.0)

        fork.on<PlayerMoveEvent> {
            if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
                val user = app.getUser(player)
                if (!user.spectator && abs(center.x - player.location.x) < 2.3 && center.y - 6 < player.location.y) {
                    player.velocity = downVector
                }
            }
        }

        fork.on<EntityDamageEvent> {
            if (cause == EntityDamageEvent.DamageCause.FALL && damage > 7) {
                val user = app.getUser(entity as Player)
                val team = getTeamByUser(user)
                team.players.remove(user)
                Bonus.JUMP.drop(bonus.minByOrNull { it.distanceSquared(team.spawn) }!!)
                AcceptLose.accept(game, entity as Player)
            }
        }

        fork.on<PlayerFishEvent> {
            if (state == PlayerFishEvent.State.CAUGHT_ENTITY && entity is Player && (entity as Player).isSneaking)
                entity.velocity = upVector
        }

        fork.on<ProjectileHitEvent> {
            if (entity is FishHook && hitEntity != null && hitEntity is Player) {
                val player = (entity as FishHook).shooter as Player
                val user = app.getUser(player)
                val victim = app.getUser(hitEntity as Player)

                if (!player.hasMetadata("tug-team") || !hitEntity.hasMetadata("tug-team"))
                    return@on

                val attackerTeam = getTeamByUser(user)
                val victimTeam = getTeamByUser(victim)

                if (attackerTeam == victimTeam || game.timer.stop) {
                    entity.remove()
                } else {
                    Glow.animate(hitEntity as Player, 0.4, GlowColor.RED)
                    Anime.title(player, "§aСкинуть!\n\n\n")
                    Glow.animate(player, 0.4, GlowColor.GREEN)

                    user.tugs++
                    Arcade.deposit(user.player?.uniqueId!!, 2)
                    BattlePassUtil.update(user.player!!, QuestType.KILL, 1, false)

                }
            }
        }
        fork.on<PlayerUseUnknownEntityEvent> {
            if (hand == EquipmentSlot.OFF_HAND)
                return@on

            if (app.getUser(player).spectator)
                return@on

            addToTeam(player)
        }
    }

    override fun start() {
        gameStarted = true
        teams.values.forEach { UtilEntity.setScale(it.gear, 4.1, 4.1, 4.1) }
        game.getUsers().mapNotNull { it.player }.forEach { startPersonal(it) }
    }

    override fun startPersonal(player: Player) {
        player.removePotionEffect(PotionEffectType.SLOW)
        addToTeam(player)

        app.getUser(player)?.let {
            Music.FUN.play(it)
            it.roundWinner = true
        }
    }

    private fun addToTeam(player: Player) {
        app.getUser(player)?.let { user ->
            if (user.spectator || teams.values.firstOrNull { it.players.contains(user) } != null)
                return

            player.inventory?.addItem(hook)

            val team = getWeakTeam()

            player.teleport(team?.spawn)
            player.setMetadata("tug-team", FixedMetadataValue(app, team?.uuid))
            team?.players?.add(user)
        }
    }

    private fun getWeakTeam() = teams.values.minByOrNull { it.players.size }

    private fun getTeamByUser(user: User) = teams[UUID.fromString(user.player!!.getMetadata("tug-team")[0].asString())]!!

}