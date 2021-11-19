package me.func

import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Firework
import org.bukkit.Color
import java.util.function.BiConsumer

object AcceptRoundWin : BiConsumer<SquidGame, User> {

    override fun accept(game: SquidGame, user: User) {
        if (user.spectator || user.roundWinner)
            return

        user.roundWinner = true

        val player = user.player

        if (Math.random() < 0.3)
            Firework.generate(player.location, game.context, Color.AQUA, Color.LIME)

        ModHelper.notifyAll(game, "§b${user.player.name} §f#${user.number} §7прошел испытание.")
        ModHelper.glow(user, 0, 0, 255)
    }
}