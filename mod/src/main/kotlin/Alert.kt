import dev.xdark.feder.NetUtil
import implario.humanize.Humanize
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object Alert {

    private const val VECTOR = 65
    private lateinit var dayTitle: TextElement
    private lateinit var lore: TextElement
    private lateinit var loreBox: RectangleElement
    private val dayTitleBox = rectangle {
        align = TOP
        origin = TOP

        offset.y += -VECTOR

        color = Color(42, 102, 189, 0.86)
        size = V3(160.0, 32.0)

        dayTitle = +text {
            align = CENTER
            origin = CENTER
            scale = V3(1.5, 1.5)
            color = WHITE
            shadow = true
            content = "Название игры"
        }
        loreBox = +rectangle {
            align = BOTTOM
            origin = BOTTOM
            size = V3(180.0, 18.0)
            offset.y += size.y
            color = Color(0, 0, 0, 0.62)

            lore = +text {
                align = CENTER
                origin = CENTER
                scale = V3(0.75, 0.75)
                color = WHITE
                content = "Долгое-долгое описание мини-игры\nухахаа пау-пау"
            }
        }
    }

    init {
        UIEngine.overlayContext + dayTitleBox

        App::class.mod.registerChannel("func:glass-alert") {
            val context = Context3D(V3(readDouble(), readDouble(), readDouble()))

            context.addChild(
                rectangle {
                    rotation = Rotation(Math.PI, 0.0, 1.0, 0.0)
                    size = V3(125.0, 60.0)
                    color = Color(0, 0, 0, 0.62)

                    val count = readInt()

                    addChild(text {
                        align = CENTER
                        origin = CENTER
                        content = "Правильное стекло\nупадет если\n§c$count ${Humanize.plurals("игрок", "игрока", "игроков", count)}\n§fвстанут на него!"
                        offset.z -= 0.1
                    })
                }
            )
            UIEngine.worldContexts.add(context)
        }

        App::class.mod.registerChannel("func:alert") {
            dayTitle.content = NetUtil.readUtf8(this)
            lore.content = NetUtil.readUtf8(this)

            dayTitleBox.animate(0.45, Easings.BACK_BOTH) {
                offset.y += VECTOR + 50
            }

            UIEngine.schedule(8.2) {
                dayTitleBox.animate(0.2, Easings.BACK_IN) {
                    offset.y -= (VECTOR + 50)
                }
            }
        }
    }
}