package calebzhou.rdi.core.client.constant

import calebzhou.rdi.core.client.RdiSharedConstants
import java.io.File

/**
 * Created by calebzhou on 2022-09-20,21:23.
 */
object RdiFileConst {
    @JvmStatic
	fun getUserPasswordFile(uuid: String): File {
        return File(RdiSharedConstants.RDI_USERS_FOLDER, uuid + "_password.txt")
    }
}
