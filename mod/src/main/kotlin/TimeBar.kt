import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object TimeBar {

    private lateinit var line: RectangleElement
    private lateinit var content: TextElement

    init {
        val cooldown = rectangle {
            offset.y += 30
            origin = TOP
            align = TOP
            size = V3(180.0, 5.0, 0.0)
            color = Color(0, 0, 0, 0.62)
            line = +rectangle {
                origin = LEFT
                align = LEFT
                size = V3(180.0, 5.0, 0.0)
                color = Color(42, 102, 189, 1.0)
            }
            content = +text {
                origin = TOP
                align = TOP
                color = WHITE
                shadow = true
                content = "Загрузка..."
                offset.y -= 15
            }
            enabled = false
        }

        var time = 0
        var currentTime = System.currentTimeMillis()

        registerHandler<GameLoop> {
            if (System.currentTimeMillis() - currentTime > 1000) {
                time--
                currentTime = System.currentTimeMillis()
                content.content = content.content.dropLast(5) + (time / 60).toString()
                    .padStart(2, '0') + ":" + (time % 60).toString().padStart(2, '0')
            }
        }

        App::class.mod.registerChannel("func:bar") {
            val text = NetUtil.readUtf8(this) + "00:00"
            time = this.readInt()

            if (time == 0) {
                line.size.x = 0.0
                cooldown.enabled = false
                return@registerChannel
            }

            cooldown.color.red = readInt()
            cooldown.color.green = readInt()
            cooldown.color.blue = readInt()

            cooldown.enabled = true
            content.content = text
            line.animate(time - 0.1) {
                size.x = 0.0
            }
            UIEngine.schedule(time) {
                cooldown.enabled = false
                line.size.x = 180.0
            }
        }

        UIEngine.overlayContext + cooldown
    }
}