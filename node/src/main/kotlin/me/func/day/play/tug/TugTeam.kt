package me.func.day.play.tug

import dev.implario.bukkit.item.item
import me.func.user.User
import me.func.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import java.util.*

data class TugTeam(
    val uuid: UUID, val spawn: Location, val players: MutableSet<User>,
    val gear: ArmorStand = StandHelper(spawn.clone().subtract(0.0, 6.6, 0.0))
        .invisible(true)
        .marker(true)
        .gravity(false)
        .slot(EnumItemSlot.HEAD, item {
            type = Material.GOLD_BLOCK
        }).build()
)