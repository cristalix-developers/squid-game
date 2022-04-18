package me.func.accept

import dev.implario.bukkit.item.item
import dev.implario.bukkit.item.itemBuilder
import me.func.App
import me.func.SquidGame
import me.func.app
import me.func.day.WaitingGame
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.mod.conversation.ModLoader
import net.minecraft.server.v1_12_R1.BlockPosition
import net.minecraft.server.v1_12_R1.World
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import kotlin.math.abs

object PreparePlayer : (Player, SquidGame) -> (Unit) {

    val musicOn = itemBuilder {
        type = Material.CLAY_BALL
        nbt("other", "settings1")
        text("§cОтключить музыку")
    }.build()

    val musicOff = itemBuilder {
        type = Material.CLAY_BALL
        enchant(Enchantment.LUCK, 1)
        nbt("other", "settings1")
        nbt("HideFlags", 63)
        text("§bВключить музыку")
    }.build()

    override fun invoke(player: Player, game: SquidGame) {
        player.setResourcePack("", "")
        Bukkit.getScheduler().runTaskLater(app, { player.setResourcePack("", "") }, 10)

        game.after(1) {
            ModLoader.send("mod-bundle.jar", player)

            val user = app.getUser(player)
            val message: String

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

            user.stat.lastSeenName = game.cristalix.getPlayer(player.uniqueId).displayName
            game.timer.activeDay.join(player)

            val users = game.getVictims()
            users.mapNotNull { it.player }.forEach { current ->
                ModHelper.playersLeft(current, users.size)
                Anime.killboardMessage(current, message)
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
