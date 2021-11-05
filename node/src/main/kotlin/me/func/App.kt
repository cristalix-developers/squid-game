package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.sdk.cristalix.ModLoader
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import implario.games.node.GameCreator
import implario.games.node.GameNode
import implario.games.node.linker.SessionBukkitLinker
import me.func.user.User
import me.func.user.UserData
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*

lateinit var app: App

class App: JavaPlugin() {

    private val statScope = Scope("squid-game", UserData::class.java)
    var userManager = BukkitUserManager(
        listOf(statScope),
        { session, context -> User(session, context.getData(statScope)) },
        { user, context -> context.store(statScope, user.stat) }
    )
    lateinit var kensuke: Kensuke

    override fun onEnable() {
        app = this
        Platforms.set(PlatformDarkPaper())

        // Games5e
        val node = GameNode()
        node.supportedImagePrefixes.add("squid-game")
        node.linker = SessionBukkitLinker.link(node)
        node.gameCreator = GameCreator { gameId, _, _ ->
            SquidGame(gameId)
        }

        node.createGame(UUID.randomUUID(), null, null)

        // Cristalix services
        val core = CoreApi.get()
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        // Realm configuration
        IRealmService.get().currentRealmInfo.groupName = "SquidGame"

        // Kensuke moment
        kensuke = BukkitKensuke.setup(app)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = IRealmService.get().currentRealmInfo.realmId.realmName
        userManager.isOptional = true

        // Mods
        ModLoader.loadAll("/mods")
    }

}