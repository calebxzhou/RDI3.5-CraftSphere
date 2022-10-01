package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import calebzhou.rdi.core.client.misc.RdiSystemTray;
import calebzhou.rdi.core.client.util.DialogUtils;
import net.minecraft.Util;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(Main.class)
public class mClientStartup {

    @Redirect(method = "<clinit>",at = @At(value = "INVOKE",target = "Ljava/lang/System;setProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String headlessNo(String key, String value){
		//是windows就false 不是就true 防止卡死
        String boolstr = Boolean.toString(Util.getPlatform() != Util.OS.WINDOWS);
        System.setProperty("java.awt.headless", boolstr);
		return boolstr;
    }
	@Inject(method = "run",at = @At("HEAD"))
	private static void rdiStart(String[] args, boolean enableDataFixerOptimizations, CallbackInfo ci){
		LoadProgressDisplay.loadStartTime=System.currentTimeMillis();
		RdiSystemTray.createTray();
		new LoadProgressDisplay().start();
		if(Util.getPlatform() == Util.OS.WINDOWS)
			DialogUtils.showPopup(TrayIcon.MessageType.INFO,"RDI客户端已经开始载入了！请您耐心等待...");
	}
   /* @Inject(method = "run",remap = false,at = @At(ordinal = 0,value = "INVOKE",target = "Lnet/minecraft/client/main/Main;parseArgument(Ljoptsimple/OptionSet;Ljoptsimple/OptionSpec;)Ljava/lang/Object;"),locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void RDI_readUuid(String[] args, boolean enableDataFixerOptimizations, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25, OptionSpec optionSpec26, OptionSet optionSet){

		String name = (String) optionSpec11.value(optionSet);
		String uuid = (String) optionSpec12.value(optionSet);
		String userTypeName = (String) optionSpec24.value(optionSet);


		if(StringUtils.isEmpty(uuid) || uuid.startsWith("00000000")){
			uuid = UuidUtils.createUuidByName(name);
		}else{
			//mojang登录的uuid不带横线，要通过正则表达式转换成带横线的
			uuid = UuidUtils.uuidAddDash(uuid);
		}
		File passwordFile = RdiFileConst.getUserPasswordFile(uuid);

		String pwd = null;
		try {
			pwd = FileUtils.readFileToString(passwordFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			RdiCore.LOGGER.warn("此账户没有注册过 {}",e.getMessage());
		}
		RdiUser.setCurrentUser(new RdiUser(uuid,name,pwd,userTypeName));
    }*/

}
