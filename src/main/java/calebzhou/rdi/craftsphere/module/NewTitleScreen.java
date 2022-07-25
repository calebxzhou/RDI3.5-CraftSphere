package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.RdiConfigure;
import calebzhou.rdi.craftsphere.texture.Textures;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public class NewTitleScreen extends Screen {
    private static boolean isPlayingMusic = false;
    public static final CubeMap PANORAMA_CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private PanoramaRenderer backgroundRenderer = new PanoramaRenderer(PANORAMA_CUBE_MAP);
    private long backgroundFadeStart;
    private int frames=0;

    public static final NewTitleScreen INSTANCE = new NewTitleScreen();
    private NewTitleScreen() {
        super(Component.literal("主界面"));
    }
    public boolean shouldCloseOnEsc() {
        return false;
    }
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {

        if (backgroundFadeStart == 0L ) {
            this.backgroundFadeStart = Util.getMillis();
        }
        float f = (float)(Util.getMillis() - this.backgroundFadeStart) / 1000.0f;
        backgroundRenderer.render(delta, Mth.clamp(f, 0.0f, 1.0f));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderTexture(0, Textures.TITLE_SCREEN);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float)Mth.ceil(Mth.clamp(f, 0.0f, 1.0f)));
        TitleScreen.blit(matrices, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        float g = Mth.clamp(f - 1.0f, 0.0f, 1.0f);
        int l = Mth.ceil(g * 255.0f) << 24;
        if ((l & 0xFC000000) == 0) {
            return;
        }
        blit(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        //if(frames>= 35){
            this.font.drawShadow(matrices, "按下 Enter", (this.width/2.0f)-30, this.height - 50, 0x00FFFFFF);
        /*}
        if(frames>=70){
            frames=0;
        }*/
        int j = this.width / 2 - 137;

        int lz = Mth.ceil(g * 255.0F) << 24;
        if ((lz & -67108864) != 0) {

            Iterator var12 = this.children().iterator();

            while(var12.hasNext()) {
                GuiEventListener element = (GuiEventListener)var12.next();
                if (element instanceof AbstractWidget) {
                    ((AbstractWidget)element).setAlpha(1.0F);
                }
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
        ++frames;
        if(RdiConfigure.getConfig().playBackgroundMusicOnTitleScreen){
            if(!isPlayingMusic){
                GameMusic.playOnTitle();
                isPlayingMusic =true;
            }

        }
    }
    public void tick() {

        long handle = Minecraft.getInstance().getWindow().getWindow();
        /*if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_F1)){
            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(this));
            return;
        }*/
        if(InputConstants.isKeyDown(handle, InputConstants.KEY_F3)){
            Minecraft.getInstance().setScreen(new SelectWorldScreen(this));
            return;
        }
        if(InputConstants.isKeyDown(handle,InputConstants.KEY_RETURN)){
            ConnectScreen.startConnecting(this,Minecraft.getInstance(), ExampleMod.SERVER_ADDRESS,ExampleMod.SERVER_INFO);
        }



    }
    public void init() {
        this.addRenderableWidget(new ImageButton(this.width -10, this.height-10, 10, 10, 0,0,20, Textures.ICON_SETTINGS,16,32, (button) -> {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }, Component.translatable("menu.options")));
        /*this.addDrawableChild(new TexturedButtonWidget(this.width-40, j, 20, 20, 0, 0, 20, new Identifier("textures/gui/accessibility.png"), 32, 64, (button) -> {
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
        }, new TranslatableText("narrator.button.accessibility")));*/
        this.addRenderableWidget(new ImageButton(0, this.height-10, 10, 10, 0, 0, 20, Textures.ICON_MODMENU, 16, 32, (button) -> {
            try {
                this.minecraft.setScreen((Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(Screen.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                DialogUtils.showError("必须安装ModMenu模组以使用本功能！！");
                e.printStackTrace();
            }
        }, Component.translatable("Mods")));
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC,0);

    }
}
