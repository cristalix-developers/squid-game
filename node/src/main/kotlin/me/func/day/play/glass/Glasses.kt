package me.func.day.play.glass

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.AcceptLose
import me.func.AcceptRoundWin
import me.func.SquidGame
import me.func.app
import me.func.day.Day
import me.func.day.misc.Workers
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector

class Glasses(private val game: SquidGame) : Day {
    override val duration = 2 * 60
    override lateinit var fork: EventContext
    override val description = arrayOf(
        "   §7Вам нужно выбирать одно",
        "   §7ищ двух стёкол, чтобы",
        "   §7дойти до конца вовремя."
    )
    override val title = "Дорога стёкол"

    private val spawn = game.map.getLabel("day5").toCenterLocation()
    private val finish = game.map.getLabel("finish-glass").toCenterLocation()
    private val glasses = game.map.getLabels("glass").map {
        val leftGlassIsStrong = Math.random() > 0.5 // шанс того, что левое стекло прочное
        listOf(
            Glass(it.clone().subtract(0.0, 1.0, 0.0), leftGlassIsStrong),
            Glass(it.clone().subtract(-3.0, 1.0, 0.0), !leftGlassIsStrong)
        )
    }.flatten()

    override fun join(user: User) {
        user.player.teleport(spawn)
    }

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        fork = context

        game.map.getLabels("glass-circle").forEach { Workers.CIRCLE.spawn(it) }

        val nullVector = Vector(0.0, 0.0, 0.0)

        fork.on<EntityDamageEvent> {
            if (cause == EntityDamageEvent.DamageCause.FALL && damage > 3.0)
                AcceptLose.accept(game, app.getUser(entity as Player))
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
            if (currentGlass != null && !currentGlass.strong) {
                currentGlass.kill()
                player.velocity = nullVector
            }
        }
    }

    override fun start() {
        game.getUsers().forEach { startPersonal(it) }
    }

    override fun startPersonal(user: User) {
        ModHelper.title(user, "§eВыбирайте из двух стекл")

        if (!user.spectator) {
            user.player.teleport(spawn)
            user.roundWinner = false
        }
    }
}