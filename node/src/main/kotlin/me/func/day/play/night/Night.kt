package me.func.day.play.night

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.Arcade
import me.func.accept.AcceptLose
import me.func.SquidGame
import me.func.app
import me.func.battlepass.BattlePassUtil
import me.func.battlepass.quest.QuestType
import me.func.day.Day
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

private const val HEALTH_BARRIER = 7f

class Night(private val game: SquidGame) : Day {

    override val duration = 1 * 60 + 20
    override lateinit var fork: EventContext
    override val description = "Взяв нож вы должны убить\nкого-либо, или вы обречены"
    override val title = "Ночная резня"

    private val blindness = PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1)
    private val generators = game.map.getLabels("knife").map { Knife(it) }

    override fun join(player: Player) { player.teleport(game.spawns.random()) }

    override fun tick(time: Int): Int {
        if (!game.timer.stop) {
            if (time % (10 * 20) == 0) {
                game.getVictims().forEach { it.player?.addPotionEffect(blindness) }
            }

            generators.filter { it.owner != null && it.drop == null }.forEach {
                if (it.owner!!.spectator) {
                    it.spawn()
                    return@forEach
                }
                it.timeLeft--
                if (it.timeLeft <= 0) {
                    AcceptLose.accept(game, it.owner!!.player!!)
                    it.spawn()
                }
            }
        }
        return time
    }

    override fun registerHandlers(context: EventContext) {
        fork = context

        fork.on<PlayerPickupItemEvent> {
            if (item.itemStack.getType() == Material.IRON_SWORD && !player.inventory.contains(Material.IRON_SWORD)) {
                generators.minByOrNull { it.generator.distanceSquared(player.location) }!!.get(player)
            } else {
                isCancelled = true
            }
        }
        fork.on<EntityDamageByEntityEvent> {
            if (game.timer.stop) {
                isCancelled = true
                return@on
            }
            if (entity is Player && damager is Player && (damager as Player).itemInHand.getType() == Material.IRON_SWORD) {
                val victim = entity as Player

                damage = 0.0
                victim.health -= 2
                if (victim.health >= HEALTH_BARRIER) {
                    Glow.animate(victim, 0.4, 255, 0, 0)
                    Anime.title(victim, "§cВас атакуют!\n\n\n")
                } else {
                    (damager as Player).itemInHand = null
                    generators.filter { it.owner != null && it.owner!!.player == damager }.forEach {
                        Glow.animate(it.owner!!.player!!, 0.4, 0, 0, 255)
                        it.spawn()
                    }
                }
            } else {
                isCancelled = true
            }
        }
        fork.on<EntityDamageEvent> {
            val player = entity
            if (!game.timer.stop && player is Player && player.health < HEALTH_BARRIER) {
                val user = app.getUser(player)
                AcceptLose.accept(game, player)
                user.kills++
                Arcade.deposit(user.player?.uniqueId!!, 2)
                BattlePassUtil.update(user.player!!, QuestType.KILL, 1, false)
            } else {
                isCancelled = true
            }
        }
    }

    override fun start() {
        generators.forEach { it.spawn() }
        game.getUsers().mapNotNull { it.player }.forEach { startPersonal(it) }
    }

    override fun startPersonal(player: Player) {
        player.health = player.maxHealth
        player.addPotionEffect(blindness)
        player.teleport(game.spawns.random())
        app.getUser(player)?.let {
            Music.LOBBY.play(it)
            it.roundWinner = true
        }
    }
}