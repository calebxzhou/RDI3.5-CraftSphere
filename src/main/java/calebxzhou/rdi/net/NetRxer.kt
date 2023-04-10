package calebxzhou.rdi.net

import calebxzhou.libertorch.SerDesJson
import calebxzhou.libertorch.util.OsDialogUt.showMessageBox
import calebxzhou.libertorch.util.OsDialogUt.showPopup
import calebxzhou.rdi.RdiCore
import calebxzhou.rdi.consts.NetPacks
import calebxzhou.rdi.logger
import calebxzhou.rdi.model.RdiGeoLocation
import calebxzhou.rdi.model.RdiUser
import calebxzhou.rdi.model.RdiWeather
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.FriendlyByteBuf
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

/**
 * Created by calebzhou on 2022-09-18,22:56.
 */
object NetRxer {
    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(NetPacks.SET_PASSWORD, NetRxer::onSetPassword)
        ClientPlayNetworking.registerGlobalReceiver(NetPacks.DIALOG_INFO, NetRxer::onReceiveDialogInfo)
        ClientPlayNetworking.registerGlobalReceiver(NetPacks.POPUP, NetRxer::onReceivePopup)
        ClientPlayNetworking.registerGlobalReceiver(NetPacks.WEATHER, NetRxer::onReceiveWeather)
        ClientPlayNetworking.registerGlobalReceiver(
            NetPacks.GEO_LOCATION,
            NetRxer::onReceiveGeoLocation
        )
    }

    private fun onReceiveWeather(
        minecraft: Minecraft,
        listener: ClientPacketListener,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        RdiCore.currentWeather = SerDesJson.gson.fromJson(buf.readUtf(), RdiWeather::class.java)
    }
    private fun onReceiveGeoLocation(
        minecraft: Minecraft,
        listener: ClientPacketListener,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        RdiCore.currentGeoLocation =SerDesJson.gson.fromJson(buf.readUtf(), RdiGeoLocation::class.java)
    }

    private fun onSetPassword(
        minecraft: Minecraft,
        listener: ClientPacketListener,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        val pwd = buf.readUtf()
        logger.info("正在更改密码：{}",pwd)
        RdiUser.now.pwd=pwd
        RdiUser.now.save()
        logger.info("更改完成：{}",RdiUser.now)
    }

    //接收服务器的弹框信息
    private fun onReceivePopup(
        minecraft: Minecraft,
        listener: ClientPacketListener,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        val split = buf.readUtf().split("|")
        val type = split[0]
        val title = split[1]
        val content = split[2]
        showPopup(type, title, content)
    }

    //接收服务器的对话框信息
    private fun onReceiveDialogInfo(
        minecraft: Minecraft,
        listener: ClientPacketListener,
        buf: FriendlyByteBuf,
        sender: PacketSender
    ) {
        val split = buf.readUtf().split("|")
        val type = split[0]
        val title = split[1]
        val content = split[2]
        showMessageBox(type, title, content)
    }
}
