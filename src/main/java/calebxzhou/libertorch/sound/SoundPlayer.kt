package calebxzhou.libertorch.sound

import calebxzhou.rdi.consts.RdiSounds
import net.minecraft.util.thread.NamedThreadFactory
import java.io.InputStream
import java.util.concurrent.Executors

/**
 * Created  on 2023-04-07,22:44.
 */
object SoundPlayer {
    private val threadPool = Executors.newSingleThreadExecutor(NamedThreadFactory("RdiSoundPlayThread"))
    fun playOgg(sound: RdiSounds){
        playOgg(sound.oggStream)
    }
    fun playOgg(musicStream: InputStream) {
        threadPool.submit { OggPlayer(musicStream).start() }
    }

}
