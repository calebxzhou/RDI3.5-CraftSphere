package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.module.area.EventAreaSelection;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class ExampleMod implements ModInitializer {
	public static final String MODID="rdi-craftsphere";
	public static final String MODID_CHN="RDI 天空科技";
	public static final int VERSION=0x3B5;
	public static final String VER_DISPLAY ="3.0R1 (2022-02-22)";
	//public static Item CHECK_ITEM;

	public static Identifier[] TITLE_MUSIC_ID = new Identifier[]{
			new Identifier(MODID,"adventure"),
			new Identifier(MODID,"celeste"),
			new Identifier(MODID,"home"),
			new Identifier(MODID,"module"),
			new Identifier(MODID,"resort"),
			new Identifier(MODID,"qpt_01"),
			new Identifier(MODID,"qpt_02"),
			new Identifier(MODID,"qpt_03"),
			new Identifier(MODID,"qpt_04"),
			new Identifier(MODID,"qpt_05"),
			new Identifier(MODID,"qpt_06"),
			new Identifier(MODID,"rk3"),
			new Identifier(MODID,"rk4"),
	};
	public static SoundEvent[] TITLE_MUSIC = Arrays.stream(TITLE_MUSIC_ID).map(SoundEvent::new).toArray(SoundEvent[]::new);
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("Fabirc");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		for (int i = 0; i < TITLE_MUSIC.length; i++) {
			Registry.register(Registry.SOUND_EVENT,TITLE_MUSIC_ID[i],TITLE_MUSIC[i]);
		}


		/*CHECK_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC));
		Registry.register(Registry.ITEM, new Identifier(MODID, "island"), CHECK_ITEM);*/
		KeyBinds.init();
		regEvents();

	}
	public void regEvents(){
		ClientTickEvents.END_WORLD_TICK.register(KeyBinds::handleKeyActions);
		ClientTickEvents.END_WORLD_TICK.register(PlayerMotionDetect::detect);
		new EventAreaSelection();
	}
}
