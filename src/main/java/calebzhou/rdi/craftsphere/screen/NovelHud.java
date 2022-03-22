package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.mixin.AccessItemRenderer;
import calebzhou.rdi.craftsphere.mixin.AccessMinecraft;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.math3.analysis.function.Min;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;


public class NovelHud extends GuiComponent{
    //要显示的格数，一共显示36个物品
    public static final int STACKS_DISPLAY = 36;
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(MODID,"textures/widgets.png");
    private static NovelHud INSTANCE = null;
    public float blitOffset=0f;
    public static NovelHud getInstance() {
        if(INSTANCE==null)
            INSTANCE = new NovelHud();
        return INSTANCE;
    }
    private NovelHud(){
    }
    //TODO 根据屏幕宽度自动翻页
    public void render(float f, PoseStack poseStack, Player player, int screenWidth, int screenHeight){
        if (player == null) return;
        //不显示mc原始的物品栏背景。
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            //RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ItemStack itemStack = player.getOffhandItem();
            HumanoidArm humanoidArm = player.getMainArm().getOpposite();
            //物品栏的起始宽度，这个值越大，往右串的越多
            int startWidth = 0/*screenWidth / 2*/;
            int widthOffset = 0;
            int blitOffset = this.getBlitOffset();
            /*this.setBlitOffset(-90);
            this.blit(poseStack, startWidth - widthOffset, screenHeight - 22, 0, 0, 182, 22);
            this.blit(poseStack, startWidth - widthOffset - 1 + player.getInventory().selected * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
            */if (!itemStack.isEmpty()) {
                if (humanoidArm == HumanoidArm.LEFT) {
                    this.blit(poseStack, startWidth - widthOffset - 29, screenHeight - 23, 24, 22, 29, 24);
                } else {
                    this.blit(poseStack, startWidth + widthOffset, screenHeight - 23, 53, 22, 29, 24);
                }
            }

            this.setBlitOffset(blitOffset);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            int m = 1;

            int stack;
            int width;
            int offsetHeight;
            //格子中间间隔，默认20
            int gapBetweenStack = 14;
            //要显示的格数
            int stacksToDisplay = STACKS_DISPLAY;
            for(stack = 0; stack < stacksToDisplay; ++stack) {
                width = startWidth - widthOffset + stack * gapBetweenStack+2;
                //这个参数越高(+)，越往下去 越低(-)，越往上去
                offsetHeight = screenHeight - 16;
                //如果是已经选中的物品格，他会比别的格子稍微高一些，高度
                if(player.getInventory().selected==stack)
                    this.renderSlot(width, offsetHeight-5, f, player, player.getInventory().items.get(stack), m++);
                else
                    this.renderSlot(width, offsetHeight, f, player, player.getInventory().items.get(stack), m++);
            }

            if (!itemStack.isEmpty()) {
                stack = screenHeight - 16 - 3;
                if (humanoidArm == HumanoidArm.LEFT) {
                    this.renderSlot(startWidth - widthOffset - 26, stack, f, player, itemStack, m++);
                } else {
                    this.renderSlot(startWidth + widthOffset + 10, stack, f, player, itemStack, m++);
                }
            }

            if (Minecraft.getInstance().options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
                float attackStrengthScale = Minecraft.getInstance().player.getAttackStrengthScale(0.0F);
                if (attackStrengthScale < 1.0F) {
                    width = screenHeight - 20;
                    offsetHeight = startWidth + widthOffset + 6;
                    if (humanoidArm == HumanoidArm.RIGHT) {
                        offsetHeight = startWidth - widthOffset - 22;
                    }

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    int scaledAtkStrength = (int)(attackStrengthScale * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(poseStack, offsetHeight, width, 0, 94, 18, 18);
                    this.blit(poseStack, offsetHeight, width + 18 - scaledAtkStrength, 18, 112 - scaledAtkStrength, 18, scaledAtkStrength);
                }
            }

            RenderSystem.disableBlend();
    }
    private void renderSlot(int width, int height, float f, Player player, ItemStack itemStack, int k) {
        if (!itemStack.isEmpty()) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            float g = (float)itemStack.getPopTime() - f;
            if (g > 0.0F) {
                float h = 1.0F + g / 5.0F;
                poseStack.pushPose();
                poseStack.translate((double)(width + 8), (double)(height + 12), 0.0D);
                poseStack.scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
                poseStack.translate((double)(-(width + 8)), (double)(-(height + 12)), 0.0D);
                RenderSystem.applyModelViewMatrix();
            }
            //渲染物品本身-----
            tryRenderGuiItem(player,itemStack,width,height,k,0);
            // Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(player, itemStack, width, height, k);

            //渲染物品本身-----
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (g > 0.0F) {
                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            //渲染物品数量数字一类的东西
            renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, width, height,null);
            //Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, width, height);
        }
    }
    private void tryRenderGuiItem(@Nullable LivingEntity livingEntity, ItemStack itemStack, int width, int height, int k, int l) {
        if (!itemStack.isEmpty()) {
            BakedModel bakedModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, (Level)null, livingEntity, k);
            this.blitOffset = bakedModel.isGui3d() ? this.blitOffset + 50.0F + (float)l : this.blitOffset + 50.0F;

            try {
                this.renderGuiItem(itemStack, width, height, bakedModel);
            } catch (Throwable var11) {
                CrashReport crashReport = CrashReport.forThrowable(var11, "Rendering item");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
                crashReportCategory.setDetail("Item Type", () -> {
                    return String.valueOf(itemStack.getItem());
                });
                crashReportCategory.setDetail("Item Damage", () -> {
                    return String.valueOf(itemStack.getDamageValue());
                });
                crashReportCategory.setDetail("Item NBT", () -> {
                    return String.valueOf(itemStack.getTag());
                });
                crashReportCategory.setDetail("Item Foil", () -> {
                    return String.valueOf(itemStack.hasFoil());
                });
                throw new ReportedException(crashReport);
            }

            this.blitOffset = bakedModel.isGui3d() ? this.blitOffset - 50.0F - (float)l : this.blitOffset - 50.0F;
        }
    }
    final float stackSizeScale=1.0f;

    public void render(ItemStack itemStack, ItemTransforms.TransformType transformType, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel) {
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            boolean bl2 = transformType == ItemTransforms.TransformType.GUI || transformType == ItemTransforms.TransformType.GROUND || transformType == ItemTransforms.TransformType.FIXED;
            if (bl2) {
                if (itemStack.is(Items.TRIDENT)) {
                    bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
                } else if (itemStack.is(Items.SPYGLASS)) {
                    bakedModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(new ModelResourceLocation("minecraft:spyglass#inventory"));
                }
            }

            bakedModel.getTransforms().getTransform(transformType).apply(bl, poseStack);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            if (!bakedModel.isCustomRenderer() && (!itemStack.is(Items.TRIDENT) || bl2)) {
                boolean bl3;
                if (transformType != ItemTransforms.TransformType.GUI && !transformType.firstPerson() && itemStack.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem)itemStack.getItem()).getBlock();
                    bl3 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    bl3 = true;
                }

                RenderType renderType = ItemBlockRenderTypes.getRenderType(itemStack, bl3);
                VertexConsumer vertexConsumer;
                if (itemStack.is(Items.COMPASS) && itemStack.hasFoil()) {
                    poseStack.pushPose();
                    PoseStack.Pose pose = poseStack.last();
                    if (transformType == ItemTransforms.TransformType.GUI) {
                        pose.pose().multiply(0.5F);
                    } else if (transformType.firstPerson()) {
                        pose.pose().multiply(0.75F);
                    }

                    if (bl3) {
                        vertexConsumer = getCompassFoilBufferDirect(multiBufferSource, renderType, pose);
                    } else {
                        vertexConsumer = getCompassFoilBuffer(multiBufferSource, renderType, pose);
                    }

                    poseStack.popPose();
                } else if (bl3) {
                    vertexConsumer = getFoilBufferDirect(multiBufferSource, renderType, true, itemStack.hasFoil());
                } else {
                    vertexConsumer = getFoilBuffer(multiBufferSource, renderType, true, itemStack.hasFoil());
                }

                this.renderModelLists(bakedModel, itemStack, i, j, poseStack, vertexConsumer);
            } else {
                ((AccessItemRenderer) Minecraft.getInstance().getItemRenderer()).getBlockEntityRenderer().renderByItem(itemStack, transformType, poseStack, multiBufferSource, i, j);
            }

            poseStack.popPose();
        }
    }
    final Random random = new Random();

    private void renderModelLists(BakedModel bakedModel, ItemStack itemStack, int i, int j, PoseStack poseStack, VertexConsumer vertexConsumer) {
        long l = 42L;
        random.setSeed(l);
        Direction[] var10 = Direction.values();
        int var11 = var10.length;

        for(int var12 = 0; var12 < var11; ++var12) {
            Direction direction = var10[var12];

            this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads((BlockState)null, direction, random), itemStack, i, j);
        }

        this.renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads((BlockState)null, (Direction)null, random), itemStack, i, j);
    }
    private void renderQuadList(PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, ItemStack itemStack, int i, int j) {
        boolean bl = !itemStack.isEmpty();
        PoseStack.Pose pose = poseStack.last();
        Iterator var9 = list.iterator();

        while(var9.hasNext()) {
            BakedQuad bakedQuad = (BakedQuad)var9.next();
            int k = -1;
            if (bl && bakedQuad.isTinted()) {
                k = ((AccessMinecraft)Minecraft.getInstance()).getItemColors().getColor(itemStack, bakedQuad.getTintIndex());
            }

            float f = (float)(k >> 16 & 255) / 255.0F;
            float g = (float)(k >> 8 & 255) / 255.0F;
            float h = (float)(k & 255) / 255.0F;
            vertexConsumer.putBulkData(pose, bakedQuad, f, g, h, i, j);
        }

    }

    protected void renderGuiItem(ItemStack itemStack, int width, int height, BakedModel bakedModel) {
        Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(width, height, 100.0F + this.blitOffset);
        poseStack.translate(8.0D, 8.0D, 0.0D);


        poseStack.scale(stackSizeScale, -stackSizeScale, stackSizeScale);

        float stackSize = 12f;
        poseStack.scale(stackSize, stackSize, stackSize);

        RenderSystem.applyModelViewMatrix();
        PoseStack poseStack2 = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean bl = !bakedModel.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }

        this.render(itemStack, ItemTransforms.TransformType.GUI, false, poseStack2, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (bl) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    //渲染物品数量一类的东西
    public void renderGuiItemDecorations(Font font, ItemStack itemStack, int width, int height, @Nullable String string) {
        //显示的数字往下串几个像素
        final int fontHeightOffset = 9;
        //显示的数字 往右串几个像素
        final int fontWidthOffset = 4;
        final float fontScale = 0.8f;
        if (!itemStack.isEmpty()) {
            PoseStack poseStack = new PoseStack();
            if (itemStack.getCount() != 1 || string != null) {
                String displayString = string == null ? String.valueOf(itemStack.getCount()) : string;
                poseStack.translate(0.0D, 0.0D, this.blitOffset + 200.0F);
               // poseStack.scale(fontScale,fontScale,fontScale);
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
                font.drawInBatch((String)displayString, (float)(width+fontWidthOffset/*+ 19 - 2 - font.width(displayString)*/), (float)(height +fontHeightOffset), 0x00FFFFFF, true, poseStack.last().pose(), bufferSource, false, 0, 0x00F000F0);
                bufferSource.endBatch();
            }

            if (itemStack.isBarVisible()) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.disableBlend();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tesselator.getBuilder();
                int k = itemStack.getBarWidth();
                int l = itemStack.getBarColor();
                this.fillRect(bufferBuilder, width + 2, height + 13, 13, 2, 0, 0, 0, 255);
                this.fillRect(bufferBuilder, width + 2, height + 13, k, 1, l >> 16 & 255, l >> 8 & 255, l & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            LocalPlayer localPlayer = Minecraft.getInstance().player;
            float f = localPlayer == null ? 0.0F : localPlayer.getCooldowns().getCooldownPercent(itemStack.getItem(), Minecraft.getInstance().getFrameTime());
            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                RenderSystem.disableTexture();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Tesselator tesselator2 = Tesselator.getInstance();
                BufferBuilder bufferBuilder2 = tesselator2.getBuilder();
                this.fillRect(bufferBuilder2, width, height + Mth.floor(16.0F * (1.0F - f)), 16, Mth.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

        }
    }
    private void fillRect(BufferBuilder bufferBuilder, int i, int j, int k, int l, int m, int n, int o, int p) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double)(i + 0), (double)(j + 0), 0.0D).color(m, n, o, p).endVertex();
        bufferBuilder.vertex((double)(i + 0), (double)(j + l), 0.0D).color(m, n, o, p).endVertex();
        bufferBuilder.vertex((double)(i + k), (double)(j + l), 0.0D).color(m, n, o, p).endVertex();
        bufferBuilder.vertex((double)(i + k), (double)(j + 0), 0.0D).color(m, n, o, p).endVertex();
        bufferBuilder.end();
        BufferUploader.end(bufferBuilder);
    }
    public static VertexConsumer getCompassFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glint()), pose.pose(), pose.normal()), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getCompassFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, PoseStack.Pose pose) {
        return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(multiBufferSource.getBuffer(RenderType.glintDirect()), pose.pose(), pose.normal()), multiBufferSource.getBuffer(renderType));
    }

    public static VertexConsumer getFoilBuffer(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        if (bl2) {
            return Minecraft.useShaderTransparency() && renderType == Sheets.translucentItemSheet() ? VertexMultiConsumer.create(multiBufferSource.getBuffer(RenderType.glintTranslucent()), multiBufferSource.getBuffer(renderType)) : VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glint() : RenderType.entityGlint()), multiBufferSource.getBuffer(renderType));
        } else {
            return multiBufferSource.getBuffer(renderType);
        }
    }

    public static VertexConsumer getFoilBufferDirect(MultiBufferSource multiBufferSource, RenderType renderType, boolean bl, boolean bl2) {
        return bl2 ? VertexMultiConsumer.create(multiBufferSource.getBuffer(bl ? RenderType.glintDirect() : RenderType.entityGlintDirect()), multiBufferSource.getBuffer(renderType)) : multiBufferSource.getBuffer(renderType);
    }

    public void renderExperienceBar(){}
}
