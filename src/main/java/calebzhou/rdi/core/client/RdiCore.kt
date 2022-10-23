package calebzhou.rdi.core.client

import calebzhou.rdi.core.client.misc.HwSpec
import calebzhou.rdi.core.client.model.*
import calebzhou.rdi.core.client.util.DialogUtils
import calebzhou.rdi.core.client.util.HttpClient
import icyllis.modernui.textmc.ModernUITextMC
import kotlinx.coroutines.*
import okhttp3.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents
import java.awt.TrayIcon
import java.io.IOException

val logger: Logger = LogManager.getLogger("RDI-Core")
class RdiCore : ModInitializer {
    companion object {
        var currentWeather: RdiWeather? = null
        var currentGeoLocation: RdiGeoLocation? = null
        var currentRdiUser: RdiUser? = null
        var gameReady = false
        private set
    }

    override fun onInitialize(container: ModContainer) {
        ClientLifecycleEvents.READY.register(ClientLifecycleEvents.Ready {
            ModernUITextMC.setupClient()
            gameReady=true
            logger.info("客户端准备好")
        })
        GlobalScope.launch {
            RdiNetworkReceiver.register()
        }
        GlobalScope.launch {
            EventRegister()
        }
    }



}
