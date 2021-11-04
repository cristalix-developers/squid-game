package me.func

import dev.implario.bukkit.platform.Platforms
import dev.implario.games5e.sdk.cristalix.ModLoader
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import implario.games.node.GameCreator
import implario.games.node.GameNode
import implario.games.node.linker.SessionBukkitLinker
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class App: JavaPlugin() {

    override fun onEnable() {

        Platforms.set(PlatformDarkPaper())

        val node = GameNode()
        node.supportedImagePrefixes.add("squid-game")
        node.linker = SessionBukkitLinker.link(node)
        node.gameCreator = GameCreator { gameId, _, _ ->
            SquidGame(gameId)
        }

        node.createGame(UUID.randomUUID(), null, null)

        ModLoader.loadAll("/mods")
    }

}