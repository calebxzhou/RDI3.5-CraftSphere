package calebzhou.rdi.craftsphere.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;

public class JoinIslandScreen extends BasicScreen {
    private TextFieldWidget textFieldWidget;
    protected JoinIslandScreen(IslandScreen screen) {
        super("加入朋友的空岛",screen,true,false);
    }

    @Override
    protected void init() {
        this.client.keyboard.setRepeatEvents(true);
        super.init();
        this.textFieldWidget = new TextFieldWidget(this.textRenderer,50,40,this.width-100,20,this.title);
        this.textFieldWidget.setMaxLength(1230);
        this.addSelectableChild(this.textFieldWidget);

        this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new LiteralText("立刻加入"), (button) -> {
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (button) -> {
            this.client.setScreen(this.parentScreen);
        }));
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.textFieldWidget.getText();
        this.init(client, width, height);
        this.textFieldWidget.setText(string);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.textFieldWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        this.textFieldWidget.tick();
        super.tick();
    }
}
