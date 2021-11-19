package me.func.util

import dev.implario.bukkit.world.Label
import me.func.SquidGame
import me.func.app
import me.func.user.UserData
import ru.cristalix.boards.bukkitapi.Boards
import ru.cristalix.core.account.IAccountService
import java.util.*

object TopCreator {

    fun create(game: SquidGame, location: Label, function: (UserData) -> String) {
        val topArgs = location.tag.split(" ")
        location.add(0.0, 6.1, 0.0)

        val blocks = Boards.newBoard()
        blocks.addColumn("#", 18.0)
        blocks.addColumn("Игрок", 120.0)
        blocks.addColumn(topArgs[0], 40.0)
        blocks.title = "Топ по " + topArgs[1]

        blocks.location = location
        Boards.addBoard(blocks)

        game.context.every(10 * 20) {
            app.kensuke.getLeaderboard(app.userManager, app.statScope, topArgs[2], 15).thenAccept {
                blocks.clearContent()

                for (entry in it) {
                    blocks.addContent(
                        UUID.fromString(entry.data.session.userId),
                        "" + entry.position,
                        entry.data.stat.lastSeenName,
                        "§d" + function(entry.data.stat)
                    )
                }

                blocks.updateContent()
            }
        }
    }
}