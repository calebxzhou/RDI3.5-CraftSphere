package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.UserInfoStorage;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.FileUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.client.main.Main;
import net.minecraft.core.UUIDUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.UUID;

@Mixin(Main.class)
public class mClientStartup {
    @Inject(method = "main",remap = false,at = @At("HEAD"))
    private static void playMusic(String[] strings, CallbackInfo ci){
        //播放音乐
        ThreadPool.newThread(()->{
            try {
                int musicAmount=2;
                Clip clip = AudioSystem.getClip();
                AudioInputStream stream = AudioSystem.getAudioInputStream(FileUtils.getJarResourceAsStream("music/startup/"+RandomUtils.nextInt(1,musicAmount+1)+".wav"));
                clip.open(stream);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }

        });

    }
    @Inject(method = "main",remap = false,at = @At(value = "INVOKE",target = "Ljava/util/List;isEmpty()Z"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void readUuid(String[] strings, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25, OptionSpec optionSpec26, OptionSet optionSet, List list){
        String uuid = (String) optionSpec12.value(optionSet);
        String name = (String) optionSpec11.value(optionSet);
        UserInfoStorage.UserName=name;
        ExampleMod.LOGGER.info("成功读取用户名，{}",name);
        if(StringUtils.isEmpty(uuid)){
            ThreadPool.newThread(()->DialogUtils.showWarnPopup("无法读取您的微软正版账号！"));
            UserInfoStorage.UserUuid= UUIDUtil.createOfflinePlayerUUID(name).toString();
        }else{
            //mojang登录的uuid不带横线，要通过正则表达式转换成带横线的
            uuid=uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5" );
            ExampleMod.LOGGER.info("成功读取正版uuid，{}",uuid);
            UserInfoStorage.UserUuid=uuid;
        }
    }
}
