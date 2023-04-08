package calebxzhou.rdi.util

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

object ChatUt {
    fun addMessage(msg: String?) {
        addMessage(Component.literal(msg))
    }

    fun addMessage(component: Component?) {
        Minecraft.getInstance().gui.chat.addMessage(component)
    }
}
