package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.sdk.cristalix.ModLoader
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import implario.games.node.GameCreator
import implario.games.node.GameNode
import implario.games.node.linker.SessionBukkitLinker
import me.func.mod.ModTransfer
import me.func.user.User
import me.func.user.UserData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.BukkitPlatform
import ru.cristalix.core.CoreApi
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.display.BukkitDisplayService
import ru.cristalix.core.display.DisplayService
import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.internal.BukkitInternals
import ru.cristalix.core.internal.FastBukkitInternals
import ru.cristalix.npcs.server.Npcs
import java.util.*

lateinit var app: App
// todo: rewrite with game settings
const val NEED_PLAYERS = 2
const val MAX_PLAYERS = 100

class App : JavaPlugin() {

    val statScope = Scope("squid-game", UserData::class.java)
    var userManager = BukkitUserManager(
        listOf(statScope),
        { session, context -> User(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    )
    lateinit var kensuke: Kensuke

    override fun onEnable() {
        app = this
        CoreApi.get().init(BukkitPlatform(Bukkit.getServer(), Bukkit.getLogger(), this))
        BukkitInternals.setInstance(FastBukkitInternals())
        CoreApi.get().registerService(IDisplayService::class.java, BukkitDisplayService())
        EntityDataParameters.register()
        Platforms.set(PlatformDarkPaper())
        Npcs.init(this)

        // Games5e
        val node = GameNode()
        node.supportedImagePrefixes.add("squid-game")
        node.linker = SessionBukkitLinker.link(node)
        node.gameCreator = GameCreator { gameId, _, _ ->
            SquidGame(gameId)
        }
        node.createGame(UUID.randomUUID(), null, null)

        // Kensuke moment
        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = "SQD-TEST-3"
        userManager.isOptional = true

        // Mods
        ModLoader.loadAll("/mods")
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userManager.getUser(uuid)

}