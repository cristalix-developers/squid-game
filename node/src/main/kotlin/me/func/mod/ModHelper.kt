package me.func.mod

import me.func.mod.conversation.ModTransfer
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.*

object ModHelper {

    fun timer(player: Player, text: String, duration: Int) {
        ModTransfer()
            .string(text)
            .integer(duration)
            .send("func:bar", player)
    }

    fun npc(location: Location, uuid: UUID, name: String?, command: String?) {
        Npcs.spawn(
            Npc.builder()
                .location(location)
                .name(name ?: "")
                .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                .skinUrl("https://webdata.c7x.dev/textures/skin/$uuid")
                .skinDigest(uuid.toString())
                .type(EntityType.PLAYER)
                .onClick { if (command != null) it.performCommand(command) }
                .build()
        )
    }

    fun npc(location: Location, uuid: UUID) {
        npc(location, uuid, null, null)
    }

    fun banner(player: Player, uuid: UUID, x: Double, y: Double, z: Double, xSize: Double, ySize: Double, texture: String) {
        ModTransfer()
            .string(uuid.toString())
            .double(x)
            .double(y)
            .double(z)
            .double(xSize)
            .double(ySize)
            .string(texture)
            .send("func:banner", player)
    }

    fun playersLeft(player: Player, count: Int) {
        ModTransfer()
            .integer(count)
            .send("func:left", player)
    }
}