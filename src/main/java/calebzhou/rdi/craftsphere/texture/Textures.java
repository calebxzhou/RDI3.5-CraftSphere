package calebzhou.rdi.craftsphere.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Textures {
    public static Identifier TITLE_SCREEN = new Identifier("rdi-craftsphere:titlescreen.png");
    public static Identifier TITLE_LOGO = new Identifier("rdi-craftsphere:title_logo.png");
    public static Identifier ICON_SETTINGS = new Identifier("rdi-craftsphere:icon/settings.png");
    public static Identifier ICON_MODMENU = new Identifier("rdi-craftsphere:icon/modmenu.png");
    public static Identifier ICON_QUIT = new Identifier("rdi-craftsphere:icon/quit.png");
    public static Identifier ICON_CONTINUE = new Identifier("rdi-craftsphere:icon/continue.png");
}
