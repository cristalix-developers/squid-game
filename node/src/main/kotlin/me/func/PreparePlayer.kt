package me.func

import dev.implario.games5e.sdk.cristalix.ModLoader
import org.bukkit.entity.Player

object PreparePlayer : (Player) -> (Unit) {

    override fun invoke(player: Player) {
        ModLoader.manyToOne(player)
    }

}

