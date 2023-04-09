package calebxzhou.rdi.misc

import calebxzhou.libertorch.util.ThreadPool
import calebxzhou.rdi.consts.RdiConsts.CoreVersion
import calebxzhou.rdi.consts.RdiConsts.DEBUG
import calebxzhou.rdi.consts.RdiConsts.MODID_DISPLAY
import calebxzhou.rdi.screen.RdiTitleScreen
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.ServerStatusPinger
import net.minecraft.client.multiplayer.resolver.ServerAddress

object ServerConnector {
    val PINGER = ServerStatusPinger()
    val SERVER_ADDRESS =
        if (DEBUG) ServerAddress("localhost", 26085) else ServerAddress("test3.davisoft.cn", 26085)
    val SERVER_INFO = ServerData(
        MODID_DISPLAY + CoreVersion,
        SERVER_ADDRESS.host + ":" + SERVER_ADDRESS.port,
        false
    )

    @JvmStatic
	fun connect() {
        Minecraft.getInstance().window.setTitle("$MODID_DISPLAY $CoreVersion")
        ConnectScreen.startConnecting(RdiTitleScreen(), Minecraft.getInstance(), SERVER_ADDRESS, SERVER_INFO)

    }

    fun ping() {
        ThreadPool.run() {
            try {
                PINGER.pingServer(SERVER_INFO) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
