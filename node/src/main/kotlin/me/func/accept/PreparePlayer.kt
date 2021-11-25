package me.func.accept

import dev.implario.bukkit.item.item
import dev.implario.games5e.sdk.cristalix.ModLoader
import me.func.SquidGame
import me.func.day.WaitingGame
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment

object PreparePlayer : (User, SquidGame) -> (Unit) {

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

    override fun invoke(user: User, game: SquidGame) {
        game.context.after(1) {
            ModLoader.manyToOne(user.player!!)

            val player = user.player
            val message: String

            user.stat.lastSeenName = game.cristalix.getPlayer(player?.uniqueId).displayName

            if (game.timer.activeDay is WaitingGame) {
                user.player?.gameMode = GameMode.ADVENTURE
                user.roundWinner = true
                user.number = getNumber(game)
                user.player?.displayName = user.stat.lastSeenName + " §f#" + user.number
                user.player?.customName = user.stat.lastSeenName

                message = "§a${user.player?.name} §f#${user.number} §7участвует."

                player?.inventory?.setItem(8, if (user.stat.music) musicOn else musicOff)
            } else {
                user.player?.gameMode = GameMode.SPECTATOR
                user.spectator = true

                message = "§a${user.player?.name} §7смотрит игру."
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

