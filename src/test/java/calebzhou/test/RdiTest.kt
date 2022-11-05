package calebzhou.test

import calebzhou.craftcone.model.dto.ConeLevelOpenRequestDto
import calebzhou.craftcone.model.dto.ConePlayerDto
import calebzhou.rdi.core.client.RdiLoader
import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.logger
import calebzhou.rdi.core.client.misc.HwSpec
import calebzhou.rdi.core.client.model.ConeLevel
import calebzhou.rdi.core.client.model.ConePlayer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.*
import java.util.*

/**
 * Created by calebzhou on 2022-10-12,22:22.
 */
class RdiTest {
    @Test
    fun testHwSpec(){
        println(HwSpec.currentHwSpec)
    }
    @Test
    fun testIp(){
        var ip: String
        try {
            val interfaces: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface: NetworkInterface = interfaces.nextElement()
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp()) continue
                val addresses: Enumeration<InetAddress> = iface.getInetAddresses()
                while (addresses.hasMoreElements()) {
                    val addr: InetAddress = addresses.nextElement()
                    ip = addr.getHostAddress()
                    println(iface.getDisplayName() + " " + ip)
                }
            }
        } catch (e: SocketException) {
            throw RuntimeException(e)
        }
    }
    @Test
    fun testIp2(){
      RdiLoader.initNetwork()
    }
    @Test
    fun testCone(){
        val level =  ConeLevelOpenRequestDto(ConePlayerDto("0","test"),"testMap")
        val levelJson = Json.encodeToString(level)
        logger.info(levelJson)
        val levelJsonByte = levelJson.toByteArray()
        val socket = Socket(RdiSharedConstants.CENTRAL_SERVER.address,RdiSharedConstants.CENTRAL_SERVER.port)
        val request =  socket.getOutputStream()
        val response =  socket.getInputStream()
        val data = ByteArray(1)
        data[0]= 0x01
        val newData = data+levelJsonByte
        request.write(newData)
        socket.shutdownOutput()
        val responseData = response.readAllBytes()
        logger.info(responseData[0])
    }
}
