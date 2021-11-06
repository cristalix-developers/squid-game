package me.func

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import dev.implario.bukkit.event.WorldEventFilter
import dev.implario.bukkit.event.on
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import implario.games.node.Game
import net.minecraft.server.v1_12_R1.SoundEffects.ca
import org.bukkit.Location
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import java.util.*

class SquidGame(gameId: UUID) : Game(gameId) {

    private val cristalix: Cristalix = Cristalix.connectToCristalix(this, "SQD", "Игра в Кальмара")

    private val map = WorldMeta(MapLoader().load("SquidGame", "game"))

    private var timer: Timer

    override fun acceptPlayer(event: AsyncPlayerPreLoginEvent) = cristalix.acceptPlayer(event)

    override fun getSpawnLocation(playerId: UUID): Location = map.getLabels("spawn")[0]

    init {
        context.appendOption(WorldEventFilter(map.world))
        timer = Timer()

        context.on<PlayerJoinEvent> {
            PreparePlayer(player)
        }

        context.on<BlockRedstoneEvent> { newCurrent = oldCurrent }
        context.on<BlockPlaceEvent> { isCancelled = true }
        context.on<BlockBreakEvent> { isCancelled = true }
        context.on<CraftItemEvent> { isCancelled = true }
        context.on<PlayerInteractEntityEvent> { isCancelled = true }
        context.on<PlayerDropItemEvent> { isCancelled = true }
        context.on<BlockFadeEvent> { isCancelled = true }
        context.on<BlockSpreadEvent> { isCancelled = true }
        context.on<BlockGrowEvent> { isCancelled = true }
        context.on<BlockFromToEvent> { isCancelled = true }
        context.on<HangingBreakByEntityEvent> { isCancelled = true }
        context.on<BlockBurnEvent> { isCancelled = true }
        context.on<EntityExplodeEvent> { isCancelled = true }
        context.on<PlayerArmorStandManipulateEvent> { isCancelled = true }
        context.on<PlayerAdvancementCriterionGrantEvent> { isCancelled = true }
        context.on<PlayerSwapHandItemsEvent> { isCancelled = true }
        context.on<InventoryClickEvent> { isCancelled = true }
        context.on<FoodLevelChangeEvent> { foodLevel = 20 }

        go()
    }

}