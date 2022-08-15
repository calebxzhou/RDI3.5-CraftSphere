package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.FileConst;
import calebzhou.rdi.craftsphere.misc.MusicPlayer;
import calebzhou.rdi.craftsphere.texture.Textures;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class RdiPauseScreen extends Screen {
    private final boolean showMenu;

    public RdiPauseScreen(boolean showMenu) {
        super(Component.literal("菜单"));
        this.showMenu=showMenu;
    }

    @Override
    protected void init() {
        if(showMenu)
            initWidgets();
    }


    private void initWidgets(){



        int w = this.width /2 - 30;
        int j = this.height / 2 - 50;
        this.addRenderableWidget(new ImageButton(w - 25, j, 20, 20, 0,0,20, Textures.ICON_CONTINUE,32,64, (button) -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        },  Component.translatable("menu.returnToGame")));

        this.addRenderableWidget(new ImageButton(w , j, 20, 20, 0,0,20, Textures.ICON_SETTINGS,32,64, (button) -> {
            MusicPlayer.playOggAsync(new File(FileConst.RDI_SOUND_FOLDER,"settings.ogg"));
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }, Component.translatable("menu.options")));
        this.addRenderableWidget(new ImageButton(w+25 , j, 20, 20, 0,0,20, Textures.ICON_MODMENU,32,64, (button) -> {
            try {
                this.minecraft.setScreen((Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(Screen.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                DialogUtils.showMessageBox("error","必须安装ModMenu模组以使用本功能！！");
                e.printStackTrace();
            }
        }, Component.translatable("menu.options")));


        this.addRenderableWidget(new ImageButton(w + 50, j, 20, 20, 0, 0, 20, Textures.ICON_QUIT, 32, 64, (button) -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = false;
            button.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
            } else {
                this.minecraft.clearLevel();
            }

            MusicPlayer.playOggAsync(new File(FileConst.RDI_SOUND_FOLDER,"disconnect.ogg"));
                this.minecraft.setScreen(RdiTitleScreen.INSTANCE);
        }, Component.translatable("menu.disconnect")));
    }

    @Override
    public void tick() {
        super.tick();




    }


    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

        if (this.showMenu) {
            this.renderBackground(matrices);
            drawCenteredString(matrices, this.font, this.title, this.width / 2, 40, 16777215);
        } else {
            drawCenteredString(matrices, this.font, this.title, this.width / 2, 10, 16777215);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
