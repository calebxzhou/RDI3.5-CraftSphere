package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.mixin.AccessSystemReport;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class GameMusic extends AbstractSoundInstance {
    public GameMusic(SoundEvent event) {
        super(event, SoundSource.AMBIENT);
        this.looping=false;
        this.volume=0.3f;
        this.relative=false;
    }
    public static void playOnTitle(){
        int i = RandomUtils.nextInt(0,6);
        Minecraft.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[i]));
        ExampleMod.LOGGER.info("play music on title");
    }

    public static void playInGame(){
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,0);
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            int i = RandomUtils.nextInt(6, ExampleMod.TITLE_MUSIC.length - 1); //前六个是长音乐，后面的都是短的
            Minecraft.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[i]));
            System.out.println("play music on join");
        }
        Map<String, String> details = ((AccessSystemReport)new SystemReport()).getEntries();
        String entityName = Minecraft.getInstance().getUser().getName();
        HttpUtils.sendRequest("POST","/graphicsDebug","name="+entityName,"obj="+new GsonBuilder().setPrettyPrinting().create().toJson(details));

    }

}
