package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.constant.RdiFileConst;
import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import calebzhou.rdi.core.client.misc.RdiSystemTray;
import calebzhou.rdi.core.client.model.RdiUser;
import calebzhou.rdi.core.client.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.Util;
import net.minecraft.client.main.Main;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Mixin(Main.class)
public class mClientStartup {

    @Redirect(method = "<clinit>",at = @At(value = "INVOKE",target = "Ljava/lang/System;setProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String headlessNo(String key, String value){
		//是windows就false 不是就true 防止卡死
        String boolstr = Boolean.toString(Util.getPlatform() != Util.OS.WINDOWS);
        System.setProperty("java.awt.headless", boolstr);
		return boolstr;
    }
    @Inject(method = "run",remap = false,at = @At(value = "INVOKE",target = "Ljava/util/List;isEmpty()Z"),locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void readUuid(String[] args, boolean bl, CallbackInfo ci, OptionParser optionParser, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10, OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15, OptionSpec optionSpec16, OptionSpec optionSpec17, OptionSpec optionSpec18, OptionSpec optionSpec19, OptionSpec optionSpec20, OptionSpec optionSpec21, OptionSpec optionSpec22, OptionSpec optionSpec23, OptionSpec optionSpec24, OptionSpec optionSpec25, OptionSpec optionSpec26, OptionSet optionSet, List list){
		RdiSystemTray.createTray();
		ThreadPool.newThread(LoadProgressDisplay.INSTANCE::start);
		if(Util.getPlatform() == Util.OS.WINDOWS)
			DialogUtils.showPopup(TrayIcon.MessageType.INFO,"RDI客户端已经开始载入了！请您耐心等待...");

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
			throw new RuntimeException(e);
		}
		RdiUser.setCurrentUser(new RdiUser(uuid,name,pwd,userTypeName));
    }

}
