package me.func.day.play.girl

import me.func.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*

data class Gun(val location: Location, val uuid: UUID? = UUID.randomUUID()) {

    private val zero = Vector(0, 0, 0)

    private val bullet = StandHelper(location)
        .invisible(true)
        .gravity(false)
        .slot(EnumItemSlot.HEAD, ItemStack(Material.COAL_BLOCK))
        .fixedData("gun", uuid.toString())
        .markTrash()
        .build()

    fun shoot(target: Location) {
        if (location.distanceSquared(bullet.location) > 2.0)
            return

        bullet.setGravity(true)

        val origin = bullet.location
        bullet.velocity = Vector(
            target.x - origin.x,
            target.y - origin.y,
            target.z - origin.z
        ).multiply(0.2)
    }

    fun refill(): Boolean {
        if (bullet.location.y < 103) {
            bullet.setGravity(false)
            bullet.teleport(location)
            bullet.velocity = zero.clone()
            return true
        }
        return false
    }

}
