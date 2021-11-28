package me.func.day.play.glass

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.accept.AcceptLose
import me.func.accept.AcceptRoundWin
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.misc.Workers
import me.func.mod.ModHelper
import me.func.mod.ModTransfer
import me.func.user.User
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

const val GLASS_DURABILITY = 8

class Glasses(private val game: SquidGame) : Day {

    override val duration = 2 * 60 + 20
    override lateinit var fork: EventContext
    override val description = "Вам нужно выбирать одно из\nдвух стёкол, чтобы победить"
    override val title = "Дорога стёкол"

    private val spawn = game.map.getLabel("day5").toCenterLocation()
    private val alert = game.map.getLabel("alert")
    private val finish = game.map.getLabel("finish-glass").toCenterLocation()
    private val glasses = game.map.getLabels("glass").map {
        val leftGlassIsStrong = Math.random() > 0.5 // шанс того, что левое стекло прочное
        listOf(
            Glass(it.clone().subtract(0.0, 1.0, 0.0), leftGlassIsStrong),
            Glass(it.clone().subtract(-3.0, 1.0, 0.0), !leftGlassIsStrong)
        )
    }.flatten()

    override fun join(user: User) {
        user.player?.teleport(spawn)

        ModTransfer()
            .double(alert.x + 4.5)
            .double(alert.y + 4.5)
            .double(alert.z + 4.5)
            .integer(GLASS_DURABILITY)
            .send("func:glass-alert", user)
    }

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        fork = context

        game.map.getLabels("glass-circle").forEach { Workers.CIRCLE.spawn(it) }

        val nullVector = Vector(0.0, 0.0, 0.0)

        fork.on<EntityDamageEvent> {
            if (entity is Player && cause == EntityDamageEvent.DamageCause.FALL && damage > 5.0) {
                AcceptLose.accept(game, app.getUser(entity as Player))
                app.getUser(entity as Player).hero = true
            }
            isCancelled = true
        }
        fork.on<PlayerMoveEvent> {
            val user = app.getUser(player)

            if (!user.spectator && game.timer.time == 0 && player.location.distanceSquared(spawn) > 4 * 4) {
                player.velocity = spawn.toVector().subtract(player.location.toVector()).multiply(0.08)
            }

            if (user.roundWinner || user.spectator || player.location.y < 111)
                return@on

            if (to.distanceSquared(finish) < 11 * 11)
                AcceptRoundWin.accept(game, user)

            val currentGlass = glasses.find { it.inside(player.location) }
            if (currentGlass != null) {
                if (!currentGlass.strong) {
                    currentGlass.kill(game)
                    player.velocity = nullVector
                } else if (game.getVictims().filter { currentGlass.inside(it.player!!.location) }.size >= GLASS_DURABILITY) {
                    currentGlass.kill(game)
                    fork.after(20 * 2) {
                        currentGlass.fill(Material.GLASS)
                        currentGlass.standing = true
                    }
                }
            }
        }
    }

    override fun start() {
        spawn.yaw = -180f

        game.getUsers().forEach { startPersonal(it) }
    }

    override fun startPersonal(user: User) {
        ModHelper.title(user, "§eВыбирайте из двух стекол")

        if (!user.spectator) {
            user.player?.teleport(spawn)
            user.roundWinner = false
        }
    }
}