package calebzhou.craftcone.model.entity

import java.net.Socket

/**
 * Created  on 2022-11-05,10:12.
 */
data class ConePlayer(val uuid:String,val pname:String,val clientSocket:Socket)