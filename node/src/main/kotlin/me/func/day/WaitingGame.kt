package me.func.day

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.accept.PreparePlayer.musicOff
import me.func.accept.PreparePlayer.musicOn
import me.func.SquidGame
import me.func.app
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import me.func.util.MusicHelper
import org.bukkit.GameMode
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import ru.cristalix.core.formatting.Formatting

class WaitingGame(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val description = "Приятной игры!"
    override val title = "Игра в кальмара"

    override fun join(user: User) {
        user.spectator = false
        user.player?.teleport(game.spawns.random())
        user.player?.sendMessage(Formatting.error("Игра еще не открыта! Происходит настройка связи серверов!"))

        fork.after(1) {
            ModHelper.timer(user, "Ожидание игроков", duration - game.timer.time / 20 - 1)
            Music.LOBBY.play(user)
        }

        user.player?.gameMode = GameMode.ADVENTURE
    }

    override val duration = 1 * 25

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        this.fork = context
        fork.on<EntityDamageEvent> { isCancelled = true }

        fork.on<PlayerInteractEvent> {
            if (hasItem()) {
                val user = app.getUser(player)
                user.stat.music = !user.stat.music

                if (!user.stat.music) {
                    player.itemInHand = musicOff
                    ModHelper.notify(user, "§cМузыка отключена")
                    MusicHelper.stop(user)
                } else {
                    player.itemInHand = musicOn
                    ModHelper.notify(user, "§bМузыка включена")
                    Music.LOBBY.play(user)
                }
            }
        }
    }

    override fun start() {}

    override fun startPersonal(user: User) {}

}