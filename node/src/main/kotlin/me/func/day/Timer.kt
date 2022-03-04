package me.func.day

import me.func.accept.AcceptLose
import me.func.NEED_PLAYERS
import me.func.SquidGame
import me.func.day.play.*
import me.func.day.play.figure.BreakForm
import me.func.day.play.girl.GreenLight
import me.func.day.play.glass.Glasses
import me.func.day.play.night.Night
import me.func.day.play.tug.TugOfWar
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.mod.conversation.ModTransfer
import me.func.util.Music
import me.func.util.MusicHelper
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

const val PLAYER_PREPARE_DURATION = 14
const val DAY_CHANGE_DURATION = 6

class Timer(private val game: SquidGame) : BukkitRunnable() {
    var time = 0
    var activeDay: Day = WaitingGame(game)
    private lateinit var dayBefore: Day
    var stop = false
    var dayIndex = 0

    private lateinit var days: MutableList<Day>

    override fun run() {
        val tick = activeDay.tick(time)

        if (stop)
            return

        time = tick + 1

        if (game.getUsers().size < NEED_PLAYERS && activeDay is WaitingGame && time / 20 > activeDay.duration) {
            time = 0
            game.getVictims().mapNotNull { it.player }.forEach {
                Anime.timer(it, "Ожидание игроков", activeDay.duration - game.timer.time / 20)
            }
            return
        }

        if (time / 20 > activeDay.duration) {
            if (activeDay is WaitingGame) {
                days = mutableListOf(GreenLight(game))
            } else if (activeDay is GreenLight) {
                days = mutableListOf(
                    BreakForm(game),
                    DeathRun(game),
                    Glasses(game),
                    Night(game),
                    TntRun(game),
                    TugOfWar(game)
                )
            }
            changeDay()
        }
    }

    private fun changeDay() {
        time = 0

        if (days.isEmpty() || game.getVictims().size < 5) {
            if (activeDay !is WinnerRoom) {
                activeDay = WinnerRoom(game)
                activeDay.registerHandlers(game.fork())
                game.getUsers().mapNotNull { it.player }.forEach { activeDay.join(it) }
            } else {
                game.close()
            }
            return
        }

        // Загрузка следующего дня
        dayBefore = activeDay
        activeDay = days.random()
        days.remove(activeDay)
        dayIndex++

        game.after(3 * 20) {
            game.getUsers().mapNotNull { it.player }.forEach {
                ModTransfer()
                    .string(activeDay.title)
                    .string(activeDay.description)
                    .send("func:alert", it)
            }
        }

        game.getUsers().forEach { user ->
            user.player!!.inventory.clear()
            user.player!!.openInventory.topInventory.clear()
            user.player!!.itemOnCursor = null
            user.player!!.activePotionEffects.forEach { user.player!!.removePotionEffect(it.type) }
            MusicHelper.stop(user)

            user.roundWinner = false

            // Смена дня
            activeDay.join(user.player!!)
        }

        dayBefore.fork.unregisterAll()
        activeDay.registerHandlers(game.fork())

        stop = true

        activeDay.fork.after(1 * 20) {
            game.getUsers().mapNotNull { it.player }.forEach {
                Anime.timer(it, "Подготовка", PLAYER_PREPARE_DURATION)
                Anime.killboardMessage(it, " §f► §bПодготовка игроков... §f◄ ")
            }
        }

        activeDay.fork.after(((PLAYER_PREPARE_DURATION - 3 + 1.5) * 20L).toLong()) {
            game.getUsers().forEach {
                Music.ATTENTION.play(it)
                Anime.counting321(it.player!!)
            }
        }

        activeDay.fork.after(PLAYER_PREPARE_DURATION * 20L + 40) {
            activeDay.start()
            game.getUsers().mapNotNull { it.player }.forEach {
                Anime.timer(it, "До окончания испытания", activeDay.duration)
                Anime.killboardMessage(it, " §f► §aИспытание началось! §f◄ ")
            }
            stop = false
        }

        activeDay.fork.after((activeDay.duration + PLAYER_PREPARE_DURATION) * 20L + 50) {

            // Удаляем все с прошло испытания
            game.map.world.livingEntities.filter { it.hasMetadata("trash") }.forEach { it.remove() }

            game.getUsers().forEach {
                Anime.timer(it.player!!, "Смена испытания", DAY_CHANGE_DURATION)
                Anime.killboardMessage(it.player!!, " §f► §eОжидайте новой игры §f◄ ")
                if (!it.spectator) {
                    it.player?.gameMode = GameMode.ADVENTURE
                    if (!it.roundWinner)
                        AcceptLose.accept(game, it.player!!)
                }
            }
            stop = true
        }

        activeDay.fork.after((activeDay.duration + DAY_CHANGE_DURATION + PLAYER_PREPARE_DURATION) * 20L + 80) {
            stop = false
        }
    }
}