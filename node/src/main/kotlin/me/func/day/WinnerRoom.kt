package me.func.day

import dev.implario.bukkit.event.EventContext
import dev.implario.bukkit.event.on
import me.func.Arcade
import me.func.SquidGame
import me.func.battlepass.BattlePassUtil
import me.func.battlepass.quest.QuestType
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.mod.conversation.ModTransfer
import me.func.top.BestUser
import me.func.user.User
import me.func.util.Music
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs

class WinnerRoom(private val game: SquidGame) : Day {

    override lateinit var fork: EventContext
    override val description = "Смотрите на них! Это лучшие из лучших"
    override val title = "Зал славы"

    private val spawn = game.map.getLabel("end")
    private val winner = game.map.getLabels("winner").map {
        it.yaw = it.tag.toFloat()
        it.clone().add(0.5, 0.0, 0.5)
    }

    override fun join(player: Player) {
        player.teleport(spawn)
        player.gameMode = GameMode.ADVENTURE
        player.inventory?.clear()
    }

    override val duration = 1 * 35

    override fun tick(time: Int) = time

    override fun registerHandlers(context: EventContext) {
        this.fork = context

        spawn.yaw = 90f

        game.getUsers().forEach {
            val player = it.player!!
            Anime.timer(player, "До выключения сервера", duration - game.timer.time / 20 - 1)
            Music.LOBBY.play(it)

            BestUser.values().forEach { best -> game.tryUpdateBest(best, it) }

            BattlePassUtil.update(player, QuestType.PLAY, 1, false)
            BattlePassUtil.update(player, QuestType.TIME, 10, false)

            if (!it.spectator) {
                ModTransfer().integer(it.number).send("func:win", player)
                it.stat.wins++
                Arcade.deposit(player.uniqueId, 5)
                Arcade.distributeLootbox(player)
                BattlePassUtil.update(player, QuestType.WIN, 1, false)
            }
        }

        winner.forEachIndexed { index, label ->
            val best = BestUser.values()[index]
            val user = game.best[best]!!

            if (best.get(user) == Double.MAX_VALUE || best.get(user) == 0 || best.get(user) == false)
                return@forEachIndexed

            Npcs.spawn(Npc.builder()
                .location(label)
                .name(user.name)
                .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                .skinUrl("https://webdata.c7x.dev/textures/skin/${user.stat.id}")
                .skinDigest(user.stat.id.toString())
                .type(EntityType.PLAYER)
                .build()
            )

            game.getUsers().forEach {
                ModTransfer()
                    .double(label.x)
                    .double(label.y + 2.5)
                    .double(label.z)
                    .string(String.format(best.title, "" + best.get(user)))
                    .double(label.yaw + 0.0)
                    .send("func:world-banner", it.player)
            }
        }

        fork.on<EntityDamageEvent> { isCancelled = true }
    }

    override fun start() {}

    override fun startPersonal(player: Player) {}

}