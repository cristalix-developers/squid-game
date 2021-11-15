package me.func.day

import me.func.AcceptLose
import me.func.NEED_PLAYERS
import me.func.SquidGame
import me.func.day.play.*
import me.func.mod.ModHelper
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

    private lateinit var days: MutableList<Day>

    override fun run() {
        val tick = activeDay.tick(time)

        if (stop)
            return

        time = tick + 1

        if (game.getUsers().size < NEED_PLAYERS && activeDay is WaitingGame && time / 20 > activeDay.duration) {
            time = 0
            game.getVictims().forEach {
                ModHelper.timer(it, "Ожидание игроков", activeDay.duration - game.timer.time / 20)
            }
            return
        }

        if (time / 20 > activeDay.duration) {
            if (activeDay is WaitingGame) {
                days = mutableListOf(
                    BreakForm(game),
                    DeathRun(game),
                    Glasses(game),
                    GreenLight(game),
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

        // Загрузка следующего дня
        //game.games.remove(activeDay)
        dayBefore = activeDay
        activeDay = days.random()
        days.remove(activeDay)

        game.getUsers().forEach { user ->
            // Паранойя
            user.player.inventory.clear()
            user.player.openInventory.topInventory.clear()
            user.player.itemOnCursor = null
            user.player.sendMessage(activeDay.description)
            user.player.activePotionEffects.forEach { user.player.removePotionEffect(it.type) }
            ModHelper.clearAllCorpses(user)
            MusicHelper.stop(user)

            user.roundWinner = false

            // Смена дня
            activeDay.join(user)
        }

        dayBefore.fork.unregisterAll()
        activeDay.registerHandlers(game.fork())

        stop = true

        activeDay.fork.after(1 * 20) {
            game.getUsers().forEach {
                ModHelper.title(it, "§b§l#N\n\n§b${activeDay.title}")
                ModHelper.timer(it, "Подготовка", PLAYER_PREPARE_DURATION)
                ModHelper.notify(it, " §f► §bПодготовка игроков... §f◄ ")
            }
        }

        activeDay.fork.after((PLAYER_PREPARE_DURATION - 3 + 2) * 20L) {
            game.getUsers().forEach { Music.ATTENTION.play(it) }
        }

        activeDay.fork.after(PLAYER_PREPARE_DURATION * 20L + 40) {
            activeDay.start()
            game.getUsers().forEach {
                ModHelper.timer(it, "До окончания испытания", activeDay.duration)
                ModHelper.notify(it, " §f► §aИспытание началось! §f◄ ")
            }
            stop = false
        }

        activeDay.fork.after((activeDay.duration + PLAYER_PREPARE_DURATION) * 20L + 50) {

            // Удаляем все с прошло испытания
            game.map.world.livingEntities.filter { it.hasMetadata("trash") }.forEach { it.remove() }

            game.getUsers().forEach {
                ModHelper.timer(it, "Смена испытания", DAY_CHANGE_DURATION)
                ModHelper.notify(it, " §f► §eОжидайте новой игры §f◄ ")
                if (!it.spectator) {
                    it.player.gameMode = GameMode.ADVENTURE
                    if (!it.roundWinner)
                        AcceptLose.accept(game, it)
                }
            }
            stop = true
        }

        activeDay.fork.after((activeDay.duration + DAY_CHANGE_DURATION + PLAYER_PREPARE_DURATION) * 20L + 80) {
            stop = false
        }
    }
}