package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiSharedConstants;
import calebzhou.rdi.core.client.misc.ServerConnector;
import calebzhou.rdi.core.client.screen.RdiTitleScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public @Mixin(TitleScreen.class)
class mTitleScreen {
    @Shadow
    @Final
    @Mutable
    public static final Component COPYRIGHT_TEXT = Component.literal("按回车键进入RDI服务器");

    @Inject(method = "<init>()V",at=@At("TAIL"))
    private void alwaysGoNew(CallbackInfo ci){
        Minecraft.getInstance().setScreen(RdiTitleScreen.INSTANCE);
    }
    @Redirect(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/WorldVersion;getName()Ljava/lang/String;"))
    private String vers(WorldVersion instance){
        return RdiSharedConstants.GAME_VERSION;
    }

    @Inject(method = "tick",at = @At("TAIL"))
    private void enterServer(CallbackInfo ci){
        long handle = Minecraft.getInstance().getWindow().getWindow();
        if(InputConstants.isKeyDown(handle,InputConstants.KEY_RETURN)){
            ServerConnector.connect();
        }
    }
}
