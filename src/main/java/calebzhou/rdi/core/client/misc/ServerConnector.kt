package calebzhou.rdi.core.client.misc

import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.loader.LoadProgressRecorder
import calebzhou.rdi.core.client.screen.RdiTitleScreen
import calebzhou.rdi.core.client.util.ThreadPool
import kotlinx.coroutines.cancelAndJoin
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.ServerStatusPinger
import net.minecraft.client.multiplayer.resolver.ServerAddress

object ServerConnector {
    val PINGER = ServerStatusPinger()
    val SERVER_ADDRESS =
        if (RdiSharedConstants.DEBUG) ServerAddress("localhost", 26085) else ServerAddress("test3.davisoft.cn", 26085)
    val SERVER_INFO = ServerData(
        RdiSharedConstants.MODID_DISPLAY + RdiSharedConstants.CORE_VERSION,
        SERVER_ADDRESS.host + ":" + SERVER_ADDRESS.port,
        false
    )

    @JvmStatic
	fun connect() {
        Minecraft.getInstance().window.setTitle(RdiSharedConstants.MODID_DISPLAY + " " + RdiSharedConstants.CORE_VERSION)
        ConnectScreen.startConnecting(RdiTitleScreen.INSTANCE, Minecraft.getInstance(), SERVER_ADDRESS, SERVER_INFO)

    }

    fun ping() {
        ThreadPool.newThread {
            try {
                PINGER.pingServer(SERVER_INFO) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
