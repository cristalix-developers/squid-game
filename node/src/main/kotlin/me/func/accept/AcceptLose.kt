package me.func.accept

import me.func.MINIMUM_PLAYERS_RESPAWN
import me.func.RESPAWN_COST
import me.func.SquidGame
import me.func.mod.ModHelper
import me.func.mod.ModTransfer
import me.func.user.User
import me.func.util.Firework
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.util.Vector
import java.util.function.BiConsumer

object AcceptLose : BiConsumer<SquidGame, User> {

    override fun accept(game: SquidGame, user: User) {
        if (user.spectator)
            return

        if (game.getVictims().size >= MINIMUM_PLAYERS_RESPAWN)
            ModTransfer().integer(RESPAWN_COST + 5 * user.respawn).send("func:try-respawn", user)

        user.roundWinner = false
        user.spectator = true
        user.team = null
        user.stat.games++

        val player = user.player

        player?.gameMode = GameMode.SPECTATOR
        player?.velocity = Vector(0.0, 0.0, 0.0)
        player?.teleport(player.location.clone().add(0.0, 1.8, 0.0))

        Firework.generate(player!!.location, game.context, Color.RED, Color.YELLOW)

        val location = player.location.clone().subtract(0.0, 0.15, 0.0)

        game.getUsers().forEach {
            ModHelper.playersLeft(it, game.getVictims().size)
            ModHelper.notify(it, "§c${user.player!!.name} §f#${user.number} §7выбывает.")
            ModHelper.unaryCorpse(it, player.name, player.uniqueId, location.x, location.y, location.z)
        }

        ModHelper.glow(user, 255, 0, 0)
    }
}