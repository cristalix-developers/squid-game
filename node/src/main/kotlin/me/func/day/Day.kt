package me.func.day

import dev.implario.bukkit.event.EventContext
import me.func.user.User

interface Day {

    fun join(user: User)

    fun duration(): Int

    fun tryFinish(): Boolean

    fun tick(time: Int): Int

    fun registerHandlers(context: EventContext)

    fun unregisterAll()

}