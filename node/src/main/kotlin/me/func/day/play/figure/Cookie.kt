package me.func.day.play.figure

import implario.humanize.Humanize
import me.func.accept.AcceptLose
import me.func.SquidGame
import me.func.day.detail.Figure
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.Location
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

data class Cookie(val type: Figure, val spawn: Location, var hasOwner: Boolean = false, var hearts: Int = 1) {

    private val slowDigging = PotionEffect(PotionEffectType.SLOW_DIGGING, 4 * 20, 255)

    fun acceptBlockBreak(game: SquidGame, user: User, location: Location) {
        if (!hasOwner || user.roundWinner)
            return
        val block = location.block

        if (block.data == 4.toByte())
            return

        if (hearts > 0) {
            hearts--
            ModHelper.glow(user, 255, 0, 0)
            ModHelper.title(user, "Осталась $hearts ${Humanize.plurals("ошибка", "ошибки", "ошибок", hearts)}")
            user.player?.addPotionEffect(slowDigging)
        } else {
            AcceptLose.accept(game, user)
        }
    }

    fun done(): Boolean {
        val clone = spawn.clone()
        val size = 9

        repeat(size) { x ->
            repeat(size) { z ->
                clone.set(spawn.x - size / 2.0 + x, spawn.y - 1, spawn.z - size / 2.0 + z)
                if (clone.block.data == 4.toByte())
                    return false
            }
        }
        return true
    }

}
