package me.func.day.detail

import me.func.user.User
import org.bukkit.Location

data class BreakFormTeam(val team: Figure, val point: Location, val min: Location, val max: Location, val users: MutableList<User>)