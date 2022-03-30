package me.func

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import dev.implario.bukkit.event.on
import dev.implario.bukkit.world.Label
import dev.implario.games5e.node.Game
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import me.func.accept.PreparePlayer
import me.func.day.Timer
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.top.BestUser
import me.func.user.User
import me.func.util.TopCreator
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.*
import ru.cristalix.core.transfer.TransferService
import java.util.*
import kotlin.properties.Delegates

data class SquidGameSettings(
    val teams: List<List<UUID>>
)

class SquidGame(gameId: UUID, settings: SquidGameSettings) : Game(gameId) {
    var timer = Timer(this)
    val cristalix: Cristalix = Cristalix.connectToCristalix(this, "SQD", "Игра в Кальмара")
    val map: WorldMeta = MapLoader.load(this, "SquidGame", "prod")
    val best = mutableMapOf<BestUser, User>()

    override fun acceptPlayer(event: AsyncPlayerPreLoginEvent): Boolean {
        if (MAX_PLAYERS > players.size || event.uniqueId.toString() == "307264a1-2c69-11e8-b5ea-1cb72caa35fd")
            return cristalix.acceptPlayer(event)
        return false
    }

    val spawns: MutableList<Label> = map.getLabels("spawn")
    val spawn: Label = map.getLabel("start")
    private val transferService = TransferService(cristalix.client)
    var lite by Delegates.notNull<Boolean>()

    override fun getSpawnLocation(playerId: UUID): Location = spawn

    fun getUsers() = players.mapNotNull { app.getUser(it) }.filter { it.player != null }

    fun getVictims() = getUsers().filter { !it.spectator }

    init {
        cristalix.setRealmInfoBuilder { it.lobbyFallback(Arcade.getLobbyRealm()) }
        cristalix.updateRealmInfo()

        // may delete
        map.world.loadChunk(map.world.getChunkAt(0, 0))
        map.getLabels("circle").forEach {
            if (it.tag != null && it.tag.isNotEmpty()) {
                val pair = it.tag.split(' ')
                it.setYaw(pair[0].toFloat())
                it.setPitch(pair[1].toFloat())
            }
            Workers.CIRCLE.spawn(it)
        }

        TopCreator.create(this, map.getLabel("top-wins")) { " " + it.wins }
        TopCreator.create(this, map.getLabel("top-games")) { " " + it.games }

        timer.runTaskTimer(app, 10, 1)
        timer.activeDay.registerHandlers(context.fork())

        context.on<PlayerQuitEvent> {
            val count = getVictims().size
            getUsers().mapNotNull { it.player }.forEach {
                Anime.killboardMessage(it, "§c${player.displayName} §7покинул игру.")
                ModHelper.playersLeft(it, count)
            }

            val user = app.getUser(player)

            BestUser.values().forEach { tryUpdateBest(it, user) }
        }

        context.on<PlayerJoinEvent> {
            val user = app.getUser(player)
            if (user.player == null)
                user.player = player
            PreparePlayer(player, this@SquidGame)
        }
        context.on<AsyncPlayerChatEvent> { format = "%1\$s → §7%2\$s" }
        context.on<BlockRedstoneEvent> { newCurrent = oldCurrent }
        context.on<BlockPlaceEvent> { isCancelled = true }
        context.on<CraftItemEvent> { isCancelled = true }
        context.on<PlayerInteractEntityEvent> { isCancelled = true }
        context.on<PlayerDropItemEvent> { isCancelled = true }
        context.on<BlockFadeEvent> { isCancelled = true }
        context.on<BlockSpreadEvent> { isCancelled = true }
        context.on<BlockGrowEvent> { isCancelled = true }
        context.on<BlockPhysicsEvent> { isCancelled = true }
        context.on<BlockFromToEvent> { isCancelled = true }
        context.on<HangingBreakByEntityEvent> { isCancelled = true }
        context.on<BlockBurnEvent> { isCancelled = true }
        context.on<EntityExplodeEvent> { isCancelled = true }
        context.on<PlayerArmorStandManipulateEvent> { isCancelled = true }
        context.on<PlayerAdvancementCriterionGrantEvent> { isCancelled = true }
        context.on<PlayerSwapHandItemsEvent> { isCancelled = true }
        context.on<InventoryClickEvent> { isCancelled = true }
        context.on<InventoryOpenEvent> {
            if (inventory.type != InventoryType.PLAYER)
                isCancelled = true
        }
        context.on<FoodLevelChangeEvent> { foodLevel = 20 }
        context.on<EntityDamageEvent> {
            if (entity is Player && (cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                cancelled = true
            }
        }

        context.on<PlayerPickupItemEvent> {
            val nmsItem = CraftItemStack.asNMSCopy(item.itemStack)
            if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("bonus", 8)) {
                if (!Bonus.valueOf(nmsItem.tag.getString("bonus").toUpperCase()).give(player))
                    isCancelled = true
            }
        }

        transferService.transferBatch(settings.teams.flatten(), cristalix.realmId)

        after(1) { lite = players.size < 10 }
    }

    fun tryUpdateBest(scoreType: BestUser, user: User) {
        if (best[scoreType] == null || (best[scoreType] != null && scoreType.compare.compare(
                best[scoreType],
                user
            ) == -1)
        )
            best[scoreType] = user
    }

    fun close() {
        transferService.transferBatch(players.map { it.uniqueId }, Arcade.getLobbyRealm())

        after(10) {
            isTerminated = true
            Bukkit.unloadWorld(map.world, false)
            unregisterAll()
        }
    }
}