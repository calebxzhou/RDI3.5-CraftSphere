package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.RdiCore;
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
        return RdiCore.MODID_CHN+" "+ RdiCore.VER_DISPLAY;
    }
    @Overwrite
    public void updateTitle() {
        window.setTitle(this.createTitle());
    }
}
