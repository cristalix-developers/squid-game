package me.func.day

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.SquidGame
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import org.bukkit.GameMode
import org.bukkit.event.entity.EntityDamageEvent

class WaitingGame(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val description = arrayOf(
        "§bИгра в кальмара",
        "   §7Уменьшайте количество конкурентов",
        "   §7или прячьтесь от врагов.",
        "   §7Взяв нож вы должны убить",
        "   §7кого-либо, или вы обречены."
    )
    override val title = "Комната ожидания"

    override fun join(user: User) {
        user.spectator = false
        user.player.teleport(game.spawns.random())

        fork.after(1) {
            ModHelper.timer(user, "Ожидание игроков", duration - game.timer.time / 20 - 1)
            Music.LOBBY.play(user)
        }

        user.player.gameMode = GameMode.ADVENTURE
    }

    override val duration = 90

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        this.fork = context
        fork.on<EntityDamageEvent> { isCancelled = true }
    }

    override fun start() {}

    override fun startPersonal(user: User) {}

}