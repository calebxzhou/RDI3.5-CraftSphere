package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.ExampleMod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.system.MemoryUtil.memSlice;

public class FileUtils {
    public static final File GAME_FOLDER = new File(".");
    public static final File MOD_FOLDER = new File(GAME_FOLDER,"mods");
    public static File getJarResourceFile(String fileInJar){
        return new File(getJarResourceFileUrl(fileInJar));
    }
    public static String getJarResourceFileUrl(String fileInJar){
        return FileUtils.class.getClassLoader().getResource(fileInJar)
                .getFile()/*.replace("/","\\")*/.replace("%20"," ");
    }
    public static InputStream getJarResourceAsStream(String fileInJar)
    {
        return ExampleMod.class.getResourceAsStream("/assets/rdict3/"+fileInJar);
    }

}
