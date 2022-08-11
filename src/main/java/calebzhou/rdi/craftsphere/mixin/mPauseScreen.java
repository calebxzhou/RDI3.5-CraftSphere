package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.screen.NewTitleScreen;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class mPauseScreen extends Screen {
    private mPauseScreen(Component component) {
        super(component);
    }

    //暂停菜单点击断开连接 去新的标题界面
    @Inject(method = "method_19836",at = @At(value = "INVOKE",ordinal = 1,target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void goNewTitle(CallbackInfo ci){
        minecraft.setScreen(NewTitleScreen.INSTANCE);
    }
    @Inject(method = "method_19836",at = @At(value = "INVOKE",ordinal = 2,target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    private void goNewTitle2(CallbackInfo ci){
        minecraft.setScreen(NewTitleScreen.INSTANCE);
    }
}
