package me.func.day

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.accept.PreparePlayer.musicOff
import me.func.accept.PreparePlayer.musicOn
import me.func.SquidGame
import me.func.app
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Music
import me.func.util.MusicHelper
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent

class WaitingGame(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val description = "Приятной игры!"
    override val title = "Игра в кальмара"

    override fun join(player: Player) {
        player.teleport(game.spawns.random())
        app.getUser(player)?.let { user ->
            user.spectator = false

            fork.after(1) {
                Anime.timer(player, "Ожидание игроков", duration - game.timer.time / 20 - 1)
                Music.LOBBY.play(user)
            }
        }

        player.gameMode = GameMode.ADVENTURE
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
                    Anime.killboardMessage(player, "§cМузыка отключена")
                    MusicHelper.stop(user)
                } else {
                    player.itemInHand = musicOn
                    Anime.killboardMessage(player, "§bМузыка включена")
                    Music.LOBBY.play(user)
                }
            }
        }
    }

    override fun start() {}

    override fun startPersonal(player: Player) {}

}