package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.module.NewTitleScreen;
import calebzhou.rdi.craftsphere.texture.Textures;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class PauseScreen extends Screen {
    private final boolean showMenu;
    public PauseScreen(boolean showMenu) {
        super(new TextComponent("菜单"));
        this.showMenu=showMenu;
    }

    @Override
    protected void init() {
        if(showMenu)
            initWidgets();
    }
    private void initWidgets(){
        int w = this.width /2 - 10;
        int j = this.height / 2 - 50;
        this.addRenderableWidget(new ImageButton(w - 25, j, 20, 20, 0,0,20, Textures.ICON_CONTINUE,32,64, (button) -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        },  new TranslatableComponent("menu.returnToGame")));

        this.addRenderableWidget(new ImageButton(w , j, 20, 20, 0,0,20, Textures.ICON_SETTINGS,32,64, (button) -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }, new TranslatableComponent("menu.options")));
        this.addRenderableWidget(new ImageButton(w + 25, j, 20, 20, 0, 0, 20, Textures.ICON_QUIT, 32, 64, (button) -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = false;
            button.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            } else {
                this.minecraft.clearLevel();
            }

            TitleScreen titleScreen = new TitleScreen();
            if (bl) {
                this.minecraft.setScreen(titleScreen);
            } else {
                this.minecraft.setScreen(NewTitleScreen.INSTANCE);
            }
        }, new TranslatableComponent("menu.disconnect")));
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
