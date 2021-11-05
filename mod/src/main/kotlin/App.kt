import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine

const val NAMESPACE = "squid-game"
const val FILE_STORE = "http://51.38.128.132"

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        TimeBar
        MarkerManager

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }
        registerHandler<VehicleHealthRender> { isCancelled = true }

        registerChannel("func:glow") {
            GlowEffect.show(0.3, readInt(), readInt(), readInt(), 0.7)
        }
    }
}