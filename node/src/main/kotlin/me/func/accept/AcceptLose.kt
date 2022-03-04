package me.func.accept

import me.func.MINIMUM_PLAYERS_RESPAWN
import me.func.RESPAWN_COST
import me.func.SquidGame
import me.func.app
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.ModHelper
import me.func.mod.conversation.ModTransfer
import me.func.util.Firework
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.function.BiConsumer

object AcceptLose : BiConsumer<SquidGame, Player> {

    override fun accept(game: SquidGame, player: Player) {
        app.getUser(player)?.let { user ->
            if (user.spectator)
                return

            if (game.getVictims().size >= MINIMUM_PLAYERS_RESPAWN)
                ModTransfer().integer(RESPAWN_COST + 2 * user.respawn).send("func:try-respawn", player)

            user.roundWinner = false
            user.spectator = true
            user.team = null
            user.stat.games++

            player.gameMode = GameMode.SPECTATOR
            player.velocity = Vector(0.0, 0.0, 0.0)
            player.teleport(player.location.clone().add(0.0, 1.8, 0.0))

            Firework.generate(player.location, Color.RED, Color.YELLOW)

            val location = player.location.clone().subtract(0.0, 0.15, 0.0)

            game.getUsers().mapNotNull { it.player }.forEach {
                ModHelper.playersLeft(player, game.getVictims().size)
                Anime.killboardMessage(it, "§c${player.name} §f#${user.number} §7выбывает.")
                Anime.corpse(it, player.name, player.uniqueId, location.x, location.y, location.z)
            }

            Glow.animate(player, 0.4, 255, 0, 0)
        }
    }
}