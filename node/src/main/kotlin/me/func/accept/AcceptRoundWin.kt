package me.func.accept

import me.func.SquidGame
import me.func.app
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.util.Firework
import org.bukkit.Color
import org.bukkit.entity.Player
import java.util.function.BiConsumer

object AcceptRoundWin : BiConsumer<SquidGame, Player> {

    override fun accept(game: SquidGame, player: Player) {
        app.getUser(player)?.let { user ->
            if (user.spectator || user.roundWinner)
                return

            user.roundWinner = true

            if (Math.random() < 0.8)
                Firework.generate(player.location, Color.AQUA, Color.LIME)

            game.getUsers().mapNotNull { it.player }.forEach {
                Anime.killboardMessage(it, "§b${user.player?.name} §f#${user.number} §7прошел испытание.")
            }
            Glow.animate(player, 0.4, 0, 0, 255)
        }
    }
}