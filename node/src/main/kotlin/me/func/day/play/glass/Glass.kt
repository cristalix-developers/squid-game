package me.func.day.play.glass

import me.func.SquidGame
import org.bukkit.Location
import org.bukkit.Material
import ru.cristalix.core.formatting.Formatting

data class Glass(val location: Location, val strong: Boolean, var standing: Boolean = true) {

    fun kill(game: SquidGame) {
        if (!standing)
            return
        if (strong)
            game.broadcast(Formatting.error("Игроки сломали стекло!"))
        standing = false
        fill(Material.AIR)
    }

    private val expandSquare = 0.3

    fun inside(to: Location): Boolean {
        return to.x > location.blockX - expandSquare && to.x < location.blockX + 2 + expandSquare &&
                to.z > location.blockZ - expandSquare && to.z < location.blockZ + 2 + expandSquare
    }

    fun fill(type: Material) {
        val copy = location.clone()

        for (x in 0..1) {
            for (z in 0..1) {
                copy.set(location.x + x, location.y, location.z + z)
                copy.block.setTypeAndDataFast(type.id, 0)
                if (type == Material.AIR)
                    copy.world.spawnFallingBlock(copy, Material.GLASS.id, 0)
            }
        }
    }
}