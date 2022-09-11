package calebzhou.rdi.core.client.texture;

import calebzhou.rdi.core.client.util.FileUtils;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
@Environment(EnvType.CLIENT)
public class LogoTexture extends SimpleTexture{
    public static ResourceLocation LOGO= new ResourceLocation("rdict3:splash.png");
        public LogoTexture() {
            super(LOGO);
        }

        protected TextureImage getTextureImage(ResourceManager resourceManager) {
            Minecraft minecraftClient = Minecraft.getInstance();
            VanillaPackResources defaultResourcePack = minecraftClient.getClientPackSource().getVanillaPack();

            try {
                InputStream inputStream = FileUtils.getJarResourceAsStream("splash.png");//defaultResourcePack.open(ResourceType.CLIENT_RESOURCES, LOGO);

                TextureImage var5;
                try {
                    var5 = new TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputStream));
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
                return new TextureImage(var9);
            }
        }

}
