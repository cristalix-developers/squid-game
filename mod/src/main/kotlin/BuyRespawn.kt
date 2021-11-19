import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.resource.ResourceLocation
import implario.humanize.Humanize
import io.netty.buffer.Unpooled
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.ContextGui
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*
import kotlin.math.abs

object BuyRespawn {

    private const val duration = 20
    private var prise = 1
    private var counter = duration * 10
    private val gui = ContextGui()
    private lateinit var respawnText: TextElement
    private var open = false
    private var accepted = false

    private val youDead = text {
        align = CENTER
        origin = CENTER
        offset.y -= 20
        content = "Вы погибли"
        scale = V3(2.3, 2.3)
    }

    private val respawn = rectangle {
        origin = BOTTOM
        align = BOTTOM
        offset.y -= 50
        color = Color(42, 102, 189, 0.78)
        size = V3(135.0, 28.0)

        respawnText = +text {
            color = WHITE
            content =  "Воскресить себя (§c${duration}§f)\n§b0 кристаликов"
            align = CENTER
            origin = CENTER
            scale = V3(1.0, 1.0)
            shadow = true
        }

        onHover {
            if (hovered) {
                animate(0.2) {
                    color.alpha = 0.62
                    scale.y = 1.15
                    scale.x = 1.15
                }
            } else  {
                animate(0.2) {
                    color.alpha = 0.35
                    scale.y = 1.0
                    scale.x = 1.0
                }
            }
        }

        onClick {
            if (!down)
                return@onClick
            if (!accepted) {
                accepted = true
                counter = duration * 10
                color = Color(34, 174, 73, 0.62)
            } else {
                gui.close()
                accepted = false
                color = Color(42, 102, 189, 0.78)
                open = false
                UIEngine.clientApi.clientConnection().sendPayload("func:respawn", Unpooled.buffer())
                GlowEffect.show(0.5, 0, 0, 255, 0.8)
            }
        }
    }

    init {
        gui + respawn
        gui + youDead
        gui + rectangle {
            align = CENTER
            origin = CENTER
            offset = youDead.offset
            textureLocation = ResourceLocation.of(NAMESPACE, "figure.png")
            color = WHITE
            size = V3(270.0, 150.0)
        }
        gui.color = Color(0, 0, 0, 0.82)

        App::class.mod.registerChannel("func:try-respawn") {
            gui.open()
            open = true
            prise = readInt()
        }

        var previous = System.currentTimeMillis()

        registerHandler<GameLoop> {
            if (open && System.currentTimeMillis() - previous >= 100) {
                previous = System.currentTimeMillis()
                counter--
                respawnText.content = if (accepted) "Подтвердить покупку\n(§c${counter / 10.0}§f)"
                else "Воскресить себя (§c${counter / 10.0}§f)\n§b$prise ${
                    Humanize.plurals(
                        "кристалик",
                        "кристалика",
                        "кристаликов",
                        prise
                    )
                }"

                if (counter <= 0) {
                    open = false
                    gui.close()
                }
            }
        }
    }
}