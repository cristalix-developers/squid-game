package me.func

import dev.implario.games5e.sdk.cristalix.ModLoader
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PreparePlayer : (Player) -> (Unit) {

    override fun invoke(player: Player) {
        Bukkit.getScheduler().runTaskLater(app, { ModLoader.manyToOne(player) }, 1)
    }

}

