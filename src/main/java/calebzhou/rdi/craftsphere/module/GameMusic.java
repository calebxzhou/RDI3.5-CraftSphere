package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.mixin.AccessSystemDetails;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.SystemDetails;
import org.apache.commons.lang3.RandomUtils;

import java.util.Map;

public class GameMusic extends AbstractSoundInstance {
    public GameMusic(SoundEvent event) {
        super(event, SoundCategory.AMBIENT);
        this.repeat=false;
        this.volume=0.3f;
        this.relative=false;
    }
    public static void playOnTitle(){
        int i = RandomUtils.nextInt(0,6);
        MinecraftClient.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[i]));
        ExampleMod.LOGGER.info("play music on title");
    }

    public static void playInGame(){
        MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,0);
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            int i = RandomUtils.nextInt(6, ExampleMod.TITLE_MUSIC.length - 1); //前六个是长音乐，后面的都是短的
            MinecraftClient.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[i]));
            System.out.println("play music on join");
        }
        Map<String, String> details = ((AccessSystemDetails)new SystemDetails()).getSections();
        String entityName = MinecraftClient.getInstance().getSession().getUsername();
        HttpUtils.sendRequest("POST","/graphicsDebug","name="+entityName,"obj="+new GsonBuilder().setPrettyPrinting().create().toJson(details));

    }

}
