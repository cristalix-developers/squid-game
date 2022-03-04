package me.func.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import me.func.day.detail.Figure
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class User(session: KensukeSession, stat: UserData?) : IBukkitKensukeUser {

    var team: Figure? = null
    var spectator = false
    var roundWinner = true
    var stat: UserData
    var number = 0

    var respawn = 0
    var kills = 0
    var blockBreak = 0
    var drownTime = Double.MAX_VALUE
    var tugs = 0
    var timeOnDeathRun = Double.MAX_VALUE
    var firstDead = false
    var hero = false
    var timeOnGreenLight = Double.MAX_VALUE

    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        player = p0
    }

    override fun getPlayer() = player

    private var session: KensukeSession
    override fun getSession(): KensukeSession {
        return session
    }

    init {
        this.stat = stat ?: UserData(
            UUID.fromString(session.userId),
            0, 0, 0, 0, 0,
            true, 0, 0, 0, 0, null
        )
        this.session = session
    }
}