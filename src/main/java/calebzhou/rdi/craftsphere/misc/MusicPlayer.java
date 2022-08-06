package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.util.FileUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import org.apache.commons.lang3.RandomUtils;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MusicPlayer {
    public static void playStartupMusic(){
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
