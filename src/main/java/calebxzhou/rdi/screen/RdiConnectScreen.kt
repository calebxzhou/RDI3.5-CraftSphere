package calebxzhou.rdi.screen

import calebxzhou.libertorch.mc.gui.LtScreen
import calebxzhou.rdi.logger
import calebxzhou.rdi.misc.ServerConnector
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl
import net.minecraft.network.Connection
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.handshake.ClientIntentionPacket
import net.minecraft.network.protocol.login.ServerboundHelloPacket
import net.minecraft.world.entity.player.ProfilePublicKey
import org.quiltmc.qsl.registry.impl.sync.client.ClientRegistrySync
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.thread

/**
 * Created  on 2023-04-09,22:35.
 */
class RdiConnectScreen : LtScreen("连接画面"){

    private var status: Component = Component.translatable("connect.connecting")
    @Volatile
    var aborted = false
    companion object{
        @JvmField
        var connection: Connection?=null
        fun startConnecting(
            minecraft: Minecraft,
        ) {
            val connectScreen = RdiConnectScreen()
            minecraft.clearLevel()
            minecraft.prepareForMultiplayer()
            minecraft.currentServer = ServerConnector.SERVER_INFO
            minecraft.setScreen(connectScreen)
            connectScreen.connect(minecraft)
        }
    }


    private fun connect(minecraft: Minecraft) {
        //quilt功能
        ClientRegistrySync.createSnapshot()

        val completableFuture = minecraft.profileKeyPairManager.preparePublicKey()
        logger.info("开始连接")
        thread {
            val ip = InetSocketAddress(ServerConnector.SERVER_ADDRESS.host,ServerConnector.SERVER_ADDRESS.port)
            try {
                if (aborted) {
                    return@thread
                }
                connection = Connection.connectToServer(ip, minecraft.options.useNativeTransport())
                connection?.setListener(ClientHandshakePacketListenerImpl(connection, minecraft, RdiTitleScreen(),::updateStatus) )
                connection?.send(ClientIntentionPacket(ip.hostName, ip.port, ConnectionProtocol.LOGIN))
                connection?.send(
                        ServerboundHelloPacket(
                            minecraft.user.name,
                            completableFuture.join() as Optional<ProfilePublicKey.Data>,
                            Optional.ofNullable(minecraft.user.profileId)
                        )
                    )
            } catch (e: Exception) {
                if (aborted) {
                    return@thread
                }
                logger.error("服务器连接失败：", e)
                minecraft.execute {
                    minecraft.setScreen(
                        DisconnectedScreen(
                            RdiTitleScreen(),
                            CommonComponents.CONNECT_FAILED,
                            Component.translatable("disconnect.genericReason",  e.message
                                ?.replace((ip.hostName + ":" + ip.port).toRegex(), "")
                                ?.replace(ip.toString().toRegex(), ""))
                        )
                    )
                }
            }
        }

    }
    private fun updateStatus(status: Component) {
        this.status = status
    }
    override fun tick() {
        if (connection != null) {
            if (connection!!.isConnected) {
                connection!!.tick()
            } else {
                connection!!.handleDisconnection()
            }
        }
    }
    override fun shouldCloseOnEsc(): Boolean {
        return true
    }
    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(poseStack)
        drawCenteredString(poseStack, font, status, width / 2, height / 2 - 50, 16777215)
        super.render(poseStack, mouseX, mouseY, partialTick)
    }
}
/*val thread: Thread = object : Thread("RdiServerConnector" ) {
            override fun run() {

            }
        }
        thread.uncaughtExceptionHandler = DefaultUncaughtExceptionHandler(logger)
        thread.start()*/
