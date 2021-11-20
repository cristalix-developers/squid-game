package me.func.day.play.night

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.AcceptLose
import me.func.SquidGame
import me.func.app
import me.func.day.Day
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
    override val description = arrayOf(
        "   §7Уменьшайте количество конкурентов",
        "   §7или прячьтесь от врагов.",
        "   §7Взяв нож вы должны убить",
        "   §7кого-либо, или вы обречены."
    )
    override val title = "§cНочная резня"

    private val blindness = PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 1)
    private val generators = game.map.getLabels("knife").map { Knife(it) }

    override fun join(user: User) {
        user.player.teleport(game.spawns.random())
    }

    override fun tick(time: Int): Int {
        if (!game.timer.stop) {
            if (time % (10 * 20) == 0) {
                game.getVictims().forEach { it.player.addPotionEffect(blindness) }
            }

            generators.filter { it.owner != null && it.drop == null }.forEach {
                if (it.owner!!.spectator) {
                    it.spawn()
                    return@forEach
                }
                it.timeLeft--
                if (it.timeLeft <= 0) {
                    AcceptLose.accept(game, it.owner!!)
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
                generators.minByOrNull { it.generator.distanceSquared(player.location) }!!.get(app.getUser(player))
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
                    val user = app.getUser(victim)
                    ModHelper.glow(user, 255, 0, 0)
                    ModHelper.title(user, "§cВас атакуют!\n\n\n")
                } else {
                    (damager as Player).itemInHand = null
                    generators.filter { it.owner != null && it.owner!!.player == damager }.forEach {
                        ModHelper.glow(it.owner!!, 0, 0, 255)
                        it.spawn()
                    }
                }
            } else {
                isCancelled = true
            }
        }
        fork.on<EntityDamageEvent> {
            if (!game.timer.stop && entity is Player && (entity as Player).health < HEALTH_BARRIER)
                AcceptLose.accept(game, app.getUser(entity as Player))
            else
                isCancelled = true
        }
    }

    override fun start() {
        generators.forEach { it.spawn() }
        game.getUsers().forEach { startPersonal(it) }
    }

    override fun startPersonal(user: User) {
        Music.LOBBY.play(user)
        user.player.health = user.player.maxHealth
        user.roundWinner = true
        user.player.addPotionEffect(blindness)
        user.player.teleport(game.spawns.random())
    }
}