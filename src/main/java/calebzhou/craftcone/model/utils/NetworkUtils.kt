package calebzhou.craftcone.model.utils

import java.net.ServerSocket

/**
 * Created  on 2022-11-05,16:43.
 */
object NetworkUtils {
    fun getAvailablePort():UShort{
        ServerSocket(0).use {socket->
            return socket.localPort.toUShort()
        }
    }
}