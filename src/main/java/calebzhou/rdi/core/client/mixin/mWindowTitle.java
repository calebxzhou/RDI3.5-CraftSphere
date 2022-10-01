package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiSharedConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public class mWindowTitle {
    @Shadow @Final private Window window;

    /**
     * @author
     *  RDI 窗口标题
     */
    @Overwrite
    private String createTitle(){
        return RdiSharedConstants.MODID_DISPLAY+" "+ RdiSharedConstants.CORE_VERSION;
    }
}
