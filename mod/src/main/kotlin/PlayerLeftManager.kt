import dev.xdark.clientapi.opengl.GlStateManager
import org.lwjgl.opengl.GL11
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.AbstractElement
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

const val DIGIT_WIDTH = 8
const val DIGIT_HEIGHT = 10

class OdometerDigit : RectangleElement() {
    var value: Int = 0

    private val text = text {
        content = "§l0"
        color = hex("95b297")
        lastParent = this
        properties[Property.ParentSizeX] = DIGIT_WIDTH.toDouble()
        align = TOP
        origin = TOP
        offset.x += 1.5
    }

    init {
        afterRender {
            val top = (offset.y / DIGIT_HEIGHT) % 10
            val bottom = (top + 1) % 10

            GlStateManager.depthFunc(GL11.GL_GREATER)
            GlStateManager.translate(0f, (-(offset.y / DIGIT_HEIGHT).toInt() * DIGIT_HEIGHT).toFloat(), 0f)
            text.content = "§l" + top.toInt().toString()
            text.transformAndRender()
            GlStateManager.translate(0f, (-DIGIT_HEIGHT).toFloat(), 0f)
            text.content = "§l" + bottom.toInt().toString()
            text.transformAndRender()
            GlStateManager.depthFunc(GL11.GL_LEQUAL)
        }
        size.x = 10.0
        size.y = 10.0
        offset.z = -1.0
    }
}

operator fun <T: AbstractElement> T.invoke(setup: T.() -> Unit): T = also(setup)

class Odometer: RectangleElement() {
    init {
        size.x = DIGIT_WIDTH * 3.0 + 2
        size.y = DIGIT_WIDTH * 1.0 + 2
        color = BLACK
        offset.z = 1.0
    }

    private val ones = +OdometerDigit()() { offset.x = DIGIT_WIDTH * 2.0 }
    private val tens = +OdometerDigit()() { offset.x = DIGIT_WIDTH * 1.0 }
    private val hundreds = +OdometerDigit()

    var value: Int = 0
        set(value) {
            field = value
            animate(3.0, Easings.QUINT_BOTH) {
                ones.offset.y = (value * DIGIT_HEIGHT).toDouble()
                tens.offset.y = (value / 10 * DIGIT_HEIGHT).toDouble()
                hundreds.offset.y = (value / 100 * DIGIT_HEIGHT).toDouble()
            }
        }
}

object PlayerLeftManager {

    private val odometer = UIEngine.overlayContext + Odometer()() {
        align = TOP
        origin = TOP
        scale = V3(1.0, 1.0, 1.0)
        color = BLACK
        enabled = false
    }


    init {
        App::class.mod.registerChannel("func:left") {
            val size = readInt()
            if (size < 0) {
                odometer.enabled = false
                return@registerChannel
            } else {
                odometer.enabled = true
                odometer.value = size
            }
        }
    }
}