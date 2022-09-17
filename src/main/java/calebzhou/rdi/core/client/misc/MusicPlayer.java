package calebzhou.rdi.core.client.misc;

import calebzhou.rdi.core.client.FileConst;
import calebzhou.rdi.core.client.util.ExampleOggPlayer;
import calebzhou.rdi.core.client.util.ThreadPool;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class MusicPlayer {

public static boolean needToPlayEnterGameSound = false;
    public static void playStartupMusic(){
        ThreadPool.newThread(()->{
            playOgg(new File(FileConst.RDI_SOUND_FOLDER,"startup.ogg"));
        });
    }
    public static void playOggAsync(File musicFile){
        ThreadPool.newThread(()-> playOgg(musicFile));
    }

    public static void playOgg(File musicFile){
        new ExampleOggPlayer(musicFile).start();
    }
    public static void playAac(File musicFile)
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
