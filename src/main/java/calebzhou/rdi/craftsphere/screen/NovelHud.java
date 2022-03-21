package calebzhou.rdi.craftsphere.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.math3.analysis.function.Min;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;


public class NovelHud extends GuiComponent{
    //要显示的格数，一共显示36个物品
    public static final int STACKS_DISPLAY = 36;
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation(MODID,"textures/widgets.png");
    private static NovelHud INSTANCE = null;

    public static NovelHud getInstance() {
        if(INSTANCE==null)
            INSTANCE = new NovelHud();
        return INSTANCE;
    }
    private NovelHud(){
    }
    
    public void render(float f, PoseStack poseStack, Player player, int screenWidth, int screenHeight){
        if (player == null) return;
        //不显示mc原始的物品栏背景。
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
            ItemStack itemStack = player.getOffhandItem();
            HumanoidArm humanoidArm = player.getMainArm().getOpposite();
            //物品栏的起始宽度，这个值越大，往右串的越多
            int startWidth = 0/*screenWidth / 2*/;
            int widthOffset = 0;
            int blitOffset = this.getBlitOffset();
            this.setBlitOffset(-90);
            this.blit(poseStack, startWidth - widthOffset, screenHeight - 22, 0, 0, 182, 22);
            this.blit(poseStack, startWidth - widthOffset - 1 + player.getInventory().selected * 20, screenHeight - 22 - 1, 0, 22, 24, 22);
            if (!itemStack.isEmpty()) {
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
            int o;
            int offsetWidth;
            int gapBetweenStack = 16;
            //要显示的格数
            int stacksToDisplay = STACKS_DISPLAY;
            for(stack = 0; stack < stacksToDisplay; ++stack) {
                o = startWidth - widthOffset + stack * gapBetweenStack;
                offsetWidth = screenHeight - 16 - 3;
                this.renderSlot(o, offsetWidth, f, player, player.getInventory().items.get(stack), m++);
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
                    o = screenHeight - 20;
                    offsetWidth = startWidth + widthOffset + 6;
                    if (humanoidArm == HumanoidArm.RIGHT) {
                        offsetWidth = startWidth - widthOffset - 22;
                    }

                    RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
                    int scaledAtkStrength = (int)(attackStrengthScale * 19.0F);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(poseStack, offsetWidth, o, 0, 94, 18, 18);
                    this.blit(poseStack, offsetWidth, o + 18 - scaledAtkStrength, 18, 112 - scaledAtkStrength, 18, scaledAtkStrength);
                }
            }

            RenderSystem.disableBlend();
    }
    private void renderSlot(int i, int j, float f, Player player, ItemStack itemStack, int k) {
        if (!itemStack.isEmpty()) {
            PoseStack poseStack = RenderSystem.getModelViewStack();
            float g = (float)itemStack.getPopTime() - f;
            if (g > 0.0F) {
                float h = 1.0F + g / 5.0F;
                poseStack.pushPose();
                poseStack.translate((double)(i + 8), (double)(j + 12), 0.0D);
                poseStack.scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
                poseStack.translate((double)(-(i + 8)), (double)(-(j + 12)), 0.0D);
                RenderSystem.applyModelViewMatrix();
            }
            //渲染物品本身
            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(player, itemStack, i, j, k);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            if (g > 0.0F) {
                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
            }
            //渲染物品数量数字一类的东西
            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, i, j);
        }
    }
}
