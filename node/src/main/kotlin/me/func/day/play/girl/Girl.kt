package me.func.day.play.girl

import dev.implario.bukkit.item.item
import dev.implario.bukkit.item.itemBuilder
import me.func.SquidGame
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.ModHelper
import me.func.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import ru.cristalix.core.util.UtilEntity

class Girl(val location: Location) {

    var forwardView = false

    private val staticBody = StandHelper(location)
        .invisible(true)
        .gravity(false)
        .slot(EnumItemSlot.HEAD, itemBuilder {
            type = Material.CLAY_BALL
            nbt("squidgame", "body")
        }.build()).marker(true)
        .build()

    private val head = StandHelper(location.clone().add(0.0, 3.8, 0.0))
        .invisible(true)
        .gravity(false)
        .slot(EnumItemSlot.HEAD, itemBuilder {
            type = Material.CLAY_BALL
            nbt("squidgame", "head")
        }.build()).marker(true)
        .build()

    init {
        val copy = head.location
        copy.yaw = 180f
        head.teleport(copy)
        UtilEntity.setScale(staticBody, 1.7, 1.7, 1.7)
        UtilEntity.setScale(head, 1.7, 1.7, 1.7)
    }

    fun rotate(game: SquidGame) {
        forwardView = !canView()

        game.getUsers().filter { !it.roundWinner }.mapNotNull { it.player }.forEach {
            Glow.animate(it, 0.4, if (forwardView) 255 else 0, 0, if (forwardView) 0 else 255)
            Anime.title(it, (if (forwardView) "§c" else "§a") + "◉◉◉\n\n\n\n\n\n")
        }

        val angle = 15f

        game.context.after(5) {
            game.context.every(1) {
                val clone = head.location

                if (forwardView && clone.yaw != 0f) {
                    clone.yaw += if (clone.yaw > 0) -angle else angle
                } else if (!forwardView && clone.yaw != 180f) {
                    clone.yaw += if (clone.yaw > 180) -angle else angle
                } else {
                    it.cancel()
                }

                head.teleport(clone)
            }
        }
    }

    fun canView() = head.location.yaw == 0f
}