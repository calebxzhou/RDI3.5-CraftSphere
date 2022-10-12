package calebzhou.rdi.core.client.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChatScreen.class)
public abstract class mUseRdiChatScreen {
	@Shadow
	public abstract String normalizeChatMessage(String message);

	@Overwrite
	public boolean handleChatInput(String message, boolean addToChat) {
		message = normalizeChatMessage(message);
		if (message.isEmpty()) {
			return true;
		} else {
			if (addToChat) {
				Minecraft.getInstance().gui.getChat().addRecentChat(message);
			}
			if (message.startsWith("/")) {
				//是指令 作为指令发送
				Minecraft.getInstance().player.commandSigned(message.substring(1),null);
			} else {
				//不是指令 作为信息发送（/speak xxx)
				Minecraft.getInstance().player.commandSigned("speak "+message,null);
			}

			return true;
		}
	}
}
