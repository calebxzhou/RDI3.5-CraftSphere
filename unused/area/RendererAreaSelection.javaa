package calebzhou.rdi.craftsphere.module.area;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RendererAreaSelection implements DebugRenderer.SimpleDebugRenderer{
    public static final RendererAreaSelection INSTANCE = new RendererAreaSelection(Minecraft.getInstance());
    private final Minecraft client;
    public RendererAreaSelection(Minecraft client) {
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

    private static void drawShapeOutline(PoseStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
        PoseStack.Pose entry = matrices.last();
        voxelShape.forAllEdges((k, l, m, n, o, p) -> {
            float q = (float)(n - k);
            float r = (float)(o - l);
            float s = (float)(p - m);
            float t = Mth.sqrt(q * q + r * r + s * s);
            vertexConsumer.vertex(entry.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).color(g, h, i, j).normal(entry.normal(), q /= t, r /= t, s /= t).endVertex();
            vertexConsumer.vertex(entry.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).color(g, h, i, j).normal(entry.normal(), q, r, s).endVertex();
        });
    }
    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, double cameraX, double cameraY, double cameraZ) {
        BlockPos p1 = ModelAreaSelection.INSTANCE.getP1().orElseThrow();
        BlockPos p2 = ModelAreaSelection.INSTANCE.getP2().orElseThrow();
        AABB box = new AABB(p1,p2);
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderType.lines());
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Entity entity = this.client.gameRenderer.getMainCamera().getEntity();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        double dMinY = (double)box.minY - cameraY;
        double dMaxY = (double)box.maxY - cameraY +1;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        double dX = box.minX - cameraX;
        double dZ = box.minZ - cameraZ;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int lineColor;
        int i;
        int lenZ = (int) box.getZsize()+1; /*+ 16.0*/
        int lenX = (int) box.getXsize()+1; /*+ 16.0*/
        for (i = 0; i < lenX; ++i) {
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            //p1边竖线 下
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ).color(RED).endVertex();
            //p1边竖线 上
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ).color(ORANGE).endVertex();
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ+lenZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            //p2边竖线 下
            bufferBuilder.vertex(dX + (double)i, dMinY, dZ+lenZ).color(YELLOW).endVertex();
            //p2边竖线 上
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ+lenZ).color(LIME).endVertex();
            bufferBuilder.vertex(dX + (double)i, dMaxY, dZ+lenZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();


        }
        for (i = 0; i < lenZ; ++i) {
            bufferBuilder.vertex(dX, dMinY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(dX, dMinY, dZ + (double)i).color(AQUA).endVertex();
            bufferBuilder.vertex(dX, dMaxY, dZ + (double)i).color(LIGHT_BLUE).endVertex();
            bufferBuilder.vertex(dX, dMaxY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(dX+lenX, dMinY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(dX+lenX, dMinY, dZ + (double)i).color(DARK_BLUE).endVertex();
            bufferBuilder.vertex(dX+lenX, dMaxY, dZ + (double)i).color(PURPLE).endVertex();
            bufferBuilder.vertex(dX+lenX, dMaxY, dZ + (double)i).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (i = (int) box.minY; i <= box.maxY+1; ++i) {
            double j2 = (double)i - cameraY;
            bufferBuilder.vertex(dX, j2, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(dX, j2, dZ).color(PINK).endVertex();
            bufferBuilder.vertex(dX, j2, dZ+lenZ).color(GREEN).endVertex();
            bufferBuilder.vertex(dX+lenX, j2, dZ+lenZ).color(DARK_LIGHT_BLUE).endVertex();
            bufferBuilder.vertex(dX+lenX, j2, dZ).color(DARK_GREEN).endVertex();
            bufferBuilder.vertex(dX, j2, dZ).color(DARK_PURPLE).endVertex();
            bufferBuilder.vertex(dX, j2, dZ).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        tessellator.end();
        RenderSystem.lineWidth(2.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }


    //在游戏画面的右上方显示区域选择信息
    public static void renderSelectionHud(ClientLevel clientWorld) {


    }
}
