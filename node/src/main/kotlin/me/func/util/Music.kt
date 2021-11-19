package me.func.util

import me.func.user.User
import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.display.messages.RadioMessage

enum class Music(private val url: String) {

    KILL_PEAK("https://implario.dev/squidgame/day1.mp3"),
    KILL_MIDDLE("https://implario.dev/squidgame/redlight-middle.mp3"),
    KILL_FAST("https://implario.dev/squidgame/redlight-fast.mp3"),
    SHOOT("https://implario.dev/squidgame/shooting.mp3"),
    LOBBY("https://implario.dev/squidgame/lobby.mp3"),
    ATTENTION("https://implario.dev/squidgame/attention.mp3"),
    FUN("https://implario.dev/squidgame/fun.mp3"), ;

    fun play(user: User) {
        MusicHelper.play(user, url)
    }

}

object MusicHelper {
    fun play(user: User, url: String) {
        stop(user)
        if (user.stat.music)
            IDisplayService.get().sendRadio(user.player.uniqueId, RadioMessage(true, url))
    }

    fun stop(user: User) {
        IDisplayService.get().sendRadio(user.player.uniqueId, RadioMessage(true, "null"))
    }

}