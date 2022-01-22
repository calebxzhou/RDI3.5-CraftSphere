package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleMod implements ModInitializer {
	public static final String MODID="rdi-craftsphere";
	public static final String MODID_CHN="RDI空岛";
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
			//按下H键返回空岛
			while (KeyBinds.HOME_KEY.wasPressed()) {
				if(client.isInSingleplayer())
					return;
				if(client.getCurrentServerEntry()!=null && client.player!=null){
					client.player.sendChatMessage("您按下了“返回空岛”快捷键。");
					client.player.sendChatMessage("/home");
				}
			}
			//按下J键开启缓降
			while (KeyBinds.SLOWFALL_KEY.wasPressed()) {
				if(client.isInSingleplayer())
					return;
				if(client.getCurrentServerEntry()!=null && client.player!=null){

					client.player.sendChatMessage("您按下了“快捷缓降”快捷键。");
					client.player.sendChatMessage("/slowfall 1");

				}
			}
		});
	}
}
