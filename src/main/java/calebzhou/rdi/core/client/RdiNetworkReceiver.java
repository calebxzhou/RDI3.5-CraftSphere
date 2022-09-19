package calebzhou.rdi.core.client;

import calebzhou.rdi.core.client.model.RdiUser;
import calebzhou.rdi.core.client.util.DialogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.io.FileUtils;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by calebzhou on 2022-09-18,22:56.
 */
public class RdiNetworkReceiver {
	public static final RdiNetworkReceiver INSTANCE = new RdiNetworkReceiver();
	private RdiNetworkReceiver(){ }
	public void register(){
		ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.SET_PASSWORD,this::onSetPassword);
		ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.DIALOG_INFO,this::onReceiveDialogInfo);
		ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.POPUP,this::onReceivePopup);
	}

	private void onSetPassword(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
		String pwd = buf.readUtf();
		File pwdFile = new File(RdiSharedConstants.RDI_USERS_FOLDER, RdiUser.getCurrentUser().getUuid() + "_password.txt");
		try {
			FileUtils.write(pwdFile,pwd, StandardCharsets.UTF_8,false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//接收服务器的弹框信息
	private void onReceivePopup(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
		String info = buf.readUtf();
		String[] split = info.split("\\|");
		String type= split[0];
		String title= split[1];
		String content= split[2];
		TrayIcon.MessageType realType;
		switch (type){
			case "info"->realType= TrayIcon.MessageType.INFO;
			case "warning"->realType= TrayIcon.MessageType.WARNING;
			case "error"->realType= TrayIcon.MessageType.ERROR;
			default -> realType= TrayIcon.MessageType.NONE;
		}
		DialogUtils.showPopup(realType,title,content);

	}

	//接收服务器的对话框信息
	private void onReceiveDialogInfo(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
		String info = buf.readUtf();
		String[] split = info.split("\\|");
		String type= split[0];
		String title= split[1];
		String content= split[2];
		DialogUtils.showMessageBox(type,title,content);
	}
}