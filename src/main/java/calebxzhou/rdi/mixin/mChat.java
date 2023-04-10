package calebxzhou.rdi.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created  on 2023-04-10,8:48.
 */
public class mChat {
}
//去掉说话的尖括号
@Mixin(TranslatableContents.class)
class mNoChatBrackets {
	@Mutable
	@Shadow
	@Final
	private String key;

	@Inject(method = "<init>(Ljava/lang/String;)V", at = @At("TAIL"))
	public void modify(String key, CallbackInfo ci) {
		fixKey();
	}

	@Inject(method = "<init>(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("TAIL"))
	public void modifyWithArgs(String key, Object[] args, CallbackInfo ci) {
		fixKey();
	}
	public void fixKey() {
		switch(key) {
			case "chat.type.text":
			case "chat.type.emote":
			case "chat.type.announcement":
			case "chat.type.admin":
			case "chat.type.team.text":
			case "chat.type.team.sent":
				key = "rdi." + key;
				break;
		}

	}
}
@Mixin(ChatListener.class)

class mNoChatValidation{
	@Overwrite
	private void onChatChainBroken() {}
}
@Mixin(ChatScreen.class)
abstract class mUseRdiChatScreen {
	@Shadow
	public abstract String normalizeChatMessage(String message);

	@Overwrite
	public boolean handleChatInput(String message, boolean addToChat) {
		message = normalizeChatMessage(message);
		if (!message.isEmpty()) {
			if (addToChat) {
				Minecraft.getInstance().gui.getChat().addRecentChat(message);
			}
			if (message.startsWith("/")) {
				//是指令 作为指令发送
				Minecraft.getInstance().player.commandSigned(message.substring(1), null);
			} else {
				//不是指令 作为信息发送（/speak xxx)
				Minecraft.getInstance().player.commandSigned("speak " + message, null);
			}

		}
		return true;
	}
}
@Mixin(ChatReportScreen.class)
class mNoReportScreen {
	@Shadow
	@Final
	@Nullable Screen lastScreen;

	@Overwrite
	public void init() {
		Minecraft.getInstance().setScreen(lastScreen);
	}
	@Overwrite
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {}
}
