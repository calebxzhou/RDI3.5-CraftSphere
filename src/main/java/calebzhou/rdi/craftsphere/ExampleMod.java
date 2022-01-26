package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer {
	public static final String MODID="rdi-craftsphere";
	public static final String MODID_CHN="RDI 天空科技";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("modid");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");
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
