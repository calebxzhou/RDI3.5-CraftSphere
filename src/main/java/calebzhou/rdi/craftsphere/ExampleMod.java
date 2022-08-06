package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.MusicPlayer;
import calebzhou.rdi.craftsphere.misc.RdiSystemTray;
import calebzhou.rdi.craftsphere.util.AwtImageUtils;
import calebzhou.rdi.craftsphere.util.FileUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExampleMod implements ModInitializer {

	//是否为调试模式,本地用
	public static final boolean debug=false;
	//mod id
	public static final String MODID="rdict3";
	//mod id中文名
	public static final String MODID_CHN="RDI CeleTech";
	//版本号与协议号
	public static final int VERSION=0x35A;
	public static final String GAME_VERSION = "1.19.2";
	//显示版本
	public static final String VER_DISPLAY ="3.5e/220806";
	//服务器地址,信息
	public static final ServerAddress SERVER_ADDRESS = debug?new ServerAddress("localhost",25565):new ServerAddress("test3.davisoft.cn",26085);
	public static final ServerData SERVER_INFO = new ServerData("rdi-celetech3", SERVER_ADDRESS.getHost(),false);


	public static final Logger LOGGER = LogManager.getLogger("Fabric");

	@Override
	public void onInitialize() {
		new EventRegister();
	}
	/*


	 */
	public static SoundEvent titleMusicEvent;
	public void regSounds(){
		ResourceLocation titleMusic = new ResourceLocation(MODID, "title_sound");
		titleMusicEvent = new SoundEvent(titleMusic);
		Registry.register(Registry.SOUND_EVENT, titleMusic, titleMusicEvent);
	}


}
