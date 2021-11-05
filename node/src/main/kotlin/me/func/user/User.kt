package me.func.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class User(session: KensukeSession, stat: UserData?) : IBukkitKensukeUser {

    private var connection: PlayerConnection? = null
    var stat: UserData

    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        if (p0 != null) {
            player = p0
        }
    }

    override fun getPlayer(): Player? {
        return player
    }

    private var session: KensukeSession
    override fun getSession(): KensukeSession {
        return session
    }

    init {
        this.stat = stat ?: UserData(
            UUID.fromString(session.userId),
            0, 0,
            0, 0, 0, 0,
            true, 0, 0, 0, 0,
            ""
        )
        this.session = session
    }

    fun sendPacket(packet: Packet<*>) {
        if (connection == null)
            connection = (player as CraftPlayer).handle.playerConnection
        connection?.sendPacket(packet)
    }
}