package me.func.day.play.tug

import me.func.user.User
import org.bukkit.Location
import java.util.*

data class TugTeam(val uuid: UUID, val spawn: Location, val players: MutableSet<User>)