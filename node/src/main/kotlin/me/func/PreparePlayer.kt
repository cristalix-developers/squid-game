package me.func

import dev.implario.bukkit.event.EventContext
import dev.implario.games5e.sdk.cristalix.ModLoader
import org.bukkit.entity.Player

object PreparePlayer : (Player, EventContext) -> (Unit) {

    override fun invoke(player: Player, context: EventContext) {
        context.after(1) { ModLoader.manyToOne(player) }
    }

}

