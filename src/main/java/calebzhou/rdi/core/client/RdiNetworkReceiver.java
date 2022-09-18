package calebzhou.rdi.core.client;

import calebzhou.rdi.core.client.model.RdiUser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.io.FileUtils;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

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
		ClientPlayNetworking.registerGlobalReceiver(NetworkPackets.SET_PASSWORD,this::setPassword);
	}

	private void setPassword(Minecraft minecraft, ClientPacketListener listener, FriendlyByteBuf buf, PacketSender sender) {
		String pwd = buf.readUtf();
		File pwdFile = new File(RdiSharedConstants.RDI_USERS_FOLDER, RdiUser.getCurrentUser().getUuid() + "_password.txt");
		try {
			FileUtils.write(pwdFile,pwd, StandardCharsets.UTF_8,false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
