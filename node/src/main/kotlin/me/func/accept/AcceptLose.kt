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
import net.minecraft.server.v1_12_R1.MinecraftServer
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

            // Воскрешение игрока
            if (game.lite && user.hearts > 0) {
                user.player?.inventory?.clear()
                user.spectator = false
                user.roundWinner = true

                var hearts = "§c"
                repeat(user.hearts) { hearts = "$hearts❤" }

                player.sendActionBar("§e§lПотеряна жизнь! §c§l$hearts осталось")

                MinecraftServer.SERVER.postToMainThread {
                    game.timer.activeDay.startPersonal(player)
                    player.gameMode = GameMode.ADVENTURE
                }

                user.hearts--
            }
            // Показать игроку меню покупки воскрешения
            if (game.getVictims().size >= MINIMUM_PLAYERS_RESPAWN && (user.hearts < 1 || !game.lite)) {
                ModTransfer().integer(RESPAWN_COST + 2 * user.respawn).send("func:try-respawn", player)
            }
        }
    }
}