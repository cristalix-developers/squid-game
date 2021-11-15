package me.func.day.detail

import dev.implario.bukkit.item.item
import me.func.app
import me.func.mod.ModHelper
import me.func.user.User
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Item
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

    fun get(user: User) {
        if (user.spectator)
            return

        ModHelper.glow(user, 42, 189, 102)
        ModHelper.title(user, "§bУ вас ${DURATION / 20} секунд на убийство")
        drop!!.remove()
        drop = null
        owner = user
        user.player.addPotionEffect(SPEED)
        timeLeft = DURATION
    }
}
