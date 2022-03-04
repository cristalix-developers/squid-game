import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.ContextGui
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object Winner {

    private val gui = ContextGui()
    private lateinit var buttonText: TextElement

    private val youWon = text {
        align = CENTER
        origin = CENTER
        offset.y -= 20
        scale = V3(2.0, 2.0)
    }

    private val back = rectangle {
        origin = BOTTOM
        align = BOTTOM
        offset.y -= 50
        color = Color(42, 102, 189, 0.78)
        size = V3(135.0, 28.0)

        buttonText = +text {
            color = WHITE
            content = "Вернуться в лобби"
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
            } else {
                animate(0.2) {
                    color.alpha = 0.35
                    scale.y = 1.0
                    scale.x = 1.0
                }
            }
        }

        onClick {
            UIEngine.clientApi.chat().sendChatMessage("/hub")
        }
    }

    init {
        gui + back
        gui + youWon
        gui + rectangle {
            align = CENTER
            origin = CENTER
            offset = youWon.offset
            textureLocation = ResourceLocation.of(NAMESPACE, "figure.png")
            color = WHITE
            size = V3(270.0, 150.0)
        }
        gui.color = Color(0, 0, 0, 0.82)

        App::class.mod.registerChannel("func:win") {
            youWon.content = "Вы победили!\n§b${UIEngine.clientApi.minecraft().player.name} #${readInt()}"
            gui.open()
        }

        App::class.mod.registerChannel("func:world-banner") {
            val context = Context3D(V3(readDouble() - 0.5, readDouble() - 0.4, readDouble() - 0.5))

            val text = NetUtil.readUtf8(this)
            val rotation = Math.toRadians(-readDouble())

            context.addChild(rectangle {
                color = Color(0, 0, 0, 0.62)
                size = V3(45.0, 25.0, 0.0)

                +text {
                    offset.z -= 0.1
                    color = WHITE
                    align = CENTER
                    origin = CENTER
                    scale = V3(0.5, 0.5)
                    content = text
                }
            })

            context.rotation = Rotation(rotation, 0.0, 1.0, 0.0)

            UIEngine.worldContexts.add(context)
        }
    }
}
