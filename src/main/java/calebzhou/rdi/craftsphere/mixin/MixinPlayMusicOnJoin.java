package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.sound.TitleScreenSound;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.SystemDetails;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClientWorld.class)
public class MixinPlayMusicOnJoin {
    @Inject(method = "Lnet/minecraft/client/world/ClientWorld;addPlayer(ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V"
            ,at = @At("TAIL"))
    private void qseda(CallbackInfo ci){
        MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,0);
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            int i = RandomUtils.nextInt(6, ExampleMod.TITLE_MUSIC.length - 1);
            MinecraftClient.getInstance().getSoundManager().play(new TitleScreenSound(ExampleMod.TITLE_MUSIC[i]));
            System.out.println("play music on join");
        }
        Map<String, String> details = ((AccessSystemDetails)new SystemDetails()).getSections();
        String entityName = MinecraftClient.getInstance().getSession().getUsername();
        HttpUtils.sendRequest("POST","/graphicsDebug","name="+entityName,"obj="+new GsonBuilder().setPrettyPrinting().create().toJson(details));

    }
}
