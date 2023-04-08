package calebxzhou.rdi.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created by calebzhou on 2022-09-22,10:56.
 */
@Mixin(Options.class)
public abstract class mFieldOfView {
	@Shadow
	public static Component genericValueLabel(Component optionText, Component value) {
		return null;
	}

	@Shadow
	@Mutable @Final
	private final OptionInstance<Integer> fov = new OptionInstance<>(
			"options.fov",
			OptionInstance.noTooltip(),
			(component, value) -> switch(value) {
				case 70 -> genericValueLabel(component, Component.translatable("options.fov.min"));
				case 110 -> genericValueLabel(component, Component.translatable("options.fov.max"));
				default -> genericValueLabel(component, Component.literal(String.valueOf(value)));
			},
			new OptionInstance.IntRange(30, 110),
			Codec.DOUBLE.xmap(value -> (int)(value * 40.0 + 70.0), integer -> ((double)integer.intValue() - 70.0) / 40.0),
			90,
			integer -> Minecraft.getInstance().levelRenderer.needsUpdate()
	);
}
