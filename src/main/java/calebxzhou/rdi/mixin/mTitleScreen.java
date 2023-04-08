package calebxzhou.rdi.mixin;

import calebxzhou.rdi.screen.RdiTitleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
class mTitleScreen {
    @Inject(method = "init",at=@At("HEAD"))
    private void alwaysGoNew(CallbackInfo ci){
        Minecraft.getInstance().setScreen(new RdiTitleScreen());
    }


}
