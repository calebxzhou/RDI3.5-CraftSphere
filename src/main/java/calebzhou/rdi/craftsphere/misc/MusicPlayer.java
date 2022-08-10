package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.util.FileUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import org.apache.commons.lang3.RandomUtils;

import javax.sound.sampled.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MusicPlayer {
    static {
        MusicPlayer.playStartupMusic();
    }
    public static void playStartupMusic(){
        ThreadPool.newThread(()->{
            System.out.println("放音乐");
            /*String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            File musicPlayHistoryFile = new File("logs/musicplay_"+yyyyMMdd);
            if(musicPlayHistoryFile.exists()){
                System.out.println("今天放过了");
                return;
            }*/
            //musicPlayHistoryFile.createNewFile();
            int musicAmount=25;
            //Clip clip = AudioSystem.getClip(); +  +
            play(new File("mods/rdi/music/startup/"+RandomUtils.nextInt(1, musicAmount + 1)+".aac"));
                /*AudioInputStream stream = AudioSystem.getAudioInputStream(bufferStream);
                clip.open(stream);
                clip.start();*/

        });
    }
    public static void play(File musicFile)
    {
        // local vars
        byte[]          b;              // array for the actual audio Data during the playback
        AudioTrack      track;          // track we are playing atm
        AudioFormat     af;             // the track's format
        SourceDataLine  line;           // the line we'll use the get our audio to the speaker's
        Decoder dec;            // decoder to get the audio bytes
        Frame frame;          //
        SampleBuffer    buf;            //
        int             currentTrack;   // index of current track from playlist
        MP4Container cont;           // container to open the current track with
        Movie movie;          // and get the content from the container
        boolean paused=false;
        try
        {
                cont    = new MP4Container(new RandomAccessFile(musicFile,"r")); // open titel with random access
                movie   = cont.getMovie();                          // get content from container,
                List<Track> content = movie.getTracks();
                if (content.isEmpty())     {
                    System.out.println("music content=empty");
                }
                track   = (AudioTrack) movie.getTracks().get(0);    // grab first track and set the audioformat
                af      = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
                line    = AudioSystem.getSourceDataLine(af);        // get a DataLine from the AudioSystem
                line.open();                                        // open and
                line.start();                                       // start it

                dec     = new Decoder(track.getDecoderSpecificInfo());

                buf = new SampleBuffer();
                while(track.hasMoreFrames())                // while we have frames left
                {
                    frame = track.readNextFrame();          // read next frame,
                    dec.decodeFrame(frame.getData(), buf);  // decode it and put into the buffer
                    b = buf.getData();                      // write the frame data from the buffer to our byte-array
                    line.write(b, 0, b.length);             // and from there write the byte array into our open AudioSystem DataLine

                    while (paused)                  // check if we should pause
                    {
                        Thread.sleep(500);          // if yes, stay half a second

                        if (Thread.interrupted())   // check if we should stop possibly
                        {
                            line.close();           // if yes, close line and
                            return;                 // exit thread
                        }
                    }

                    if (Thread.interrupted())       // if not in pause, still check on each frame if we should
                    {                               // stop. If so
                        line.close();               // close line and
                        return;                     // exit thread
                    }
                }

                line.close();           // after titel is over, close line

        }
        catch (LineUnavailableException | IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}