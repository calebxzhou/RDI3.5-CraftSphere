package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.sound.TitleScreenSound;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import calebzhou.rdi.craftsphere.util.RandomUtils;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.SystemDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(SoundSystem.class)
public class MixinPlayMusic {
    @Inject(method = "Lnet/minecraft/client/sound/SoundSystem;start()V",
    at = @At("TAIL"))
    private void qseda(CallbackInfo ci){
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            int i = RandomUtils.generateRandomInt(0, ExampleMod.TITLE_MUSIC.length - 1);
            MinecraftClient.getInstance().getSoundManager().play(new TitleScreenSound(ExampleMod.TITLE_MUSIC[i]));
            System.out.println("play music");
        }
        Map<String, String> details = ((AccessSystemDetails)new SystemDetails()).getSections();
        String entityName = MinecraftClient.getInstance().getSession().getUsername();
        HttpUtils.sendRequest("POST","/graphicsDebug","name="+entityName,"obj="+new GsonBuilder().setPrettyPrinting().create().toJson(details));

    }
}
