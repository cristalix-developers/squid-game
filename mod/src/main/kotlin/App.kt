import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import kotlin.math.abs

const val NAMESPACE = "squidgame"
const val FILE_STORE = "http://51.38.128.132"

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        PlayerLeftManager
        BannerManager
        Winner
        Alert

        val texture = "figure.png"
        loadTextures(
            load(texture, "08832C088F83D" + abs(texture.hashCode())),
        ).thenRun {
            BuyRespawn
        }

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<PlayerListRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }
        registerHandler<VehicleHealthRender> { isCancelled = true }
    }
}