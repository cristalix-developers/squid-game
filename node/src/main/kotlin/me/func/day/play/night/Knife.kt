package me.func.day.play.night

import dev.implario.bukkit.item.item
import me.func.app
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

const val DURATION = 35 * 20

private val SPEED = PotionEffect(PotionEffectType.SPEED, DURATION, 0)

data class Knife(
    val generator: Location,
    var owner: User? = null,
    var drop: Item? = null,
    var timeLeft: Int = DURATION
) {
    fun spawn() {
        owner = null
        drop = generator.world.dropItemNaturally(generator, item {
            type = Material.IRON_SWORD
            text("§bТы знаешь, что делать.")
            nbt("weapons", "iron_dagger")
            nbt("Unbreakable", 1)
        })
        drop!!.setMetadata("trash", FixedMetadataValue(app, 0))
    }

    fun get(player: Player) {
        app.getUser(player)?.let {
            if (it.spectator)
                return

            Glow.animate(player, 0.4, 42, 189, 102)
            Anime.title(player, "§bУ вас ${DURATION / 20} секунд на убийство")
            drop!!.remove()
            drop = null
            owner = it
            player.addPotionEffect(SPEED)
            timeLeft = DURATION
        }
    }
}
