package me.func.day.play.tug

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import me.func.AcceptLose
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import org.bukkit.Material
import org.bukkit.entity.FishHook
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.Vector
import java.util.*

class TugOfWar(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val duration = 1 * 60 + 20
    override val description = arrayOf(
        "§fИспытание §b§l#3§b: Перетягивание каната",
        "   §7Поднимитесь на мост и",
        "   §7зацепите соперника, чтобы",
        "   §7скинуть его вниз",
    )
    override val title = "Перетягивание каната"

    private val spawn = game.map.getLabel("day3").toCenterLocation()
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


    override fun join(user: User) {
        user.player.teleport(spawn)
        if (game.timer.time > 0)
            startPersonal(user)
    }

    override fun tick(time: Int): Int {
        if (game.timer.stop && game.timer.time > 0) {
            getWeakTeam().players.forEach { it.roundWinner = false }
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

        fork.on<EntityDamageEvent> {
            if (cause == EntityDamageEvent.DamageCause.FALL) {
                val user = app.getUser(entity as Player)
                val team = getTeamByUser(user)
                team.players.remove(user)
                Bonus.JUMP.drop(bonus.minBy { it.distanceSquared(team.spawn) }!!)
                AcceptLose.accept(game, user)
            }
        }

        fork.on<PlayerFishEvent> {
            if (state == PlayerFishEvent.State.CAUGHT_ENTITY && entity is Player && (entity as Player).isSneaking)
                entity.velocity = upVector
        }

        fork.on<ProjectileHitEvent> {
            if (entity is FishHook && hitEntity != null && hitEntity is Player) {
                val user = app.getUser((entity as FishHook).shooter as Player)
                val victim = app.getUser(hitEntity as Player)

                val attackerTeam = getTeamByUser(user)
                val victimTeam = getTeamByUser(victim)

                if (attackerTeam == victimTeam) {
                    entity.remove()
                } else {
                    ModHelper.glow(victim, 255, 0, 0)
                    ModHelper.title(user, "§aСкинуть!\n\n\n")
                    ModHelper.glow(user, 0, 0, 255)
                }
            }
        }
        fork.on<PlayerUseUnknownEntityEvent> {
            if (hand == EquipmentSlot.OFF_HAND)
                return@on
            val user = app.getUser(player)

            if (user.spectator)
                return@on

            if (teams.values.firstOrNull { it.players.contains(user) } != null)
                return@on

            addToTeam(user)
        }
    }

    override fun start() {
        gameStarted = true
        game.getUsers().forEach {
            startPersonal(it)
        }
    }

    override fun startPersonal(user: User) {
        Music.FUN.play(user)
        if (user.team == null)
            addToTeam(user)
        user.roundWinner = true
        user.player.inventory.addItem(hook)
        ModHelper.title(user, "§bСкиньте соперника!")
    }

    private fun addToTeam(user: User) {
        if (user.spectator)
            return

        val team = getWeakTeam()

        user.player.teleport(team.spawn)
        user.player.setMetadata("tug-team", FixedMetadataValue(app, team.uuid))
        team.players.add(user)
    }

    private fun getWeakTeam(): TugTeam {
        return teams.values.minBy { it.players.size }!!
    }

    private fun getTeamByUser(user: User): TugTeam {
        return teams[UUID.fromString(user.player.getMetadata("tug-team")[0].asString())]!!
    }

}