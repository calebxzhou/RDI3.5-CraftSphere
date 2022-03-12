package calebzhou.rdi.craftsphere.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class BasicScreen extends Screen {
    protected final Screen parentScreen;
    private final boolean exitable;
    private final boolean transparent;
    protected BasicScreen(String title, Screen parentScreen, boolean exitable, boolean transparent) {
        super(new TextComponent(title));
        this.parentScreen = parentScreen;
        this.exitable = exitable;
        this.transparent = transparent;
    }

    @Override
    public void tick() {
        if(exitable){
            long handle = Minecraft.getInstance().getWindow().getWindow();
            if(InputConstants.isKeyDown(handle,InputConstants.KEY_ESCAPE)){
                Minecraft.getInstance().setScreen(parentScreen);
            }
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        if(!transparent){
            this.renderBackground(matrices);
        }
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 40, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }
    public void close() {
        this.minecraft.setScreen(this.parentScreen);
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }
}
