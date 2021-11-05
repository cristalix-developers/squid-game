import dev.xdark.clientapi.event.render.RenderTickPre
import dev.xdark.clientapi.opengl.GlStateManager
import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil.readUtf8
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.UIEngine.clientApi
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.utility.*

object MarkerManager {

    private var holos: MutableMap<String, Context3D> = HashMap()

    init {
        App::class.mod.registerChannel("func:marker-create") {
            val uuid = readUtf8(this)
            val x = readDouble()
            val y = readDouble()
            val z = readDouble()
            val texture = readUtf8(this)
            addHolo(uuid, x, y, z, texture)
        }
        App::class.mod.registerChannel("func:marker-remove") {
            val uuid = readUtf8(this)
            holos.remove(uuid)
        }

        val player = clientApi.minecraft().player
        registerHandler<RenderTickPre> {
            holos.forEach { (_, value) ->
                val yaw =
                    (player.rotationYaw - player.prevRotationYaw) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationYaw
                val pitch =
                    (player.rotationPitch - player.prevRotationPitch) * clientApi.minecraft().timer.renderPartialTicks + player.prevRotationPitch
                value.rotation = Rotation(-yaw * Math.PI / 180 + Math.PI, 0.0, 1.0, 0.0)
                value.children[0].rotation = Rotation(-pitch * Math.PI / 180, 1.0, 0.0, 0.0)
            }
        }
    }

    private fun addHolo(uuid: String, x: Double, y: Double, z: Double, texture: String) {
        val rect = rectangle {
            textureLocation = ResourceLocation.of("minecraft:$texture")
            size = V3(16.0 * 4, 16.0 * 4)
            origin = Relative.CENTER
            color = WHITE
            beforeRender = {
                GlStateManager.disableDepth()
            }
            afterRender = {
                GlStateManager.enableDepth()
            }
        }
        val context = Context3D(V3(x, y, z))
        context + rect
        holos[uuid] = context
        UIEngine.worldContexts + context
    }
}