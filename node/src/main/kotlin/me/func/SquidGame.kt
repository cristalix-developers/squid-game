package me.func

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import dev.implario.bukkit.event.WorldEventFilter
import dev.implario.bukkit.event.on
import dev.implario.bukkit.world.Label
import dev.implario.games5e.sdk.cristalix.Cristalix
import dev.implario.games5e.sdk.cristalix.MapLoader
import dev.implario.games5e.sdk.cristalix.WorldMeta
import implario.games.node.Game
import me.func.day.Day
import me.func.day.Timer
import me.func.day.misc.Bonus
import me.func.day.misc.Workers
import me.func.day.play.*
import me.func.mod.ModHelper
import me.func.util.TopCreator
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
import org.bukkit.event.player.*
import java.util.*


class SquidGame(gameId: UUID) : Game(gameId) {
    var timer = Timer(this)

    private val cristalix: Cristalix = Cristalix.connectToCristalix(this, "TEST", "Игра в Кальмара")

    val map = WorldMeta(MapLoader().load("SquidGame", "prod"))

    override fun acceptPlayer(event: AsyncPlayerPreLoginEvent): Boolean {
        if (MAX_PLAYERS > players.size || event.uniqueId.toString() == "307264a1-2c69-11e8-b5ea-1cb72caa35fd")
            return cristalix.acceptPlayer(event)
        return false
    }

    val spawns: MutableList<Label> = map.getLabels("spawn")
    val spawn: Label = map.getLabel("start")

    override fun getSpawnLocation(playerId: UUID): Location = spawn

    fun getUsers() = players.mapNotNull { app.getUser(it) }

    fun getVictims() = getUsers().filter { !it.spectator }

    init {
        context.appendOption(WorldEventFilter(map.world))

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

        TopCreator.create(this, map.getLabel("top-wins")) { it.wins.toString() }
        TopCreator.create(this, map.getLabel("top-games")) { it.games.toString() }

        timer.runTaskTimer(app, 10, 1)
        timer.activeDay.registerHandlers(context.fork())

        context.on<PlayerJoinEvent> {
            PreparePlayer(app.getUser(player), this@SquidGame)
        }

        context.on<PlayerQuitEvent> {
            val count = getVictims().size
            getUsers().forEach {
                ModHelper.notify(it, "§c${player.name} §7покинул игру.")
                ModHelper.playersLeft(it, count)
            }
        }

        context.on<AsyncPlayerChatEvent> {
            val user = app.getUser(player)
            format = "%1\$s → §7%2\$s"
        }

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
        context.on<FoodLevelChangeEvent> { foodLevel = 20 }
        context.on<EntityDamageEvent> {
            if (entity is Player && (cause == EntityDamageEvent.DamageCause.FALL || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                cancelled = true
            }
        }

        context.on<PlayerPickupItemEvent> {
            val nmsItem = CraftItemStack.asNMSCopy(item.itemStack)
            if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("bonus", 8)) {
                if (!Bonus.valueOf(nmsItem.tag.getString("bonus").toUpperCase()).give(app.getUser(player)))
                    isCancelled = true
            }
        }
    }

    fun close() {
        // todo: realize that
    }
}