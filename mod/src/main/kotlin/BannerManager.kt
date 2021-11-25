import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.Rotation
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.WHITE
import ru.cristalix.uiengine.utility.rectangle
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.abs
import kotlin.math.sin

object BannerManager {

    private var banner: MutableMap<String, Context3D> = HashMap()

    init {
        var counter = 0
        var previousSeconds = System.currentTimeMillis()
        var animation = false

        registerHandler<GameLoop> {
            if (animation)
                return@registerHandler
            val current = System.currentTimeMillis()

            if (current - previousSeconds > 5) {
                counter++
                previousSeconds = current
                banner.forEach { (_, u) -> u.children.forEach { it.offset.y += sin(Math.toRadians(counter.toDouble())) / 50 } }
            }

        }

        App::class.mod.registerChannel("func:banner") {
            val uuid = NetUtil.readUtf8(this)
            val x = readDouble()
            val y = readDouble()
            val z = readDouble()

            val xSize = readDouble()
            val ySize = readDouble()

            val texture = NetUtil.readUtf8(this)

            // Загрузка фотографий
            loadTextures(
                load(texture, "08832C088F83D" + abs(texture.hashCode())),
            ).thenRun {
                val context = Context3D(V3(x, y, z))

                val rect = rectangle {
                    textureLocation = ResourceLocation.of(NAMESPACE, texture)
                    size = V3(xSize, ySize, 0.1)
                    color = WHITE
                    rotation = Rotation(-Math.PI/2, 1.0, 0.0, 0.0)
                }

                context.addChild(rect)
                banner[uuid] = context
                UIEngine.worldContexts.add(context)
            }
        }
        App::class.mod.registerChannel("func:banner-clear") {
            banner.forEach { (_, it) -> UIEngine.worldContexts.remove(it) }
            banner.clear()
        }

        App::class.mod.registerChannel("func:water-move") {
            animation = true
            val duration = readInt()
            banner.forEach { (_, it) -> it.animate(duration) {
                offset.y += readInt()
            } }
            UIEngine.schedule(duration + 1) {
                animation = false
            }
        }
    }
}