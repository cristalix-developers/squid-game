package me.func.day

import dev.implario.bukkit.event.EventContext
import me.func.user.User

class GreenLight : Day {

    lateinit var context: EventContext

    override fun join(user: User) {
        TODO("Not yet implemented")
    }

    override fun duration() = 100

    override fun tryFinish() = false

    override fun tick(time: Int): Int = time

    override fun registerHandlers(context: EventContext) {
        this.context = context
    }

    override fun unregisterAll() {
        context.unregisterAll()
    }
}