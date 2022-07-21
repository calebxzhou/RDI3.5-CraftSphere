package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.RandomUtils;

public class GameMusic extends AbstractSoundInstance {
    public GameMusic(SoundEvent event) {
        super(event, SoundSource.AMBIENT, RandomSource.create());
        this.looping=false;
        this.volume=0.3f;
        this.relative=false;
    }
    public static void playOnTitle(){
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,0);
            Minecraft.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[0]));

            ExampleMod.LOGGER.info("play music on title");
        }
    }

    public static void playInGame(){
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            int i = RandomUtils.nextInt(1,ExampleMod.TITLE_MUSIC.length-1);
            Minecraft.getInstance().getSoundManager().play(new GameMusic(ExampleMod.TITLE_MUSIC[i]));
            ExampleMod.LOGGER.info("play music on join");
        }

    }

}
