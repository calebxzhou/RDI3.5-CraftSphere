package calebxzhou.rdi.mixin;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Created  on 2023-04-13,21:38.
 */
@Mixin(LootTables.class)
public interface ALootTables {
	@Accessor
	Map<ResourceLocation, LootTable> getTables();

	@Accessor
	static Gson getGSON() {
		return null;
	}
}
