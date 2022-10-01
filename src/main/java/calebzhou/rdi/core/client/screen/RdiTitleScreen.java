package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.RdiSharedConstants;
import calebzhou.rdi.core.client.misc.MusicPlayer;
import calebzhou.rdi.core.client.misc.ServerConnector;
import calebzhou.rdi.core.client.util.DialogUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class RdiTitleScreen extends Screen {
    public static final RdiTitleScreen INSTANCE = new RdiTitleScreen();
    public RdiTitleScreen() {
        super(Component.literal("主界面"));
		Minecraft.getInstance().updateTitle();
		/*enterKeyTexture = RdiTexture.loadTexture(new File(RdiSharedConstants.RDI_TEXTURE_FOLDER, "enter.png").getAbsolutePath());
		enterKeyTexture.bind();*/
	}
    public boolean shouldCloseOnEsc() {
        return false;
    }
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		GlStateManager._clearColor(0.9f, 0.9f, 0.9f, 1.0F);
		GlStateManager._clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableBlend();

		/*enterKeyTexture.setParameter( GL_TEXTURE_MIN_FILTER,GL_LINEAR);
		enterKeyTexture.setParameter( GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		enterKeyTexture.setParameter( GL_TEXTURE_WRAP_S, GL_CLAMP);
		enterKeyTexture.setParameter( GL_TEXTURE_WRAP_T, GL_CLAMP);*/
		this.font.draw(matrices, "按Enter(回车)键", (this.width/2.0f)-30, this.height/2f, 0xFF000000);
    }
    public void tick() {

        long handle = Minecraft.getInstance().getWindow().getWindow();
        if(InputConstants.isKeyDown(handle, InputConstants.KEY_0)){
            if(RdiSharedConstants.DEBUG)
            this.minecraft.setScreen(new JoinMultiplayerScreen(this));
            return;
        }
        if(InputConstants.isKeyDown(handle, InputConstants.KEY_K)){
            this.minecraft.setScreen(new SelectWorldScreen(this));
            return;
        }
        if(InputConstants.isKeyDown(handle, InputConstants.KEY_M)){
            try {
                this.minecraft.setScreen((Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(Screen.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                DialogUtils.showMessageBox("error","必须安装ModMenu模组以使用本功能！！");
                e.printStackTrace();
            }
            return;
        }
        if(InputConstants.isKeyDown(handle, InputConstants.KEY_O)){
            MusicPlayer.playOggAsync(new File(RdiSharedConstants.RDI_SOUND_FOLDER,"settings.ogg"));
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
            return;
        }
        if(InputConstants.isKeyDown(handle,InputConstants.KEY_RETURN)){
            ServerConnector.connect();
            //Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,1);
            MusicPlayer.playOggAsync(new File(RdiSharedConstants.RDI_SOUND_FOLDER,"connect.ogg"));
        }



    }
    public void init() {
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,0);
    }
}
