package calebzhou.rdi.craftsphere.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class BasicScreen extends Screen {
    protected final Screen parentScreen;
    private final boolean exitable;
    private final boolean transparent;
    protected BasicScreen(String title, Screen parentScreen, boolean exitable, boolean transparent) {
        super(new LiteralText(title));
        this.parentScreen = parentScreen;
        this.exitable = exitable;
        this.transparent = transparent;
    }

    @Override
    public void tick() {
        if(exitable){
            long handle = MinecraftClient.getInstance().getWindow().getHandle();
            if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_ESCAPE)){
                MinecraftClient.getInstance().setScreen(parentScreen);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if(!transparent){
            this.renderBackground(matrices);
        }
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
    public void onClose() {
        this.client.setScreen(this.parentScreen);
    }

    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
    }
}
