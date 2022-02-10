package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.model.ApiResponse;
import calebzhou.rdi.craftsphere.screen.IslandScreen;
import calebzhou.rdi.craftsphere.sound.TitleScreenSound;
import calebzhou.rdi.craftsphere.texture.Textures;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.RotatingCubeMapRenderer;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static boolean isPlayingMusic = false;
    @Shadow public abstract void removed();

    @Shadow private long backgroundFadeStart;

    @Shadow @Final private boolean doBackgroundFade;

    @Shadow @Final private RotatingCubeMapRenderer backgroundRenderer;

    @Shadow @Final private static Identifier PANORAMA_OVERLAY;
    private int frames=0;
    // private static String weather="正在载入天气预报";
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    //不显示左下角和右下角的内容
    /**
     * @author
     */
    @Overwrite
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {

        if (backgroundFadeStart == 0L && doBackgroundFade) {
            this.backgroundFadeStart = Util.getMeasuringTimeMs();
        }
        float f = this.doBackgroundFade ? (float)(Util.getMeasuringTimeMs() - this.backgroundFadeStart) / 1000.0f : 1.0f;
        backgroundRenderer.render(delta, MathHelper.clamp(f, 0.0f, 1.0f));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        //RenderSystem.setShaderTexture(0, Textures.TITLE_SCREEN);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.doBackgroundFade ? (float)MathHelper.ceil(MathHelper.clamp(f, 0.0f, 1.0f)) : 1.0f);
        TitleScreen.drawTexture(matrices, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        float g = this.doBackgroundFade ? MathHelper.clamp(f - 1.0f, 0.0f, 1.0f) : 1.0f;
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
       // this.textRenderer.draw(matrices, weather, 0, this.height - 25, 0x00000000);

       // RenderSystem.setShaderTexture(0, Textures.TITLE_LOGO);
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
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
        /*if(!isPlayingMusic){
            MinecraftClient.getInstance().getSoundManager().play(new TitleScreenSound());
            System.out.println("play music");
            isPlayingMusic=true;
        }*/
        ++frames;
    }
    /**
     * @author
     */
    @Overwrite
    public void init() {
        MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,0);

        this.addDrawableChild(new TexturedButtonWidget(this.width -10, this.height-10, 10, 10, 0,0,20,Textures.ICON_SETTINGS,16,32, (button) -> {
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
    }
    /**
     * @author
     * 去掉演示模式
     */
    @Overwrite
    private void initWidgetsDemo(int y, int spacingY) {}
    //去掉单人模式按钮
    @Redirect(method = "Lnet/minecraft/client/gui/screen/TitleScreen;initWidgetsNormal(II)V",
            at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
                    ordinal = 0))
    private Element noSingleButton(TitleScreen instance, Element element){
        return element;
    }
    //多人模式不显示按钮
    @Redirect(method = "Lnet/minecraft/client/gui/screen/TitleScreen;initWidgetsNormal(II)V",
    at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",ordinal = 1))
    private Element multiBtn(TitleScreen instance, Element element,int y, int spacingY){

        return element;
    }
    //去掉领域服
    @Inject(method = "Lnet/minecraft/client/gui/screen/TitleScreen;initWidgetsNormal(II)V",
            at=@At(value = "INVOKE",target = "Lnet/minecraft/client/gui/screen/TitleScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;",
            ordinal = 2), cancellable = true)
    private void noRealmsButton(CallbackInfo ci){
        ci.cancel();
    }

    /**
     * @author
     * 按回车键进入服务器,按shift+回车进入单人
     */
    @Overwrite
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
            //MinecraftClient.getInstance().setScreen(new IslandScreen(this));
            ServerAddress address = new ServerAddress("test3.davisoft.cn",26088);
            ServerInfo info = new ServerInfo("rdi-celetech3",address.getAddress(),false);
            ConnectScreen.connect(this,MinecraftClient.getInstance(),address,info);
            MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MUSIC,1);
                //this.cli    ent.setScreen(new MultiplayerScreen(this));
        }



    }
}
@Mixin(MinecraftClient.class)
class TitleScreenClientMixin{
    /**
     * @author
     * 不检查是否允许多人游戏
     */
    @Overwrite
    public boolean isMultiplayerEnabled() {
        return true;
    }
    /**
     * @author
     * 不开启领域服
     */
    @Overwrite
    public boolean isRealmsEnabled() {
        return false;
    }
}
