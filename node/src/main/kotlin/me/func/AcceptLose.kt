package me.func

import me.func.mod.ModHelper
import me.func.mod.ModTransfer
import me.func.user.User
import me.func.util.Firework
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import ru.cristalix.core.formatting.Formatting
import java.util.function.BiConsumer

object AcceptLose : BiConsumer<SquidGame, User> {

    override fun accept(game: SquidGame, user: User) {
        if (user.spectator)
            return
        user.roundWinner = false
        user.spectator = true
        user.team = null
        user.stat.games++
        ModTransfer().integer(RESPAWN_COST + 5 * user.respawn).send("func:try-respawn", user)

        val player = user.player

        player.gameMode = GameMode.SPECTATOR
        player.velocity = Vector(0.0, 0.0, 0.0)
        player.teleport(player.location.clone().add(0.0, 1.8, 0.0))

        Firework.generate(player.location, game.context, Color.RED, Color.YELLOW)

        val location = player.location.clone().subtract(0.0, 0.15, 0.0)

        game.getUsers().forEach {
            ModHelper.playersLeft(it, game.getVictims().size)
            ModHelper.notify(it, "§c${user.player.name} §f#${user.number} §7выбывает.")
            ModHelper.unaryCorpse(it, player.name, player.uniqueId, location.x, location.y, location.z)
        }

        ModHelper.glow(user, 255, 0, 0)
    }
}