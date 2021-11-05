import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.clientapi.resource.ResourceLocation
import org.lwjgl.opengl.GL11
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.Color
import ru.cristalix.uiengine.utility.rectangle

object GlowEffect {

    var added = false
    private val vignette = rectangle {
        size = UIEngine.overlayContext.size

        color = Color(0, 0, 0, 0.0)

        textureLocation = ResourceLocation.of("minecraft", "textures/misc/vignette.png")
        beforeRender = {
            GlStateManager.disableDepth()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
        }
        afterRender = {
            GlStateManager.enableDepth()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        }
    }

    fun show(duration: Double, red: Int, blue: Int, green: Int, power: Double) {
        if (!added) {
            UIEngine.overlayContext.addChild(vignette)
            added = true
        }

        vignette.color.red = red
        vignette.color.blue = blue
        vignette.color.green = green

        vignette.animate(duration) {
            vignette.color.alpha = power
        }
        UIEngine.schedule(duration) {
            vignette.animate(duration) {
                vignette.color.alpha = 0.0
            }
        }
    }
}