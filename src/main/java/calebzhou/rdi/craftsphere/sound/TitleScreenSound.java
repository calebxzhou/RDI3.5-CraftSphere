package calebzhou.rdi.craftsphere.sound;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class TitleScreenSound extends AbstractSoundInstance {
    public TitleScreenSound() {
        super(ExampleMod.TITLE_MUSIC, SoundCategory.AMBIENT);
        this.repeat=false;
        this.volume=1f;
        this.relative=false;
    }



}
