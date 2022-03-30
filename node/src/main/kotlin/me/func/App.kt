package me.func

import com.google.gson.Gson
import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.node.CoordinatorClient
import dev.implario.games5e.node.DefaultGameNode
import dev.implario.games5e.node.GameCreator
import dev.implario.games5e.node.linker.SessionBukkitLinker
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.battlepass.quest.ArcadeType
import me.func.mod.Anime
import me.func.mod.ModHelper
import me.func.mod.conversation.ModLoader
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
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import ru.cristalix.npcs.server.Npcs
import java.util.*

lateinit var app: App

const val NEED_PLAYERS = 3
const val MAX_PLAYERS = 150
const val RESPAWN_COST = 3
const val MINIMUM_PLAYERS_RESPAWN = 8

class App : JavaPlugin() {

    private val core: CoreApi = CoreApi.get()
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

        val gson = Gson()

        // Games5e
        val node = DefaultGameNode()
        node.supportedImagePrefixes.add("squid-game")
        node.linker = SessionBukkitLinker.link(node)
        node.gameCreator = GameCreator { gameId, _, settings ->
             SquidGame(gameId, gson.fromJson(settings, SquidGameSettings::class.java))
        }
        val coordinatorClient = CoordinatorClient(node)
        coordinatorClient.enable()

        // Kensuke moment
        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = "SQD-TEST-3"
        userManager.isOptional = true

        // Arcade
        Arcade.start(ArcadeType.SQD)

        // Mods
        ModLoader.loadAll("mods")

        // Respawn
        Bukkit.getMessenger().registerIncomingPluginChannel(app, "func:respawn") { _, player, _ ->
            node.runningGames.values.filter { it.players.contains(player) }.forEach { game ->
                val user = app.getUser(player)

                if (user.spectator) {
                    val squidGame = game as SquidGame

                    if (squidGame.getVictims().size < MINIMUM_PLAYERS_RESPAWN) {
                        Anime.killboardMessage(player, Formatting.error("Воскрешение недоступно!"))
                        return@forEach
                    }

                    game.cristalix.client.writeAndAwaitResponse<MoneyTransactionResponsePackage>(
                        MoneyTransactionRequestPackage(player.uniqueId, RESPAWN_COST + 2 * user.respawn, true, "Воскрешение на SquidGame")
                    ).thenAccept { responsePackage ->
                        if (responsePackage.errorMessage != null) {
                            player.sendMessage(Formatting.error(responsePackage.errorMessage))
                            return@thenAccept
                        }
                        user.player?.inventory?.clear()
                        user.spectator = false
                        user.roundWinner = true
                        user.respawn++

                        MinecraftServer.SERVER.postToMainThread {
                            squidGame.timer.activeDay.startPersonal(player)
                            player.gameMode = GameMode.ADVENTURE
                        }

                        player.sendMessage(Formatting.fine("Спасибо за поддержку разработчика!"))
                        squidGame.getUsers().mapNotNull { it.player }.forEach {
                            ModHelper.playersLeft(it, game.getVictims().size)
                            Anime.killboardMessage(it, "§b${player.name} §f#${user.number} §7снова вздохнул §b†")
                        }
                    }
                } else {
                    Anime.killboardMessage(player, Formatting.error("Не удалось вас воскресить!"))
                }
            }
        }
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userManager.getUser(uuid)

}