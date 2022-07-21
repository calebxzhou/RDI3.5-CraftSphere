package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.module.MiddleKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public abstract class MixinMouseMiddleKey {
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public ClientLevel level;

    @Shadow protected abstract void pickBlock();

    @Redirect(method = "Lnet/minecraft/client/Minecraft;handleKeybinds()V",
    at=@At(value="INVOKE",target = "Lnet/minecraft/client/Minecraft;pickBlock()V"))
    private void handleMiddleKey(Minecraft mc){
        //非创造 ， 才能用智能选取工具
        if(!player.isCreative()){
            MiddleKey.handle(hitResult,player,level);
        }else
            pickBlock();

    }
}
