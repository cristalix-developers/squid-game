package me.func

import me.func.mod.ModHelper
import me.func.user.User
import me.func.util.Firework
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.util.Vector
import ru.cristalix.core.formatting.Formatting
import java.util.function.BiConsumer

object AcceptLose : BiConsumer<SquidGame, User> {

    override fun accept(game: SquidGame, user: User) {
        if (user.spectator)
            return
        user.roundWinner = false
        user.spectator = true
        user.stat.games++

        val player = user.player

        Firework.generate(player.location, game.context, Color.RED, Color.YELLOW)

        val location = player.location.clone().subtract(0.0, 0.15, 0.0)

        game.getUsers().forEach {
            ModHelper.playersLeft(it, game.getVictims().size)
            ModHelper.notify(it, "§c${user.player.name} §f#${user.number} §7выбывает.")
            ModHelper.unaryCorpse(it, player.name, player.uniqueId, location.x, location.y, location.z)
        }

        player.gameMode = GameMode.SPECTATOR
        ModHelper.glow(user, 255, 0, 0)
        ModHelper.title(user, "㥏\n\n§cСмерть...")

        //game.after(100) {
        //    player.inventory.clear()
        //    user.spectator = false
        //    user.roundWinner = false
        //    game.timer.activeDay.join(user)
        //    game.timer.activeDay.startPersonal(user)
        //    player.gameMode = GameMode.ADVENTURE
        //}
    }
}