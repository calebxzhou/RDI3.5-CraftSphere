package calebzhou.rdi.craftsphere.sound;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class TitleScreenSound extends AbstractSoundInstance {
    public TitleScreenSound(SoundEvent event) {
        super(event, SoundCategory.AMBIENT);
        this.repeat=true;
        this.volume=0.18f;
        this.relative=false;
    }



}
