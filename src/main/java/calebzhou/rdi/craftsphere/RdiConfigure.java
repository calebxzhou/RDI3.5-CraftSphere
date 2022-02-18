package calebzhou.rdi.craftsphere;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = ExampleMod.MODID)
public class RdiConfigure implements ConfigData {

    @ConfigEntry.Gui.RequiresRestart
    @Comment("""
            强制设置客户端的语言为简体中文。
            当您使用其他语言（例如文言文、繁体中文等），请将此选项设置为true，
            这样您选择语言的缺失条目，将会优先从简体中文中寻找。""")
    public boolean setDefaultLanguageToChinese = false;

    @Comment("""
            在主界面播放随机播放四首自带的BGM。
            如果您觉得BGM不太好听，可以将此选项设置为false，下次启动时将不再播放BGM。
            """)
    public boolean playBackgroundMusicOnTitleScreen = true;

    @Comment("""
            掉落速度多快时，自动开启缓降，防止摔死。速度单位为m/s，设置为-1关闭此功能
            """)
    public int autoSlowfallSpeed = 2;

    @ConfigEntry.Gui.Excluded
    private transient static boolean isReg = false;

    public static synchronized RdiConfigure getConfig(){
        if(!isReg){
            AutoConfig.register(RdiConfigure.class, Toml4jConfigSerializer::new);
            isReg=true;
        }
        return AutoConfig.getConfigHolder(RdiConfigure.class).getConfig();
    }
}
