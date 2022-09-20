package calebzhou.rdi.core.client;

import java.io.File;

/**
 * Created by calebzhou on 2022-09-10,15:09.
 */
public class RdiSharedConstants {
	//mod id
	public static final String MODID="rdict3";
	//mod id中文名
	public static final String MODID_DISPLAY ="RDI CeleTech";
	//是否为调试模式,本地用
	public static final boolean DEBUG=true;
	//版本号与协议号
	public static final int PROTOCOL_VERSION =0x37;
	public static final String GAME_VERSION = "1.19.2";
	//显示版本
	public static final String CORE_VERSION ="3.7/220917";
	public static final File RDI_FOLDER = new File("mods/rdi");
	public static final File RDI_SOUND_FOLDER = new File(RDI_FOLDER,"sound");
	public static final File RDI_USERS_FOLDER = new File(RDI_FOLDER,"users");
	public static final File RDI_EMOJI_IMAGE_FOLDER = new File(RDI_FOLDER,"emoji/img");
	public static final File RDI_EMOJI_FOLDER = new File(RDI_FOLDER,"emoji");
	public static final String RDI_ICON_PATH = "assets/rdict3/icon/rdi_logo.png";
	public static final String FONT_PATH = "assets/minecraft/font/rdifont.ttf";
}
