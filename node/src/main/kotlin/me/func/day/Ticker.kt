package me.func.day

import me.func.SquidGame
import org.bukkit.scheduler.BukkitRunnable

class Timer(private val game: SquidGame) : BukkitRunnable() {
    private var time = 0
    private val activeDay = GreenLight

    override fun run() {
        val dayBefore = activeDay
        time = activeDay.tick(time) + 1
        if (dayBefore != activeDay) {
            game.context.unregisterAll()
            activeDay.handlers(game.fork())
            // state change
        }
    }
}