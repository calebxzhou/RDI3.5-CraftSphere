package calebxzhou.libertorch.util

import java.net.ServerSocket

/**
 * Created  on 2023-04-13,22:30.
 */
object NetUt {
    fun getAvailablePort(): Int {
        ServerSocket(0).use { socket->
            return socket.localPort
        }
    }
}
