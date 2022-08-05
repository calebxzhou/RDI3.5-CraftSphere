package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.FileUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import net.minecraft.client.main.Main;
import org.apache.commons.lang3.RandomUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mixin(Main.class)
public class mStartupMusic {
    @Inject(method = "main",remap = false,at = @At("HEAD"))
    private static void playMusic(String[] strings, CallbackInfo ci){
        //播放音乐
        ThreadPool.newThread(()->{
            System.out.println("放音乐");
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File musicPlayHistoryFile = new File("logs/musicplay_"+yyyyMMdd);
            if(musicPlayHistoryFile.exists()){
                System.out.println("今天放过了");
                return;
            }
            try {
                musicPlayHistoryFile.createNewFile();
                int musicAmount=11;
                Clip clip = AudioSystem.getClip();
                InputStream inputStream = FileUtils.getJarResourceAsStream("music/startup/" + RandomUtils.nextInt(1, musicAmount + 1) + ".wav");
                BufferedInputStream bufferStream = new BufferedInputStream(inputStream);
                AudioInputStream stream = AudioSystem.getAudioInputStream(bufferStream);
                clip.open(stream);
                clip.start();
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                throw new RuntimeException(e);
            }

        });

    }
}
