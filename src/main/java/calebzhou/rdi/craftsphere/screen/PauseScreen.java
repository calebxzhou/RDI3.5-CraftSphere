package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.texture.Textures;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class PauseScreen extends Screen {
    private final boolean showMenu;
    public PauseScreen(boolean showMenu) {
        super(new LiteralText("菜单"));
        this.showMenu=true;
    }

    @Override
    protected void init() {
        if(showMenu)
            initWidgets();
    }
    private void initWidgets(){
        int w = this.width /2 - 10;
        int j = this.height / 2 - 50;
        this.addDrawableChild(new TexturedButtonWidget(w - 25, j, 20, 20, 0,0,20, Textures.ICON_CONTINUE,32,64, (button) -> {
            this.client.setScreen(null);
            this.client.mouse.lockCursor();
        },  new TranslatableText("menu.returnToGame")));

        this.addDrawableChild(new TexturedButtonWidget(w , j, 20, 20, 0,0,20, Textures.ICON_SETTINGS,32,64, (button) -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }, new TranslatableText("menu.options")));
        this.addDrawableChild(new TexturedButtonWidget(w + 25, j, 20, 20, 0, 0, 20, Textures.ICON_QUIT, 32, 64, (button) -> {
            /*if(this.client.isInSingleplayer())
                this.client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
            else
                this.client.disconnect();*/
            this.client.setScreen(new TitleScreen());
        }, new TranslatableText("menu.disconnect")));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        if (this.showMenu) {
            this.renderBackground(matrices);
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
        } else {
            drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }
}
