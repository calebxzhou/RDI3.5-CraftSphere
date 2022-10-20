package calebzhou.rdi.core.client.util

import com.google.gson.GsonBuilder

object RdiSerializer {
    @JvmField
	val gson = GsonBuilder().serializeNulls().create()
}
