package calebxzhou.rdi

import calebxzhou.libertorch.sound.SoundPlayer
import calebxzhou.rdi.misc.LoadProgressRecorder
import calebxzhou.rdi.misc.HwSpec
import calebxzhou.rdi.model.RdiUser
import calebxzhou.libertorch.util.UuidUt
import calebxzhou.rdi.consts.RdiSounds
import calebxzhou.rdi.model.RdiUser.Companion.default
import joptsimple.OptionParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.User
import org.apache.commons.io.FileUtils
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.io.IOException
import java.nio.charset.StandardCharsets


/**
 * Created by calebzhou on 2022-10-14,20:53.
 */
object RdiLoader {
    @JvmStatic
    fun onMinecraftStart(args: Array<String>) = GlobalScope.launch {
        LoadProgressRecorder.loadStartTime = System.currentTimeMillis()
        logger.info("开始载入客户端 RDI部分 客户端启动参数：${args.contentToString()}")
        launch {
            initUser(args)
        }
        launch {
            //播放启动音效
            SoundPlayer.playOgg(RdiSounds.Launch)
        }
        launch {
            logger.info("载入硬件信息")
            HwSpec.currentHwSpec = HwSpec.loadSystemSpec()
        }

    }


    private fun initUser(args: Array<String>) {
        logger.info("开始读取用户信息")
        val parser = OptionParser()
        parser.allowsUnrecognizedOptions()
        val nameSpec = parser.accepts("username").withRequiredArg()
        val uuidSpec = parser.accepts("uuid").withRequiredArg().defaultsTo("00000000")
        val userTypeSpec = parser.accepts("userType").withRequiredArg().defaultsTo(User.Type.LEGACY.name)
        val specSet = parser.parse(*args)

        val userType = userTypeSpec.value(specSet)
        logger.info("账户类型 {}", userType)

        val name = nameSpec.value(specSet)
        logger.info("昵称 {}", name)

        //PCL启动器会用00000000开头的uuid，不要让他用这样的，要生成新的
        //mc的uuid刚读出来是不带横线的，给他加上
        val uuid = UuidUt.uuidAddDash(
            if (uuidSpec.value(specSet).startsWith("00000000"))
                UuidUt.createUuidByName("OfflinePlayer:$name")
            else
                uuidSpec.value(specSet)
        )
        logger.info("UUID {}", uuid)

        logger.info("读取用户文件")
        RdiUser.now = try {
            RdiUser.load(uuid)
        } catch (e: Exception) {
            logger.warn("没有读取到用户文件： {} {}", e.javaClass.name,e.message)
            RdiUser(uuid,name,"","LEGACY")
        }
        logger.info("读取完成：{}",RdiUser.now)
    }


}
