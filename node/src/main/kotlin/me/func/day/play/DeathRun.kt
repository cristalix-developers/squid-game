package me.func.day.play

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.SquidGame
import me.func.accept.AcceptLose
import me.func.accept.AcceptRoundWin
import me.func.day.Day
import me.func.mod.ModHelper
import me.func.mod.ModTransfer
import me.func.user.User
import me.func.util.Music
import org.bukkit.event.entity.EntityDamageEvent
import java.util.*

class DeathRun(private val game: SquidGame) : Day {

    override val duration = 1 * 60 + 10
    override val description = "Вам нужно быстро убегать от\nотравленной воды"
    override val title = "Переход"
    override lateinit var fork: EventContext

    private val spawn = game.map.getLabel("day4")
    private val barrierMin = game.map.getLabel("barrier-min")
    private val barrierMax = game.map.getLabel("barrier-max")

    private val checkPoints = game.map.getLabels("safe").map {
        it.yaw = it.tagFloat
        it
    }

    private val deltaY = 95
    private val okayLevel = 1.4
    private val handicap = 4

    override fun join(user: User) {
        user.player?.teleport(spawn)
        ModHelper.banner(
            user,
            UUID.randomUUID(),
            spawn.x - 20,
            spawn.y - 0.6,
            spawn.z - 20,
            1024.0,
            1024.0,
            "water.png"
        )
    }

    override fun tick(time: Int): Int {
        if (time > 0 && time % 10 == 0) {
            game.getUsers().forEach {
                if (underWater(it)) {
                    ModHelper.glow(it, 42, 189, 102)
                    if (!it.spectator && !it.roundWinner)
                        AcceptLose.accept(game, it)
                }
                if (!it.roundWinner && !it.spectator && spawn.y + deltaY - okayLevel * 3 < it.player!!.location.y) {
                    AcceptRoundWin.accept(game, it)
                    it.timeOnDeathRun = (game.timer.time / 2.0).toInt() / 10.0
                }
            }
        }
        return time
    }

    override fun registerHandlers(context: EventContext) {
        fork = context
        fork.on<EntityDamageEvent> { isCancelled = true }
    }

    override fun start() {
        val copy = barrierMin.clone()

        repeat((barrierMax.x - barrierMin.x + 1).toInt()) {
            copy.set(barrierMin.x + it, barrierMin.y, barrierMin.z)
            copy.block.setTypeAndDataFast(0, 0)
            copy.set(barrierMin.x + it, barrierMin.y + 1, barrierMin.z)
            copy.block.setTypeAndDataFast(0, 0)
        }
        game.getUsers().forEach { startPersonal(it) }
    }

    override fun startPersonal(user: User) {
        Music.FUN.play(user)
        user.roundWinner = false
        game.after(handicap * 20L) {
            ModTransfer()
                .integer((duration - handicap / 1.5).toInt())
                .integer(deltaY)
                .send("func:water-move", user)
        }

        if (game.timer.time / 20 > handicap) {
            val highestUserY = game.getVictims().map { it.player!!.location.y }.maxBy { it }
            val nearestSafePoint = if (highestUserY == null) checkPoints.maxBy { it.y }!!
            else checkPoints.filter { it.y > highestUserY }.minBy { it.y }!!

            user.player!!.teleport(nearestSafePoint)
        }
    }

    private fun underWater(user: User): Boolean {
        return game.timer.time / 20 < duration && (game.timer.time / 20 - handicap) * deltaY / duration + spawn.y >= user.player!!.location.y + okayLevel
    }
}