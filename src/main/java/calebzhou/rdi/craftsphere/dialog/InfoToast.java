package calebzhou.rdi.craftsphere.dialog;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InfoToast implements Toast {
    private static final long DURATION = 1000L;
    private Text title;
    private List<OrderedText> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;
    private int textColor;
    @Override
    public int getWidth() {
        return width;
    }

    public InfoToast(Text title, List<OrderedText> lines, int width,int textColor) {
        this.title = title;
        this.lines = lines;
        this.width = width;
        this.textColor=textColor;
    }


    public static InfoToast create(Text title, @Nullable Text description) {
        return create(title, description,0xFF_FFFF00);
    }
    public static InfoToast create(Text title, @Nullable Text description,int color) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<OrderedText> list = textRenderer.wrapLines(description==null?new LiteralText(""):description, 200);
        if(description==null){
            list=new ArrayList<>();
        }
        int i = Math.max(200, list.stream().mapToInt(textRenderer::getWidth).max().orElse(200));
        return new InfoToast(title, list, i + 30,color);
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        return draw(matrices, manager, startTime,textColor);
    }
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime,int textColor) {
        int k;
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.getWidth();
        int j = 12;
        if (i == 160 && this.lines.size() <= 1) {
            manager.drawTexture(matrices, 0, 0, 0, 64, i, this.getHeight());
        } else {
            k = this.getHeight() + Math.max(0, this.lines.size() - 1) * 12;
            int l = 28;
            int m = Math.min(4, k - 28);
            this.drawPart(matrices, manager, i, 0, 0, 28);
            for (int n = 28; n < k - m; n += 10) {
                this.drawPart(matrices, manager, i, 16, n, Math.min(16, k - n - m));
            }
            this.drawPart(matrices, manager, i, 32 - m, k - m, m);
        }
        if (this.lines == null || this.lines.isEmpty()) {
            manager.getClient().textRenderer.draw(matrices, this.title, 18.0f, 12.0f, textColor);
        } else {
            manager.getClient().textRenderer.draw(matrices, this.title, 18.0f, 7.0f, textColor);
            for (k = 0; k < this.lines.size(); ++k) {
                manager.getClient().textRenderer.draw(matrices, this.lines.get(k), 18.0f, (float)(18 + k * 12), -1);
            }
        }
        return startTime - this.startTime < DURATION ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void drawPart(MatrixStack matrices, ToastManager manager, int width, int textureV, int y, int height) {
        int i = textureV == 0 ? 20 : 5;
        int j = Math.min(60, width - i);
        manager.drawTexture(matrices, 0, y, 0, 64 + textureV, i, height);
        for (int k = i; k < width - j; k += 64) {
            manager.drawTexture(matrices, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
        }
        manager.drawTexture(matrices, width - j, y, 160 - j, 64 + textureV, j, height);
    }
    private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text) {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    public void setContent(Text title, @Nullable Text description) {
        this.title = title;
        this.lines = getTextAsList(description);
        this.justUpdated = true;
    }
}
