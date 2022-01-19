package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.texture.Textures;
import calebzhou.rdi.craftsphere.util.HttpUtils;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    private static String weather="正在载入天气预报";
    protected TitleScreenMixin(Text title) {
        super(title);
    }
    //不显示左下角和右下角的内容
    /**
     * @author
     */
    @Overwrite
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Textures.TITLE_SCREEN);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        drawTexture(matrices, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        this.textRenderer.draw(matrices, "按 Enter", (this.width/2.0f), this.height - 50, 0x00000000);
        this.textRenderer.draw(matrices, weather, (this.width/2.0f), this.height/2 - 50, 0x00000000);

        RenderSystem.setShaderTexture(0, Textures.TITLE_LOGO);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
        int j = this.width / 2 - 137;
        this.drawWithOutline(j, 30, (x, y) -> {
            this.drawTexture(matrices, x, y, 0, 0, 155, 44);
            this.drawTexture(matrices, x + 155, y, 0, 45, 155, 44);
        });
         float g = 1.0F;
        int l = MathHelper.ceil(g * 255.0F) << 24;
        if ((l & -67108864) != 0) {

            Iterator var12 = this.children().iterator();

            while(var12.hasNext()) {
                Element element = (Element)var12.next();
                if (element instanceof ClickableWidget) {
                    ((ClickableWidget)element).setAlpha(g);
                }
            }
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
    /**
     * @author
     */
    @Overwrite
    public void init() {
        ThreadPool.newThread(()->{
            String request = HttpUtils.sendRequest("GET", "getWeather");
            this.weather=request;
        });
        int j = this.height-25;
        /*this.addDrawableChild(new TexturedButtonWidget(this.width / 2 - 124, j + 72 + 12, 20, 20, 0, 106, 20, ButtonWidget.WIDGETS_TEXTURE, 256, 256, (button) -> {
            this.client.setScreen(new LanguageOptionsScreen(this, this.client.options, this.client.getLanguageManager()));
        }, new TranslatableText("narrator.button.language")));*/
        this.addDrawableChild(new TexturedButtonWidget(this.width -25, j, 20, 20, 0,0,20,Textures.ICON_SETTINGS,32,64, (button) -> {
            this.client.setScreen(new OptionsScreen(this, this.client.options));
        }, new TranslatableText("menu.options")));
        /*this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslatableText("menu.quit"), (button) -> {
            this.client.scheduleStop();
        }));*/
        this.addDrawableChild(new TexturedButtonWidget(this.width-50, j, 20, 20, 0, 0, 20, new Identifier("textures/gui/accessibility.png"), 32, 64, (button) -> {
            this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options));
        }, new TranslatableText("narrator.button.accessibility")));

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
        if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_ENTER)){
            if(InputUtil.isKeyPressed(handle, InputUtil.GLFW_KEY_RIGHT_SHIFT)){
                this.client.setScreen(new SelectWorldScreen(this));
            }else if(InputUtil.isKeyPressed(handle,InputUtil.GLFW_KEY_RIGHT_CONTROL)){
                this.client.setScreen(new MultiplayerScreen(this));
            }
            else{
                ServerAddress address = new ServerAddress("test3.davisoft.cn",26038);
                ServerInfo info = new ServerInfo("rdi-celetech3",address.getAddress(),false);
                ConnectScreen.connect(this,MinecraftClient.getInstance(),address,info);
            }
                //this.client.setScreen(new MultiplayerScreen(this));
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
