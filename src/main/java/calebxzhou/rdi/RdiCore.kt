package calebxzhou.rdi

import calebxzhou.rdi.misc.EventRegister
import calebxzhou.rdi.mixin.ALootTables
import calebxzhou.rdi.model.RdiGeoLocation
import calebxzhou.rdi.model.RdiWeather
import calebxzhou.rdi.net.NetRxer
import icyllis.modernui.textmc.ModernUITextMC
import net.minecraft.Util
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import org.apache.commons.io.FileUtils
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

/**
 * Created  on 2023-04-07,23:04.
 */
val logger: Logger = RdiCore.logger
class RdiCore : ModInitializer {
    companion object {
        @JvmStatic
        val logger: Logger = LoggerFactory.getLogger("RDI-Core")
        lateinit var logicServer : MinecraftServer
        var currentWeather: RdiWeather? = null
        var currentGeoLocation: RdiGeoLocation? = null
        var gameReady = false
            private set
    }
    override fun onInitialize(container: ModContainer) {

        ClientLifecycleEvents.READY.register(ClientLifecycleEvents.Ready { mc->
            logicServer = RdiLogicServer.spin(RdiLogicServer::start)
            ModernUITextMC.setupClient()
            gameReady=true

            logger.info("客户端准备好")

        })
        ServerLifecycleEvents.READY.register(ServerLifecycleEvents.Ready { mcs ->
            val lt = logicServer.lootTables as ALootTables
            FileUtils.write(File("loot_tables.json"), ALootTables.getGSON().toJson(lt.tables), StandardCharsets.UTF_8)
            logger.info("逻辑服务器准备好")
        })
        thread {
            NetRxer.register()
            EventRegister()
        }


    }




}
