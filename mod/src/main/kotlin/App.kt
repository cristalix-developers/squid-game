import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*

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