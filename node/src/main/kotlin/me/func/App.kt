package me.func

import com.google.gson.Gson
import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.DefaultGameNode
import dev.implario.games5e.sdk.cristalix.ModLoader
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import dev.implario.games5e.node.GameCreator
import dev.implario.games5e.node.GameNode
import dev.implario.games5e.node.linker.SessionBukkitLinker
import me.func.mod.ModHelper
import me.func.user.User
import me.func.user.UserData
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.BukkitPlatform
import ru.cristalix.core.CoreApi
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.display.BukkitDisplayService
import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.internal.BukkitInternals
import ru.cristalix.core.internal.FastBukkitInternals
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.permissions.PermissionService
import ru.cristalix.npcs.server.Npcs
import java.util.*

lateinit var app: App

// todo: rewrite with game settings
const val NEED_PLAYERS = 2
const val MAX_PLAYERS = 100
const val RESPAWN_COST = 4

class App : JavaPlugin() {

    val core: CoreApi = CoreApi.get()
    val statScope = Scope("squid-game", UserData::class.java)
    var userManager = BukkitUserManager(
        listOf(statScope),
        { session, context -> User(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    )
    lateinit var kensuke: Kensuke

    override fun onEnable() {
        app = this
        core.init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))
        BukkitInternals.setInstance(FastBukkitInternals())
        core.registerService(IDisplayService::class.java, BukkitDisplayService())
        EntityDataParameters.register()
        Platforms.set(PlatformDarkPaper())
        Npcs.init(this)

        // Games5e
        val node = DefaultGameNode()
        node.supportedImagePrefixes.add("squid-game")
        node.linker = SessionBukkitLinker.link(node)
        val gson = Gson()
        node.gameCreator = GameCreator { gameId, _, settings ->
            SquidGame(gameId, gson.fromJson(settings, SquidGameSettings::class.java))
        }

        val coordinatorClient = CoordinatorClient(node)
//        node.createGame(UUID.randomUUID(), null, null)

        // Kensuke moment
        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = "SQD-TEST-3"
        userManager.isOptional = true

        // Mods
        ModLoader.loadAll("/mods")

        coordinatorClient.enable()

        // Respawn
        Bukkit.getMessenger().registerIncomingPluginChannel(app, "func:respawn") { _, player, _ ->
            node.runningGames.values.filter { it.players.contains(player) }.forEach { game ->
                val user = app.getUser(player)

                if (user.spectator) {
                    val squidGame = game as SquidGame

                    game.cristalix.client.writeAndAwaitResponse<MoneyTransactionResponsePackage>(
                        MoneyTransactionRequestPackage(player.uniqueId, RESPAWN_COST + 5 * user.respawn, true, "Воскрешение на SquidGame")
                    ).thenAccept { responsePackage ->
                        if (responsePackage.errorMessage != null) {
                            player.sendMessage(Formatting.error(responsePackage.errorMessage))
                            return@thenAccept
                        }
                        user.player.inventory.clear()
                        user.spectator = false
                        user.roundWinner = true
                        user.respawn++

                        MinecraftServer.SERVER.postToMainThread {
                            squidGame.timer.activeDay.startPersonal(user)
                            user.player.gameMode = GameMode.ADVENTURE
                        }

                        player.sendMessage(Formatting.fine("Спасибо за поддержку разработчика!"))
                        squidGame.getUsers().forEach {
                            ModHelper.playersLeft(it, game.getVictims().size)
                            ModHelper.notify(it, "§b${user.player.name} §f#${user.number} §7снова вздохнул §b†")
                        }
                    }
                } else {
                    player.sendMessage(Formatting.error("Не удалось вас воскресить!"))
                }
            }
        }
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userManager.getUser(uuid)

}