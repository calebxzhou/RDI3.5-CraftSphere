package calebxzhou.rdi.model

import calebxzhou.libertorch.SerDesJson
import calebxzhou.libertorch.util.IdUt
import calebxzhou.libertorch.util.IdUt.toRdid
import calebxzhou.libertorch.util.UuidUt
import com.mojang.authlib.GameProfile
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Created by calebzhou on 2022-09-18,22:40.
 */
data class RdiUser(
    val uuid: UUID,
    val name: String,
    var pwd: String,
    //LEGACY = 非正版 MOJANG = 正版
    val type: String
){
    //RDID
    val rdid
        get() = uuid.toRdid()
    //本地用户配置文件
    val profileFile
        get() = getProfileFile(rdid)

    val gameProfile
        get() = GameProfile(uuid,name)

    //保存至用户文件
    fun save(){
        val json = SerDesJson.gson.toJson(this)
        FileUtils.write(profileFile,json,StandardCharsets.UTF_8,false)
    }

    companion object{
        lateinit var now: RdiUser
        val default = RdiUser(UuidUt.createFromName(""),"","","LEGACY")
        @JvmStatic
        fun load(uuid: String): RdiUser {
            val file = getProfileFile(uuid)
            val json = FileUtils.readFileToString(file, StandardCharsets.UTF_8)
            val rdiUser = SerDesJson.gson.fromJson(json, RdiUser::class.java);
            return rdiUser
        }

        fun getProfileFile(uuid: String):File{
            return File("rdi_user_$uuid.json")
        }
    }
}

