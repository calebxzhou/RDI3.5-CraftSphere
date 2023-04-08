package calebxzhou.rdi.mixin;

import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static calebxzhou.rdi.consts.RdiConsts.CORE_VERSION_DISPLAY;
import static calebxzhou.rdi.consts.RdiConsts.MODID_DISPLAY;

/**
 * Created by calebzhou on 2022-09-18,22:28.
 */
@Mixin(GameConfig.GameData.class)
public class mGameConfigData {
	@Shadow
	@Final
	@Mutable
	public String versionType;

	@Mutable
	@Shadow
	@Final
	public boolean disableMultiplayer;

	@Mutable
	@Shadow
	@Final
	public boolean disableChat;

	@Mutable
	@Shadow
	@Final
	public boolean demo;

	@Inject(method = "<init>",at=@At("TAIL"))
	private void changeVersionType(boolean bl, String string, String string2, boolean bl2, boolean bl3, CallbackInfo ci){
		versionType = MODID_DISPLAY+CORE_VERSION_DISPLAY;
		disableMultiplayer = false;
		disableChat = false;
		demo =false;
	}
}
