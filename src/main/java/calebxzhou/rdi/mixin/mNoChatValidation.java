package calebxzhou.rdi.mixin;

import net.minecraft.client.multiplayer.chat.ChatListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created by calebzhou on 2022-10-01,14:16.
 */
@Mixin(ChatListener.class)
public
class mNoChatValidation{
	@Overwrite
	private void onChatChainBroken() {}
}
