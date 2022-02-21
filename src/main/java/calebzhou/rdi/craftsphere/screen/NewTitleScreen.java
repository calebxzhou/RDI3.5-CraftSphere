package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.texture.Textures;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.CubeMapRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Iterator;

public class NewTitleScreen extends Screen {
    public static final CubeMapRenderer PANORAMA_CUBE_MAP = new CubeMapRenderer(new Identifier("textures/gui/title/background/panorama"));
    private static final Identifier PANORAMA_OVERLAY = new Identifier("textures/gui/title/background/panorama_overlay.png");
    private RotatingCubeMapRenderer backgroundRenderer = new RotatingCubeMapRenderer(PANORAMA_CUBE_MAP);
    private long backgroundFadeStart;
    private int frames=0;

    public NewTitleScreen() {
        super(new LiteralText("主界面"));
    }
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        if (backgroundFadeStart == 0L ) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        float f = (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0f;
        backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0f, 1.0f));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderTexture(0, Textures.TITLE_SCREEN);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (float)MathHelper.ceil(MathHelper.clamp(f, 0.0f, 1.0f)));
        TitleScreen.drawTexture(matrices, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        float g = MathHelper.clamp(f - 1.0f, 0.0f, 1.0f);
        int l = MathHelper.ceil(g * 255.0f) << 24;
        if ((l & 0xFC000000) == 0) {
            return;
        }
        drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        if(frames>= 35){
            this.textRenderer.drawWithShadow(matrices, "按下 Enter", (this.width/2.0f)-30, this.height - 50, 0x00FFFFFF);
        }
        if(frames>=70){
            frames=0;
        }
        int j = this.width / 2 - 137;

        int lz = MathHelper.ceil(g * 255.0F) << 24;
        if ((lz & -67108864) != 0) {

            Iterator var12 = this.children().iterator();

            while(var12.hasNext()) {
                Element element = (Element)var12.next();
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget)element).setAlpha(1.0F);
                }
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
        ++frames;
    }
    public void tick() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_F1)){
            MinecraftClient.getInstance().setScreen(new MultiplayerScreen(this));
            return;
        }
        if(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_F3)){
            MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
            return;
        }
        if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_ENTER)){
            ServerAddress address = new ServerAddress("test3.davisoft.cn",26088);
            ServerInfo info = new ServerInfo("rdi-celetech3",address.getAddress(),false);
            ConnectScreen.connect(this,MinecraftClient.getInstance(),address,info);
        }



    }
    public void init() {
        MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,0);

        this.addDrawableChild(new TexturedButtonWidget(this.width -10, this.height-10, 10, 10, 0,0,20, Textures.ICON_SETTINGS,16,32, (button) -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }, new TranslatableText("menu.options")));
        /*this.addDrawableChild(new TexturedButtonWidget(this.width-40, j, 20, 20, 0, 0, 20, new Identifier("textures/gui/accessibility.png"), 32, 64, (button) -> {
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
        }, new TranslatableText("narrator.button.accessibility")));*/
        this.addDrawableChild(new TexturedButtonWidget(0, this.height-10, 10, 10, 0, 0, 20, Textures.ICON_MODMENU, 16, 32, (button) -> {
            try {
                this.client.setScreen((Screen) Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(Screen.class).newInstance(this));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                DialogUtils.showError("必须安装ModMenu模组以使用本功能！！");
                e.printStackTrace();
            }
        }, new TranslatableText("Mods")));
        MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,1);
    }
}
