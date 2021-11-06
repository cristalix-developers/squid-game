package me.func.day

import me.func.SquidGame
import me.func.app
import org.bukkit.scheduler.BukkitRunnable

class Timer(private val game: SquidGame) : BukkitRunnable() {
    private var time = 0
    private val activeDay = GreenLight

    override fun run() {
        val dayBefore = activeDay
        activeDay.tick(time)
        time = activeDay.tick(time) + 1
        if (dayBefore != activeDay) {
            game.context.unregisterAll()
            activeDay.handlers(game.fork())
            game.players.forEach { activeDay.join(app.getUser(it.player)) }
            // state change
        }
    }
}