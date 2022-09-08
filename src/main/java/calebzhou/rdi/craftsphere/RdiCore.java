package calebzhou.rdi.craftsphere;

import calebzhou.rdi.craftsphere.misc.LoadProgressDisplay;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import calebzhou.rdi.craftsphere.emojiful.EmojiClientProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class RdiCore implements ModInitializer {
	static {
		ThreadPool.newThread(LoadProgressDisplay.INSTANCE::start);
	}

	//是否为调试模式,本地用
	public static final boolean debug=true;
	//mod id
	public static final String MODID="rdict3";
	//mod id中文名
	public static final String MODID_CHN="RDI CeleTech";
	//版本号与协议号
	public static final int VERSION=0x35A;
	public static final String GAME_VERSION = "1.19.2";
	//显示版本
	public static final String VER_DISPLAY ="3.7/220917";

	public static final Logger LOGGER = LogManager.getLogger("RDI-Core");

	@Override
	public void onInitialize(ModContainer container) {
		new EventRegister();
		EmojiClientProxy.INSTANCE.init();

	}
	/*


	 */
	//public static SoundEvent titleMusicEvent;
	/*public void regSounds(){
		ResourceLocation titleMusic = new ResourceLocation(MODID, "title_sound");
		titleMusicEvent = new SoundEvent(titleMusic);
		Registry.register(Registry.SOUND_EVENT, titleMusic, titleMusicEvent);
	}*/


}
