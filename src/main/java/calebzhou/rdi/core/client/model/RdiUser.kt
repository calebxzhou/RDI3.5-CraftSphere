package calebzhou.rdi.core.client.model

import calebzhou.rdi.core.client.constant.RdiFileConst
import calebzhou.rdi.core.client.logger
import calebzhou.rdi.core.client.util.UuidUtils
import net.minecraft.client.User
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.nio.charset.StandardCharsets

/**
 * Created by calebzhou on 2022-09-18,22:40.
 */
data class RdiUser(val uuid: String, val name: String, var pwd: String, val type: String){

    companion object{
        var currentRdiUser: RdiUser? = null
        fun loadCurrentRdiUser(uuid:String,name:String,type: User.Type):RdiUser{
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
            return RdiUser(
                uuidWithDash,
                name,
                pwd,
                userTypeName
            )
        }
    }
}

