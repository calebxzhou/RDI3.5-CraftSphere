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

    @Inject(method = "createPauseMenu",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screens/PauseScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private void goNewTitle(CallbackInfo ci){
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, Component.literal("断开连接"), button -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = this.minecraft.isConnectedToRealms();
            button.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                this.minecraft.clearLevel();
            }
            if (bl) {
                this.minecraft.setScreen(NewTitleScreen.INSTANCE);
            } else if (bl2) {
                this.minecraft.setScreen(NewTitleScreen.INSTANCE);
            } else {
                this.minecraft.setScreen(NewTitleScreen.INSTANCE);
            }
        }));
    }
}
