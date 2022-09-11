package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class GameMusic extends AbstractSoundInstance {
    public GameMusic(SoundEvent event) {
        super(event, SoundSource.AMBIENT, RandomSource.create());
        this.looping=false;
        this.volume=0.3f;
        this.relative=false;
    }
    public static void playOnTitle(){
            Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,0);
            Minecraft.getInstance().getSoundManager().play(new GameMusic(ExampleMod.titleMusicEvent));
            ExampleMod.LOGGER.info("play music on title");
    }


}
