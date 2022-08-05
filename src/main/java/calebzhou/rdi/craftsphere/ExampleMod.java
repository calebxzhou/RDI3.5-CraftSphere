package calebzhou.rdi.craftsphere;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class ExampleMod implements ModInitializer {

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
	public static final String VER_DISPLAY ="3.5c/220805";
	//服务器地址,信息
	public static final ServerAddress SERVER_ADDRESS = debug?new ServerAddress("localhost",25565):new ServerAddress("test3.davisoft.cn",26085);
	public static final ServerData SERVER_INFO = new ServerData("rdi-celetech3", SERVER_ADDRESS.getHost(),false);


	public static final Logger LOGGER = LogManager.getLogger("Fabric");

	//载入区......
	public static long loadStartTime;
	public static long loadEndTime;
	public static JTextArea loadProgressInfo = new JTextArea("RDI客户端正在启动....\n");
	public static JProgressBar loadProgressBar = new JProgressBar();
	public static JFrame loadProgressFrame = new JFrame("RDI客户端启动中");
	static {
		loadProgressBar.setMaximum(100);
		loadStartTime=System.currentTimeMillis();
		loadProgressFrame.setLayout(new BorderLayout());
		loadProgressFrame.setAlwaysOnTop (true);
		loadProgressFrame.setBounds(0,0,400,300);
		DefaultCaret caret = (DefaultCaret)loadProgressInfo.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		loadProgressInfo.setCaret(caret);

		JScrollPane scroll = new JScrollPane (loadProgressInfo,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		loadProgressFrame.add(scroll,BorderLayout.CENTER);
		loadProgressFrame.add(loadProgressBar,BorderLayout.SOUTH);
		loadProgressFrame.setLocationRelativeTo(null);
		loadProgressFrame.setVisible(true);
	}
	public static void appendLoadProgressInfo(String info){
		loadProgressInfo.append(info);
		loadProgressInfo.append("\n");
		loadProgressInfo.setCaretPosition(loadProgressInfo.getDocument().getLength());
		int n = loadProgressBar.getValue();
		loadProgressBar.setValue(++n);
	}
//。。。。。。。。。
	@Override
	public void onInitialize() {
		//regSounds();
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
