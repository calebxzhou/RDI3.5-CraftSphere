package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.module.*;
import calebzhou.rdi.craftsphere.module.area.EventAreaSelection;
import calebzhou.rdi.craftsphere.module.area.RendererAreaSelection;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class ExampleMod implements ModInitializer {

	//是否为调试模式,本地用
	public static final boolean debug=true;
	//mod id
	public static final String MODID="rdict3";
	//mod id中文名
	public static final String MODID_CHN="RDI MarinTech";
	//版本号与协议号
	public static final int VERSION=0x350;
	//显示版本
	public static final String VER_DISPLAY ="3.5 (2022-0x-xx)";

	//服务器地址,信息
	public static final ServerAddress SERVER_ADDRESS = debug?new ServerAddress("localhost",25565):new ServerAddress("test3.davisoft.cn",26088);
	public static final ServerInfo SERVER_INFO = new ServerInfo("rdi-celetech3", SERVER_ADDRESS.getAddress(),false);

	//public static Item CHECK_ITEM;

	//当前电脑信息


	public static Identifier[] TITLE_MUSIC_ID = new Identifier[]{
			new Identifier(MODID,"celeste"),
			new Identifier(MODID,"home"),
			new Identifier(MODID,"module"),
			new Identifier(MODID,"resort"),
			new Identifier(MODID,"rk3"),
			new Identifier(MODID,"rk4"),
			new Identifier(MODID,"qpt_01"),
			new Identifier(MODID,"qpt_02"),
			new Identifier(MODID,"qpt_03"),
			new Identifier(MODID,"qpt_04"),
			new Identifier(MODID,"qpt_05"),
			new Identifier(MODID,"qpt_06"),
			new Identifier(MODID,"jy"),

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
		regWorldTick();
		regNetwork();
		/*CHECK_ITEM = new Item(new FabricItemSettings().group(ItemGroup.MISC));
		Registry.register(Registry.ITEM, new Identifier(MODID, "island"), CHECK_ITEM);*/
		KeyBinds.init();
		regEvents();
		new NetworkUtils();
	}
	public void regWorldTick(){
		new AfkDetect();
		new FastTree();
		new Leap();
		new NoDroppingVoid();
	}
	public void regNetwork(){
		new IslandInfo();
	}
	public void regEvents(){
		ClientTickEvents.END_WORLD_TICK.register(KeyBinds::handleKeyActions);
		ClientTickEvents.END_WORLD_TICK.register(RendererAreaSelection::renderSelectionHud);
		new EventAreaSelection();
	}


}
