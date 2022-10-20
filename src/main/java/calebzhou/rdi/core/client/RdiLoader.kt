package calebzhou.rdi.core.client

import calebzhou.rdi.core.client.constant.RdiFileConst
import calebzhou.rdi.core.client.loader.LoadProgressRecorder
import calebzhou.rdi.core.client.misc.HwSpec
import calebzhou.rdi.core.client.model.RdiGeoLocation
import calebzhou.rdi.core.client.model.RdiUser
import calebzhou.rdi.core.client.model.RdiWeather
import calebzhou.rdi.core.client.model.ResponseData
import calebzhou.rdi.core.client.util.DialogUtils
import calebzhou.rdi.core.client.util.HttpClient
import calebzhou.rdi.core.client.util.UuidUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.minecraft.client.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.io.FileUtils
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.awt.TrayIcon
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Created by calebzhou on 2022-10-14,20:53.
 */
object RdiLoader {
    fun onMinecraftStart(){
        GlobalScope.launch{
            TinyFileDialogs.tinyfd_notifyPopup("RDI客户端将会启动！","","info");
            loadBasicInfos()
            LoadProgressRecorder.onStart()
        }
    }
    suspend fun loadBasicInfos(){
        coroutineScope {
            launch {
                HwSpec.currentHwSpec = HwSpec.loadSystemSpec()
            }


        }
    }
    fun loadCurrentRdiUser(uuid:String,name:String,type: User.Type) {
        var processedUuid:String = uuid;
        if (uuid.startsWith("00000000")) {
            processedUuid = UuidUtils.createUuidByName(name)
        }
        val uuidWithDash = UuidUtils.uuidAddDash(processedUuid)
        logger.info("u:{}", uuidWithDash)
        val userTypeName = type.getName()
        val passwordFile = RdiFileConst.getUserPasswordFile(uuidWithDash)
        var pwd = ""
        try {
            pwd = FileUtils.readFileToString(passwordFile, StandardCharsets.UTF_8)
            logger.info("p:{}", pwd)
        } catch (e: IOException) {
            logger.warn("此账户没有注册过 {}", e.message)
        }
        //mojang登录的uuid不带横线，要通过正则表达式转换成带横线的
        RdiCore.currentRdiUser= RdiUser(
            uuidWithDash,
            name,
            pwd,
            userTypeName
        )
    }


}
