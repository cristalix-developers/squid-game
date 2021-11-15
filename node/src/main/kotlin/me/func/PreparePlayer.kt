package me.func

import dev.implario.games5e.sdk.cristalix.ModLoader
import me.func.day.WaitingGame
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.GameMode

object PreparePlayer : (User, SquidGame) -> (Unit) {

    override fun invoke(user: User, game: SquidGame) {
        game.context.after(1) {
            ModLoader.manyToOne(user.player)

            val message: String

            if (game.timer.activeDay is WaitingGame) {
                user.player.gameMode = GameMode.ADVENTURE
                user.roundWinner = true
                user.number = getNumber(game)
                user.player.displayName = "§7" +user.player.displayName + " §f#" + user.number
                user.player.customName = user.player.displayName

                message = "§a${user.player.name} §f#${user.number} §7участвует."
            } else {
                user.player.gameMode = GameMode.SPECTATOR
                user.spectator = true

                message = "§a${user.player.name} §7смотрит игру."
            }
            game.timer.activeDay.join(user)

            val users = game.getVictims()
            users.forEach { current ->
                ModHelper.playersLeft(current, users.size)
                ModHelper.notify(current, message)
            }
        }
    }

    private fun getNumber(game: SquidGame): Int {
        val users = game.getUsers()
        repeat(game.getUsers().size) { counter ->
            if (!users.any { it.number == counter + 1 })
                return counter + 1
        }
        return users.size
    }
}

