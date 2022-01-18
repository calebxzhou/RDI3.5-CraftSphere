package calebzhou.rdi.craftsphere.texture;

import calebzhou.rdi.craftsphere.util.FileUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
@Environment(EnvType.CLIENT)
public class LogoTexture extends ResourceTexture{
    public static Identifier LOGO= new Identifier("rdi-craftsphere:splash.png");
        public LogoTexture() {
            super(LOGO);
        }

        protected ResourceTexture.TextureData loadTextureData(ResourceManager resourceManager) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            DefaultResourcePack defaultResourcePack = minecraftClient.getResourcePackProvider().getPack();

            try {
                InputStream inputStream = FileUtils.getJarResourceAsStream("splash.png");//defaultResourcePack.open(ResourceType.CLIENT_RESOURCES, LOGO);

                ResourceTexture.TextureData var5;
                try {
                    var5 = new ResourceTexture.TextureData(new TextureResourceMetadata(true, true), NativeImage.read(inputStream));
                } catch (Throwable var8) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (inputStream != null) {
                    inputStream.close();
                }

                return var5;
            } catch (IOException var9) {
                return new ResourceTexture.TextureData(var9);
            }
        }

}
