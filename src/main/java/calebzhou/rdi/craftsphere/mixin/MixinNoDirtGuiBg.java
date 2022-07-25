package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;
//去掉泥土图片显示背景
@Mixin(GuiComponent.class)
public class MixinNoDirtGuiBg {

    @Shadow @Final @Mutable
    public static ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(MODID,"options_background.png");
}
