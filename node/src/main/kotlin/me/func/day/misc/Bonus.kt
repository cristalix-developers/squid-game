package me.func.day.misc

import dev.implario.bukkit.item.item
import dev.implario.bukkit.item.itemBuilder
import me.func.app
import me.func.mod.Glow
import me.func.mod.ModHelper
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

enum class Bonus(private val itemStack: ItemStack, private val stackable: Boolean, val effect: (Player) -> Unit) {

    SPEED(itemBuilder {
        type = Material.CLAY_BALL
        text("§bСкорость §l§fПКМ")
        nbt("bonus", "speed")
        nbt("museum", "small_crystal_blue")
    }.build(), false, { }),
    WEB(itemBuilder {
        type = Material.WEB
        text("§bПаутина §l§fКИНУТЬ")
        nbt("bonus", "web")
    }.build(), false, { }),
    SUPER_SONIC(itemBuilder {
        type = Material.CLAY_BALL
        text("§bСкорость 1")
        nbt("bonus", "super_sonic")
        nbt("museum", "small_crystal_blue")
    }.build(), false, {
        it.player?.addPotionEffect(
            org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED,
                20 * 999,
                0
            )
        )
    }),
    SNOWBALL(itemBuilder {
        type = Material.SNOW_BALL
        text("§bСнаряд §l§fПКМ")
        nbt("bonus", "snowball")
    }.build(), true, { }),
    JUMP(itemBuilder {
        type = Material.CLAY_BALL
        text("§aПрыгучесть 3")
        nbt("museum", "small_crystal_green")
        nbt("bonus", "jump")
        nbt("random", Math.random() * 1000) // make item non-stackable
    }.build(), false, {
        it.player?.addPotionEffect(
            org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.JUMP,
                20 * 999,
                3
            )
        )
    }), ;

    fun drop(location: Location) {
        val item = location.world.dropItemNaturally(location, itemStack)
        item.setMetadata("trash", FixedMetadataValue(app, 0))
    }

    fun give(player: Player): Boolean {
        if (stackable || !player.inventory.contains(itemStack)) {
            effect(player)
            Glow.animate(player, 0.4, 42, 189, 102)
            return true
        }
        return false
    }

}