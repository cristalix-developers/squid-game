import dev.xdark.clientapi.event.render.*
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.clientapi.readDate
import ru.cristalix.uiengine.UIEngine
import sun.plugin2.message.PluginMessage
import kotlin.math.abs

const val NAMESPACE = "squidgame"
const val FILE_STORE = "http://51.38.128.132"

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        Title
        TimeBar
        MarkerManager
        CorpseManager
        PlayerLeftManager
        BannerManager
        KillBoardManager

        val texture = "figure.png"
        loadTextures(
            load(texture, "08832C088F83D" + abs(texture.hashCode())),
        ).thenRun {
            BuyRespawn
        }

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }
        registerHandler<VehicleHealthRender> { isCancelled = true }

        registerChannel("func:glow") {
            GlowEffect.show(0.45, readInt(), readInt(), readInt(), 0.9)
        }
    }
}