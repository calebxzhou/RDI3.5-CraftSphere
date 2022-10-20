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
}

