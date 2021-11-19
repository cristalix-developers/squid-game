package me.func.day.play

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.AcceptLose
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.misc.Bonus
import me.func.user.User
import me.func.util.Music
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TntRun(private val game: SquidGame) : Day {

    override val duration = 1 * 60
    override val description = arrayOf(
        "   §7Бегите по блокам,",
        "   §7но продержитесь до",
        "   §7завершения."
    )
    override val title = "Аккуратность"
    override lateinit var fork: EventContext

    private val bonus = game.map.getLabels("tnt-bonus").map { it.clone().add(0.0, 2.0, 0.0) }
    private val slow = PotionEffect(PotionEffectType.SLOW, 2 * 20, 2)

    override fun join(user: User) {
        user.player.teleport(bonus.random())
        user.player.addPotionEffect(slow)
    }

    override fun tick(time: Int): Int {
        if (time % 5 == 0 && time > 0) {
            game.getVictims().forEach { downgrade(it.player.location.block.getRelative(BlockFace.DOWN)) }
        }
        if (!game.timer.stop && time % 5 == 0 && time > 0) {
            generateBonus()
        }
        return time
    }

    override fun registerHandlers(context: EventContext) {
        fork = context

        fork.on<EntityDamageEvent> {
            if (cause == EntityDamageEvent.DamageCause.FALL && damage > 3)
                AcceptLose.accept(game, app.getUser(entity as Player))
            isCancelled = true
        }

        fork.on<ProjectileHitEvent> {
            if (hitBlock != null) {
                BlockFace.values().forEach { face -> downgrade(hitBlock.getRelative(face)) }
            }
        }
    }

    override fun start() {
        game.getUsers().forEach { startPersonal(it) }
    }

    override fun startPersonal(user: User) {
        Music.FUN.play(user)

        if (!user.spectator) {
            val origin = bonus.random()
            user.player.teleport(origin)
            if (user.roundWinner) {
                val size = 6
                val start = user.player.location

                repeat(size) { x ->
                    repeat(size) { z ->
                        start.set(origin.x - size / 2 + x, origin.y - 3, origin.z - size / 2 + z)
                        start.block.type = Material.STAINED_CLAY
                    }
                }
            }
        }

        user.roundWinner = true
    }

    private fun downgrade(block: Block) {
        if (block.type == Material.AIR || block.type == Material.BARRIER || game.timer.stop)
            return
        val data = when (block.data) {
            3.toByte() -> 0
            0.toByte() -> 4
            4.toByte() -> 6
            6.toByte() -> 14
            else -> -1
        }
        game.after(4) {
            block.setTypeAndDataFast(if (data >= 0) 159 else 0, data.toByte())
        }
    }

    private val drop = setOf(
        Bonus.SUPER_SONIC,
        Bonus.SNOWBALL,
        Bonus.JUMP
    )

    private fun generateBonus() {
        val place = bonus.filter { it.block.getRelative(BlockFace.DOWN).type != Material.AIR }
        if (place.isNotEmpty())
            drop.random().drop(place.random())
    }
}
