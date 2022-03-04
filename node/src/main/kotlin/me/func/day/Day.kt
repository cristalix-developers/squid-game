package me.func.day

import dev.implario.bukkit.event.EventContext
import me.func.user.User
import org.bukkit.entity.Player

interface Day {

    fun join(player: Player)
    fun tick(time: Int): Int
    fun registerHandlers(context: EventContext)
    fun start()
    fun startPersonal(player: Player)
    val duration: Int
    val title: String
    val description: String
    val fork: EventContext

}