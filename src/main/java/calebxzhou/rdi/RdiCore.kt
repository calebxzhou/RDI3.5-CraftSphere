package calebxzhou.rdi

import calebxzhou.rdi.consts.RdiConsts
import calebxzhou.rdi.misc.EventRegister
import calebxzhou.rdi.model.RdiGeoLocation
import calebxzhou.rdi.model.RdiUser
import calebxzhou.rdi.model.RdiWeather
import icyllis.modernui.textmc.ModernUITextMC
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.world.level.storage.LevelStorageSource
import org.apache.logging.log4j.LogManager
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created  on 2023-04-07,23:04.
 */
val logger: Logger = RdiCore.logger
class RdiCore : ModInitializer {
    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger("RDI-Core")

        var currentWeather: RdiWeather? = null
        var currentGeoLocation: RdiGeoLocation? = null
        var gameReady = false
            private set
    }

    override fun onInitialize(container: ModContainer) {
        ClientLifecycleEvents.READY.register(ClientLifecycleEvents.Ready { mc->
            ModernUITextMC.setupClient()
            gameReady=true
            logger.info("客户端准备好")

        })
        GlobalScope.launch {
            NetRxer.register()
        }
        GlobalScope.launch {
            EventRegister()
        }
    }



}
