package me.func.day.play.glass

import org.bukkit.Location
import org.bukkit.Material

data class Glass(val location: Location, val strong: Boolean, var standing: Boolean = true) {

    fun kill() {
        if (!standing)
            return

        standing = false

        val copy = location.clone()

        for (x in 0..1) {
            for (z in 0..1) {
                copy.set(location.x + x, location.y, location.z + z)
                copy.block.setTypeAndDataFast(0, 0)
                copy.world.spawnFallingBlock(copy, Material.GLASS.id, 0)
            }
        }
    }

    private val expandSquare = 0.3

    fun inside(to: Location): Boolean {
        return to.x > location.blockX - expandSquare && to.x < location.blockX + 2 + expandSquare &&
                to.z > location.blockZ - expandSquare && to.z < location.blockZ + 2 + expandSquare
    }
}