package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.misc.ServerConnector;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
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

//改变游戏版本
@Mixin(DebugScreenOverlay.class)
public class mGameVersion {
    @Redirect(method = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;getGameInformation()Ljava/util/List;",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/WorldVersion;getName()Ljava/lang/String;"))
    private String version(WorldVersion instance){
        return ExampleMod.GAME_VERSION;
    }
}
@Mixin(TitleScreen.class)
class mTitleScreen {
    @Shadow @Final @Mutable
    public static final Component COPYRIGHT_TEXT = Component.literal("按Enter(回车)键进入RDI服务器");

    @Redirect(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/WorldVersion;getName()Ljava/lang/String;"))
    private String vers(WorldVersion instance){
        return ExampleMod.GAME_VERSION;
    }
    @Inject(method = "tick",at = @At("TAIL"))
    private void enterServer(CallbackInfo ci){
        long handle = Minecraft.getInstance().getWindow().getWindow();
        if(InputConstants.isKeyDown(handle,InputConstants.KEY_RETURN)){
            ServerConnector.connect();
        }
    }
}
