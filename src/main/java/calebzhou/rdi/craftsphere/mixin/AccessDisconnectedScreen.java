package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisconnectedScreen.class)
public interface AccessDisconnectedScreen {
    @Accessor
    Text getReason();
}
