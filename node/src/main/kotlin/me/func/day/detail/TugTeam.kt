package me.func.day.detail

import me.func.user.User
import org.bukkit.Location
import java.util.*

data class TugTeam(val uuid: UUID, val spawn: Location, val players: MutableSet<User>)