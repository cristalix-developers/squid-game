package me.func.mod

import me.func.SquidGame
import me.func.user.User
import org.bukkit.Location
import org.bukkit.entity.EntityType
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.*

object ModHelper {

    fun glow(user: User, red: Int, blue: Int, green: Int) {
        ModTransfer()
            .integer(red)
            .integer(blue)
            .integer(green)
            .send("func:glow", user)
    }

    fun timer(user: User, text: String, duration: Int) {
        ModTransfer()
            .string(text)
            .integer(duration)
            .send("func:bar", user)
    }

    fun notify(user: User, text: String) {
        ModTransfer()
            .string(text)
            .send("func:notice", user)
    }

    fun notifyAll(game: SquidGame, text: String) {
        game.getUsers().forEach { notify(it, text) }
    }

    fun attention(user: User) {
        ModTransfer()
            .integer(0)
            .send("func:attention", user)
    }

    fun clearAllCorpses(user: User) {
        ModTransfer().integer(0).send("func:corpse-clear", user)
    }

    fun unaryCorpse(to: User, name: String, uuid: UUID, x: Double, y: Double, z: Double) {
        ModTransfer()
            .string(name)
            .string("https://webdata.c7x.dev/textures/skin/$uuid")
            .string(uuid.toString())
            .double(x)
            .double(y + 3)
            .double(z)
            .boolean(true)
            .send("func:corpse-create", to)
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

    fun title(user: User, text: String) {
        ModTransfer()
            .string(text)
            .send("func:title", user)
    }

    fun banner(user: User, uuid: UUID, x: Double, y: Double, z: Double, xSize: Double, ySize: Double, texture: String) {
        ModTransfer()
            .string(uuid.toString())
            .double(x)
            .double(y)
            .double(z)
            .double(xSize)
            .double(ySize)
            .string(texture)
            .send("func:banner", user)
    }

    fun playersLeft(user: User, count: Int) {
        ModTransfer()
            .integer(count)
            .send("func:left", user)
    }
}