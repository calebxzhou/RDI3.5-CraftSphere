package calebzhou.rdi.craftsphere.module.area;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

public class RendererAreaSelection implements DebugRenderer.Renderer{
    public static final RendererAreaSelection INSTANCE = new RendererAreaSelection(MinecraftClient.getInstance());
    private final MinecraftClient client;
    public RendererAreaSelection(MinecraftClient client) {
        this.client = client;
    }
    public static final int RED = 0xFFff0000;
    public static final int ORANGE = 0xFFff8800;
    public static final int YELLOW = 0xFFffd500;
    public static final int LIME = 0xFFa6ff00;
    public static final int AQUA = 0xFF00ffe5;
    public static final int LIGHT_BLUE = 0xFF00b7ff;
    public static final int DARK_LIGHT_BLUE = 0xFF3f7475;
    public static final int DARK_BLUE  = 0xFF1900ff;
    public static final int PURPLE  = 0xFF8c00ff;
    public static final int DARK_PURPLE  = 0xFF561857;
    private static final int PINK = 0xFFe342f5;
    private static final int GREEN = 0xFF00ff22;
    private static final int DARK_GREEN = 0xFF035e00;

    private static void drawShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        MatrixStack.Entry entry = matrices.peek();
        voxelShape.forEachEdge((k, l, m, n, o, p) -> {
            float q = (float)(n - k);
            float r = (float)(o - l);
            float s = (float)(p - m);
            float t = MathHelper.sqrt(q * q + r * r + s * s);
            vertexConsumer.vertex(entry.getPositionMatrix(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(entry.getNormalMatrix(), q /= t, r /= t, s /= t).next();
            vertexConsumer.vertex(entry.getPositionMatrix(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(entry.getNormalMatrix(), q, r, s).next();
        });
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        BlockPos p1 = ModelAreaSelection.INSTANCE.getP1().orElseThrow();
        BlockPos p2 = ModelAreaSelection.INSTANCE.getP2().orElseThrow();
        Box box = new Box(p1,p2);
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getLines());
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Entity entity = this.client.gameRenderer.getCamera().getFocusedEntity();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        double dMinY = (double)box.minY - cameraY;
        double dMaxY = (double)box.maxY - cameraY +1;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        double dX = box.minX - cameraX;
        double dZ = box.minZ - cameraZ;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        int lineColor;
        int i;
        int lenZ = (int) box.getZLength()+1; /*+ 16.0*/
        int lenX = (int) box.getXLength()+1; /*+ 16.0*/
        for (i = 0; i < lenX; ++i) {
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            //p1边竖线 下
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ).color(RED).next();
            //p1边竖线 上
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ).color(ORANGE).next();
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ+lenZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            //p2边竖线 下
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ+lenZ).color(YELLOW).next();
            //p2边竖线 上
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ+lenZ).color(LIME).next();
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ+lenZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        for (i = 0; i < lenZ; ++i) {
            bufferBuilder.vertex(dX, dMinY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(dX, dMinY, dZ + (double)i).color(AQUA).next();
            bufferBuilder.vertex(dX, dMaxY, dZ + (double)i).color(LIGHT_BLUE).next();
            bufferBuilder.vertex(dX, dMaxY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(dX+lenX, dMinY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(dX+lenX, dMinY, dZ + (double)i).color(DARK_BLUE).next();
            bufferBuilder.vertex(dX+lenX, dMaxY, dZ + (double)i).color(PURPLE).next();
            bufferBuilder.vertex(dX+lenX, dMaxY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        for (i = (int) box.minY; i <= box.maxY+1; ++i) {
            double j2 = (double)i - cameraY;
            bufferBuilder.vertex(dX, j2, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
            bufferBuilder.vertex(dX, j2, dZ).color(PINK).next();
            bufferBuilder.vertex(dX, j2, dZ+lenZ).color(GREEN).next();
            bufferBuilder.vertex(dX+lenX, j2, dZ+lenZ).color(DARK_LIGHT_BLUE).next();
            bufferBuilder.vertex(dX+lenX, j2, dZ).color(DARK_GREEN).next();
            bufferBuilder.vertex(dX, j2, dZ).color(DARK_PURPLE).next();
            bufferBuilder.vertex(dX, j2, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).next();
        }
        tessellator.draw();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}
