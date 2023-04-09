package calebxzhou.rdi.model

import calebxzhou.libertorch.SerDesJson
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Created by calebzhou on 2022-09-18,22:40.
 */
data class RdiUser(
    val uuid: String,
    val name: String,
    var pwd: String,
    val type: String
){
    val profileFile
        get() = getProfileFile(uuid)
    fun save(){
        val json = SerDesJson.gson.toJson(this)
        FileUtils.write(profileFile,json,StandardCharsets.UTF_8,false)
    }

    companion object{
        lateinit var now: RdiUser
        val default = RdiUser("","","","LEGACY")
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

