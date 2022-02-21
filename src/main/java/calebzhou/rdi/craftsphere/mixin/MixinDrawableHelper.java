package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

@Mixin(DrawableHelper.class)
public class MixinDrawableHelper {
    //去掉泥土图片显示背景
    @Shadow @Final @Mutable
    public static Identifier OPTIONS_BACKGROUND_TEXTURE =
            new Identifier(MODID,"options_background.png");
}
