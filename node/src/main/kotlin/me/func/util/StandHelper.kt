package me.func.util

import me.func.app
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class StandHelper(location: Location) {

    private var stand: ArmorStand = location.world.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand

    init {
        stand.isInvulnerable = true
    }

    fun invisible(boolean: Boolean): StandHelper {
        stand.isVisible = !boolean
        return this
    }

    fun name(title: String): StandHelper {
        stand.isCustomNameVisible = true
        stand.customName = title
        return this
    }

    fun gravity(boolean: Boolean): StandHelper {
        stand.setGravity(boolean)
        return this
    }

    fun marker(boolean: Boolean): StandHelper {
        stand.isMarker = boolean
        return this
    }

    fun child(boolean: Boolean): StandHelper {
        stand.isSmall = boolean
        return this
    }

    fun fixedData(key: String, value: Any): StandHelper {
        stand.setMetadata(key, FixedMetadataValue(app, value))
        return this
    }

    fun markTrash(): StandHelper {
        fixedData("trash", true)
        return this
    }

    fun slot(slot: EnumItemSlot, item: ItemStack): StandHelper {
        (stand as CraftArmorStand).handle.setSlot(slot, CraftItemStack.asNMSCopy(item))
        return this
    }

    fun build(): ArmorStand {
        return stand
    }

}

fun ArmorStand.setSlot(slot: EnumItemSlot, item: ItemStack) {
    (this as CraftArmorStand).handle.setSlot(slot, CraftItemStack.asNMSCopy(item))
}
