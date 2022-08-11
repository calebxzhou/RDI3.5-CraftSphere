package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.ExampleMod;

import java.io.File;
import java.io.InputStream;

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
