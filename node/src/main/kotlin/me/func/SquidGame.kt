package me.func

import dev.implario.bukkit.event.WorldEventFilter
import dev.implario.bukkit.event.on
import dev.implario.bukkit.item.item
import dev.implario.bukkit.item.itemBuilder
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import implario.games.node.Game
import implario.games.node.PlayerFilter
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.map.BukkitWorldLoader
import ru.cristalix.core.map.MapService
import java.util.*
import java.util.concurrent.TimeUnit

class SquidGame(gameId: UUID): Game(gameId) {

    private val cristalix: Cristalix = Cristalix.connectToCristalix(this, "SQD", "Игра в Кальмара")

    private val map = WorldMeta(MapLoader().load("SquidGame", "game"))

    override fun acceptPlayer(event: AsyncPlayerPreLoginEvent) = cristalix.acceptPlayer(event)

    override fun getSpawnLocation(playerId: UUID): Location = map.getLabels("spawn")[0]

    init {

        context.appendOption(WorldEventFilter(map.world))

        context.on<PlayerJoinEvent> {
            player.sendMessage("무궁화 꼬찌 피엇 소리다")
        }

        go()

    }

}