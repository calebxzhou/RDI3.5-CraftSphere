package calebzhou.rdi.core.client.model

import java.net.InetAddress

/**
 * Created  on 2022-11-05,8:49.
 */
@kotlinx.serialization.Serializable
data class ConeLevel(val levelName:String,var players:List<ConePlayer>)
@kotlinx.serialization.Serializable
data class ConePlayer(val uuid:String, val pname:String, val ip: String, val port :UShort)
