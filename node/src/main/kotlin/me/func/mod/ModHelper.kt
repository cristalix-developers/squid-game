package me.func.mod

import me.func.user.User
import org.bukkit.Color

object ModHelper {

    fun glow(user: User, red: Int, blue: Int, green: Int) {
        ModTransfer()
            .integer(red)
            .integer(blue)
            .integer(green)
            .send("func:glow", user)
    }

    fun timer(user: User, text: String, duration: Int, color: Color) {
        ModTransfer()
            .string(text)
            .integer(duration)
            .integer(color.getRed())
            .integer(color.getBlue())
            .integer(color.getGreen())
            .send("func:bar", user)
    }
}