package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.misc.SmartMiddleKey;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;

//智能中键选取工具
@Mixin(Minecraft.class)
public abstract class MixinSmartMiddleKey {
    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public ClientLevel level;

    @Shadow protected abstract void pickBlock();


    @Redirect(method = "handleKeybinds()V",
    at=@At(value="INVOKE",target = "Lnet/minecraft/client/Minecraft;pickBlock()V"))
    private void handleMiddleKey(Minecraft mc){
        if(player==null) return;
        //非创造 ， 才能用智能选取工具
        if(player.isCreative()){
            SmartMiddleKey.handle(hitResult,player,level);
        }else
            pickBlock();

    }
}
