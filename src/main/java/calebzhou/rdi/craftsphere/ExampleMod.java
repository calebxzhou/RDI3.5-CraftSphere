package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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
	public static final String VER_DISPLAY ="3.0Beta5A (2022-02-12)";
	public static Item CHECK_ITEM;

	public static Identifier[] TITLE_MUSIC_ID = new Identifier[]{
			new Identifier(MODID,"adventure"),
			new Identifier(MODID,"celeste"),
			new Identifier(MODID,"home"),
			new Identifier(MODID,"module"),
			new Identifier(MODID,"resort"),
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


		CHECK_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC));
		Registry.register(Registry.ITEM, new Identifier(MODID, "island"), CHECK_ITEM);
		KeyBinds.init();
		regEvents();

	}
	public void regEvents(){
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if(client.isInSingleplayer() || client.getCurrentServerEntry()==null || client.player==null)
				return;
			//按下H键返回空岛
			while (KeyBinds.HOME_KEY.wasPressed()) {
					KeyBinds.notifyPlayer(KeyBinds.HOME_KEY,client.player);
					client.player.sendChatMessage("/home");
			}
			//按下J键开启缓降
			while (KeyBinds.SLOWFALL_KEY.wasPressed()) {

				KeyBinds.notifyPlayer(KeyBinds.SLOWFALL_KEY,client.player);
					client.player.sendChatMessage("/slowfall 1");

			}
			//按下G键隔空跳跃
			while (KeyBinds.LEAP_KEY.wasPressed()) {
				KeyBinds.notifyPlayer(KeyBinds.LEAP_KEY,client.player);
				client.player.sendChatMessage("/leap");

			}
		});
	}
}
