package me.func.day

import dev.implario.bukkit.event.EventContext
import me.func.user.User

interface Day {

    fun join(user: User)
    fun tick(time: Int): Int
    fun registerHandlers(context: EventContext)
    fun start()
    fun startPersonal(user: User)
    val duration: Int
    val title: String
    val description: Array<String>
    val fork: EventContext

}