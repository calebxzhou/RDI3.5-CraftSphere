package calebzhou.rdi.craftsphere.dialog;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InfoToast implements Toast {
    private static final long DURATION = 3800L;
    private Component title;
    private List<FormattedCharSequence> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;
    private int textColor;
    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return 20;
    }

    public InfoToast(Component title, List<FormattedCharSequence> lines, int width, int textColor) {
        this.title = title;
        this.lines = lines;
        this.width = width;
        this.textColor=textColor;
    }


    public static InfoToast create(Component title, @Nullable Component description) {
        return create(title, description,0xFF_FFFF00);
    }
    public static InfoToast create(Component title, @Nullable Component description,int color) {
        Font textRenderer = Minecraft.getInstance().font;
        List<FormattedCharSequence> list = textRenderer.split(description==null?new TextComponent(""):description, 200);
        if(description==null){
            list=new ArrayList<>();
        }
        int i = Math.max(200, list.stream().mapToInt(textRenderer::width).max().orElse(200));
        if(title.getString().length()>68)
            title = new TextComponent(title.getString(68));
        return new InfoToast(title, list, i+30,color);
    }

    @Override
    public Visibility render(PoseStack matrices, ToastComponent manager, long startTime) {
        return draw(matrices, manager, startTime,textColor);
    }
    public Visibility draw(PoseStack matrices, ToastComponent manager, long startTime,int textColor) {
        int k;
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int width = this.width();
        int j = 12;
        if (width == 160 && this.lines.size() <= 1) {
            manager.blit(matrices, 0, 0, 0, 64, width, this.height());
        } else {
            k = this.height() + Math.max(0, this.lines.size() - 1) * 12;
            int l = 28;
            int m = Math.min(4, k - 28);
            this.drawPart(matrices, manager, width, 0, 0, 28);
            for (int n = 28; n < k - m; n += 10) {
                this.drawPart(matrices, manager, width, 16, n, Math.min(16, k - n - m));
            }
            this.drawPart(matrices, manager, width, 32 - m, k - m, m);
        }
        if (this.lines == null || this.lines.isEmpty()) {
            manager.getMinecraft().font.draw(matrices, this.title, 18.0f, 12.0f, textColor);
        } else {
            manager.getMinecraft().font.draw(matrices, this.title, 18.0f, 7.0f, textColor);
            for (k = 0; k < this.lines.size(); ++k) {
                manager.getMinecraft().font.draw(matrices, this.lines.get(k), 18.0f, (float)(18 + k * 12), -1);
            }
        }
        return startTime - this.startTime < DURATION ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void drawPart(PoseStack matrices, ToastComponent manager, int width, int textureV, int y, int height) {
        int i = textureV == 0 ? 20 : 5;
        int j = Math.min(60, width - i);
        manager.blit(matrices, 0, y, 0, 64 + textureV, i, height);
        for (int k = i; k < width - j; k += 64) {
            manager.blit(matrices, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
        }
        manager.blit(matrices, width - j, y, 160 - j, 64 + textureV, j, height);
    }
    private static ImmutableList<FormattedCharSequence> getTextAsList(@Nullable Component text) {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.getVisualOrderText());
    }

    public void setContent(Component title, @Nullable Component description) {
        this.title = title;
        this.lines = getTextAsList(description);
        this.justUpdated = true;
    }
}
