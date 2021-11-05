package me.func.day

import me.func.user.User

interface Day {

    fun join(user: User)

    fun duration(): Int

    fun tryFinish(): Boolean

}