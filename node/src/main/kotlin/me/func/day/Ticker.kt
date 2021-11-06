package me.func.day

import me.func.SquidGame
import me.func.app
import me.func.mod.ModHelper
import org.bukkit.Color
import org.bukkit.scheduler.BukkitRunnable

class Timer(private val game: SquidGame) : BukkitRunnable() {
    private var time = 0
    private val activeDay = GreenLight()

    override fun run() {
        val dayBefore = activeDay
        activeDay.tick(time)
        time = activeDay.tick(time) + 1
        if (dayBefore != activeDay) {
            activeDay.registerHandlers(game.fork())
            dayBefore.unregisterAll()
            game.players.map { app.getUser(it.player) }.forEach {
                activeDay.join(it)
                ModHelper.timer(it, "Начало следующего испытания", activeDay.duration(), Color.AQUA)
            }
            // state change
        }
    }
}