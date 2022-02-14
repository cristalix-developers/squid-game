package me.func.accept

import dev.implario.bukkit.item.item
import me.func.SquidGame
import me.func.app
import me.func.day.WaitingGame
import me.func.mod.ModHelper
import me.func.mod.conversation.ModLoader
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

object PreparePlayer : (Player, SquidGame) -> (Unit) {

    val musicOn = item {
        type = Material.CLAY_BALL
        nbt("other", "settings1")
        text("§cОтключить музыку")
    }

    val musicOff = item {
        type = Material.CLAY_BALL
        enchant(Enchantment.LUCK, 1)
        nbt("other", "settings1")
        nbt("HideFlags", 63)
        text("§bВключить музыку")
    }

    override fun invoke(player: Player, game: SquidGame) {
        game.context.after(1) {
            ModLoader.manyToOne(player)

            val user = app.getUser(player)
            val message: String

            user.stat.lastSeenName = game.cristalix.getPlayer(player.uniqueId).displayName

            if (game.timer.activeDay is WaitingGame) {
                player.gameMode = GameMode.ADVENTURE
                user.roundWinner = true
                user.number = getNumber(game)
                player.displayName = user.stat.lastSeenName + " §f#" + user.number
                player.customName = user.stat.lastSeenName

                message = "§a${user.player?.name} §f#${user.number} §7участвует."

                player.inventory?.setItem(8, if (user.stat.music) musicOn else musicOff)
            } else {
                player.gameMode = GameMode.SPECTATOR
                user.spectator = true

                message = "§a${player.name} §7смотрит игру."
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

