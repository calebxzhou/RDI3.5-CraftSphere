package calebzhou.rdi.craftsphere.texture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class Textures {
    public static Identifier TITLE_SCREEN = new Identifier("rdict3:titlescreen.png");
    public static Identifier TITLE_LOGO = new Identifier("rdict3:title_logo.png");
    public static Identifier ICON_SETTINGS = new Identifier("rdict3:icon/settings.png");
    public static Identifier ICON_MODMENU = new Identifier("rdict3:icon/modmenu.png");
    public static Identifier ICON_QUIT = new Identifier("rdict3:icon/quit.png");
    public static Identifier ICON_CONTINUE = new Identifier("rdict3:icon/continue.png");
}
