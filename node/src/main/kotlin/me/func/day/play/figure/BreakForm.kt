package me.func.day.play.figure

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import dev.implario.bukkit.item.itemBuilder
import me.func.SquidGame
import me.func.accept.AcceptRoundWin
import me.func.app
import me.func.day.Day
import me.func.day.detail.Figure
import me.func.mod.Anime
import me.func.user.User
import me.func.util.Music
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent

class BreakForm(private val game: SquidGame) : Day {

    override val duration = 1 * 25
    override val description = "Киркой вырежьте фигуру, тогда\nвы сможете спастись"
    override val title = "Вырезание фигур"
    override lateinit var fork: EventContext

    private val spawn = game.map.getLabel("day7")
    private val alert = game.map.getLabel("alert")
    private val teams = Figure.values().map {
        BreakFormTeam(
            it,
            game.map.getLabel("team-" + it.name.toLowerCase()),
            game.map.getLabel(it.name.toLowerCase() + "-min"),
            game.map.getLabel(it.name.toLowerCase() + "-max"),
            mutableListOf()
        )
    }
    private val cookies = Figure.values().toList().associateWith { figure ->
        game.map.getLabels("cook-" + figure.name.toLowerCase()).map { Cookie(figure, it) }
    }

    private val pickaxe = itemBuilder {
        type = Material.GOLD_PICKAXE
        enchant(Enchantment.DIG_SPEED, 6)
        text("§bКирка")
        nbt("Unbreakable", 1)
    }.build()

    init {
        spawn.yaw = 180f
    }

    override fun join(player: Player) {
        player.teleport(spawn)
        Anime.title(player, "§bВыберите фигуру")
    }

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        fork = context

        fork.on<EntityDamageEvent> { isCancelled = true }
        fork.on<BlockBreakEvent> {
            app.getUser(player)?.let { user ->
                if (block.type != Material.STAINED_CLAY || user.roundWinner) {
                    isCancelled = true
                    return@on
                }
                val team = user.team
                val cookie =
                    cookies[team]?.filter { it.hasOwner }?.minByOrNull { it.spawn.distanceSquared(block.location) }

                if (cookie != null) {
                    cookie.acceptBlockBreak(game, player, block.location)
                    block.setTypeAndDataFast(0, 0)
                    if (cookie.done()) {
                        AcceptRoundWin.accept(game, player)
                        user.drownTime = (game.timer.time / 2.0).toInt() / 10.0
                    }
                }
            }
        }

        fork.on<PlayerMoveEvent> {
            app.getUser(player)?.let { user ->
                if (game.timer.stop && (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ)) {
                    if (user.spectator)
                        return@on

                    val nearestTeam = teams.minByOrNull { it.point.distanceSquared(to) }!!

                    if (to.z > nearestTeam.min.z || nearestTeam.users.contains(user))
                        return@on

                    nearestTeam.users.add(user)
                    user.team = nearestTeam.team

                    if (nearestTeam.users.size > game.getVictims().size / 4.0) {
                        val minCopy = nearestTeam.min.clone()

                        repeat(nearestTeam.max.blockY - nearestTeam.min.blockY + 1) { y ->
                            repeat(nearestTeam.max.blockX - nearestTeam.min.blockX + 1) { x ->
                                minCopy.set(nearestTeam.min.x + x, nearestTeam.min.y + y, nearestTeam.min.z)
                                minCopy.block.setTypeAndDataFast(95, 5)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun start() = game.getUsers().mapNotNull { it.player }.forEach { startPersonal(it) }

    override fun startPersonal(player: Player) {
        app.getUser(player)?.let { user ->
            Music.FUN.play(user)

            if (!user.spectator) {
                if (user.team == null)
                    user.team = teams.minByOrNull { it.users.size }!!.team
                game.after(5) { user.player?.gameMode = GameMode.SURVIVAL }
                player.inventory?.addItem(pickaxe)

                val cookie = cookies[user.team]!!.first { !it.hasOwner }
                cookie.hasOwner = true
                player.teleport(cookie.spawn)
            } else {
                player.teleport(cookies[Figure.values().random()]!![0].spawn)
            }
        }
    }
}
