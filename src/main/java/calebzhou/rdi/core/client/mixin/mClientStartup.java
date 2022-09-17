package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiCore;
import calebzhou.rdi.core.client.UserInfoStorage;
import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import calebzhou.rdi.core.client.util.RdiAccountUtils;
import calebzhou.rdi.core.client.util.UuidUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.DataFixerUpper;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.Main;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Mixin(Main.class)
public class mClientStartup {
    @Redirect(method = "<clinit>",at = @At(value = "INVOKE",target = "Ljava/lang/System;setProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String headlessNo(String key, String value){
        //if(Util.getPlatform()== Util.OS.WINDOWS){
        String boolstr = Boolean.toString(Util.getPlatform() != Util.OS.WINDOWS);
        System.setProperty("java.awt.headless", boolstr);
            return boolstr;
        /*}
        return "true";*/
    }
    private static ArgumentAcceptingOptionSpec<String> passwordSpec;
    @Inject(method = "run",remap = false,at = @At(value = "INVOKE",target = "Ljoptsimple/OptionParser;nonOptions()Ljoptsimple/NonOptionArgumentSpec;"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void readPassword(String[] args, boolean bl, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25){
        passwordSpec = optionParser.accepts("password").withOptionalArg().defaultsTo("", (String[])new String[0]);
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("正在读取启动参数...");
    }
    @Inject(method = "run",remap = false,at = @At(value = "INVOKE",target = "Ljava/util/List;isEmpty()Z"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void readUuid(String[] args, boolean bl, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25, OptionSpec optionSpec26, OptionSet optionSet, List list){
		String name = (String) optionSpec11.value(optionSet);
		String uuid = (String) optionSpec12.value(optionSet);
		String pwd = passwordSpec.value(optionSet);
		RdiAccountUtils.login(name,uuid,pwd);



		UserInfoStorage.UserName=name;
		UserInfoStorage.UserUuid=uuid;
		UserInfoStorage.UserPwd=pwd;
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("游戏角色载入成功！");
    }
    @Inject(method = "run",remap=false,at = @At(value = "INVOKE",target = "Ljoptsimple/OptionSet;valuesOf(Ljoptsimple/OptionSpec;)Ljava/util/List;"))
    private static void loadParam2(String[] args, boolean bl, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("读取完成，正在分析...");
    }

}
@Mixin(RenderSystem.class)
class mStartup23{
    @Inject(remap = false,method = "beginInitialization",at = @At("HEAD"))
    private static void pre(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("初始化游戏引擎...");
    }
    @Inject(remap = false,method = "finishInitialization",at = @At("TAIL"))
    private static void pre2(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("初始化游戏引擎完成...");
    }
}
@Mixin(CrashReport.class)
class mStartup10{
    @Inject(method = "preload",at = @At("HEAD"))
    private static void preload(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入系统报告分析模块...");
    }
    @Inject(method = "preload",at = @At("TAIL"))
    private static void preload2(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入系统报告分析模块完成");
    }

}
@Mixin(Bootstrap.class)
abstract
class mStartup2{
    @Inject(method = "bootStrap",at = @At(value = "HEAD"))
    private static void l222(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入方块交互逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "TAIL"))
    private static void l22234(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入方块交互逻辑完成...");
    }
    @Inject(method = "validate",at = @At(value = "HEAD"))
    private static void l222c(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("验证方块交互逻辑...");
    }
    @Inject(method = "validate",at = @At(value = "TAIL"))
    private static void l22c234(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("验证方块交互逻辑完成...");
    }

    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/block/ComposterBlock;bootStrap()V"))
    private static void l2(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入堆肥方块逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/item/alchemy/PotionBrewing;bootStrap()V"))
    private static void l3(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入药水制作方块逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/commands/arguments/selector/options/EntitySelectorOptions;bootStrap()V"))
    private static void l4(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入实体选择器逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/core/dispenser/DispenseItemBehavior;bootStrap()V"))
    private static void l5(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入发射器逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/core/cauldron/CauldronInteraction;bootStrap()V"))
    private static void l6(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入熔炉逻辑...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/core/Registry;freezeBuiltins()V"))
    private static void l7(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("固化逻辑中..");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/server/Bootstrap;wrapStreams()V"))
    private static void l8(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入日志输出流...");
    }
    @Inject(method = "bootStrap",at = @At(value = "INVOKE",target = "Lnet/minecraft/world/level/block/FireBlock;bootStrap()V"))
    private static void l1(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入火焰方块逻辑...");
    }



}
@Mixin(DataFixerBuilder.class)
class mStartup3{
    @Inject(remap = false,method = "buildOptimized",locals = LocalCapture.CAPTURE_FAILSOFT,at = @At(value = "INVOKE",target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private void fre(Executor executor, CallbackInfoReturnable<DataFixer> cir, DataFixerUpper fixerUpper, Instant started, List futures, IntBidirectionalIterator iterator, int versionKey, Schema schema, Iterator var8, String typeName){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("#注册"+typeName);
    }
}
@Mixin(Minecraft.class)
class mStartup4{
    /*@Inject( method = "<init>",at = @At(value = "NEW",target = "net.minecraft.client.resources.ClientPackSource"))
    private void prese(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("读取设置...");
    }
    @Inject( method = "<init>",at = @At(value = "NEW",target = "net.minecraft.client.renderer.VirtualScreen"))
    private void presde(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("创建渲染缓冲...");
    }*/
    @Inject(method = "run",at=@At("HEAD"))
    private void run(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("启动游戏主线程...");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setClearColor(FFFF)V"))
    private void a(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入键鼠管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 0,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入语言管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 1,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a12(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入材质管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 2,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a122(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入声音管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 3,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a121232(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入图片管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 4,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a124342(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入字体管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 5,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1722(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入草色管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 7,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a17822(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 8,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a178222(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入实体模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 9,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a175822(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入容器模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 10,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a176822(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入容器管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 11,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a178822(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入物品渲染模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 12,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a178922(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入方块渲染模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 13,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a170822(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入实体渲染模型管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 14,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1022(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入着色器管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 15,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1422(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入地图管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 16,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1222(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入搜索管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 17,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a122s2(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入粒子管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 18,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1s222(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入图画管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 19,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1f222(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入怪物特效管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 20,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a1g222(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入显卡管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",ordinal = 21,target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V"))
    private void a122b2(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("载入本地化管理器");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"))
    private void ca(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("设置窗口尺寸");
    }
    @Inject(method = "<init>",at = @At(value = "INVOKE",target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;"))
    private void aj(GameConfig gameConfig, CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("读取资源包");
    }
//TODO
}
@Mixin(SoundEngine.class)
class mStartup5{
    @Inject(method = "reload",at = @At("HEAD"))
    private void reload(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("启动声效模块");
    }
    @Inject(method = "reload",at = @At("TAIL"))
    private void reload2(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("成功启动声效模块");
    }
}
@Mixin(TextureAtlas.class)
class mStartup6{
    @Shadow @Final private ResourceLocation location;

    @Inject(method = "reload",at = @At("HEAD"))
    private void reload( CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo(
                String.format("#贴图写入显存：%s",
                        location.getPath()));
    }
    @Inject(method = "reload",at = @At("TAIL"))
    private void reload2(CallbackInfo ci){
        LoadProgressDisplay.INSTANCE.appendLoadProgressInfo("成功！");
    }
}
