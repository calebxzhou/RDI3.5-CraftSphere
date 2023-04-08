package calebxzhou.libertorch.mc.mixin;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created  on 2023-03-12,21:34.
 */
@Mixin(AbstractWidget.class)
public interface AccessAbstractWidget {
	@Accessor
	boolean getIsHovered();
}
