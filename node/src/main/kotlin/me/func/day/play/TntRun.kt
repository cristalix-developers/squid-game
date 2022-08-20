package me.func.day.play

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.SquidGame
import me.func.accept.AcceptLose
import me.func.app
import me.func.day.Day
import me.func.day.misc.Bonus
import me.func.util.Music
import org.bukkit.Location
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
    override val description = "Бегите по блокам, но продержитесь\nдо завершения"
    override val title = "Аккуратность"
    override lateinit var fork: EventContext

    private val bonus = game.map.getLabels("tnt-bonus").map { it.clone().add(0.0, 2.0, 0.0) }
    private val slow = PotionEffect(PotionEffectType.SLOW, 2 * 20, 2)

    override fun join(player: Player) {
        player.teleport(bonus.random())
        player.addPotionEffect(slow)
    }

    override fun tick(time: Int): Int {
        if (time % 5 == 0 && time > 0) {
            game.getVictims().forEach {
                it.blockBreak++
                downgrade(getBlockBelowPlayer(it.player!!.location.clone().subtract(0.0, 1.0, 0.0).blockY, it.player!!.location)!!)
            }
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
                AcceptLose.accept(game, entity as Player)
            isCancelled = true
        }

        fork.on<ProjectileHitEvent> {
            if (hitBlock != null && hitBlock.type == Material.STAINED_CLAY) {
                BlockFace.values().forEach { face -> downgrade(hitBlock.getRelative(face)) }
            }
        }
    }

    override fun start() = game.getUsers().mapNotNull { it.player }.forEach { startPersonal(it) }

    override fun startPersonal(player: Player) {
        app.getUser(player)?.let { user ->
            Music.FUN.play(user)

            if (!user.spectator) {
                val origin = bonus.random()
                player.teleport(origin)
                if (user.roundWinner) {
                    val size = 6
                    val start = player.location

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
        drop.random().drop(bonus.random())
    }

    private fun getBlockBelowPlayer(y: Int, loc: Location): Block? {
        val world = loc.world
        val x = loc.x
        val z = loc.z

        val block1 = Location(world, x + 0.3, y.toDouble(), z - 0.3).block
        if(block1.type !== Material.AIR) return block1

        val block2 = Location(world, x - 0.3, y.toDouble(), z + 0.3).block
        if(block2.type !== Material.AIR) return block2

        val block3 = Location(world, x + 0.3, y.toDouble(), z + 0.3).block
        if(block3.type !== Material.AIR) return block3

        val block4 = Location(world, x - 0.3, y.toDouble(), z - 0.3).block
        return if(block4.type !== Material.AIR) block4 else null
    }
}
