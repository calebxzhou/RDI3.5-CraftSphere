package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.sound.TitleScreenSound;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer {
	public static final String MODID="rdi-craftsphere";
	public static final String MODID_CHN="RDI 天空科技";
	public static final String VER="3.0Beta5 22/02/10";
	public static final String DEFAULT_UID="6400b138-3da9-4780-8540-bb212f487aa2";
	public static Item CHECK_ITEM;
	public static Identifier TITLE_MUSICI = new Identifier(MODID,"home");
	public static SoundEvent TITLE_MUSIC = new SoundEvent(TITLE_MUSICI);
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("modid");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registry.SOUND_EVENT,TITLE_MUSICI,TITLE_MUSIC);

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
