package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiSharedConstants;
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

@Mixin(TitleScreen.class)
class mTitleScreen {
    @Inject(method = "init",at=@At("HEAD"))
    private void alwaysGoNew(CallbackInfo ci){
        Minecraft.getInstance().setScreen(new RdiTitleScreen());
    }


}
