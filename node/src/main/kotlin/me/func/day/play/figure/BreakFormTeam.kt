package me.func.day.play.figure

import me.func.day.detail.Figure
import me.func.user.User
import org.bukkit.Location

data class BreakFormTeam(val team: Figure, val point: Location, val min: Location, val max: Location, val users: MutableList<User>)