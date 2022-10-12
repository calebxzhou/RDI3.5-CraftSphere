package calebzhou.rdi.core.client

import calebzhou.rdi.core.client.misc.HwSpec
import icyllis.modernui.textmc.ModernUITextMC
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents

val logger: Logger = LogManager.getLogger("RDI-Core")
class RdiCore : ModInitializer {
    override fun onInitialize(container: ModContainer) {
        EventRegister()
        RdiNetworkReceiver.register()
        HwSpec.loadSystemSpec()
        ClientLifecycleEvents.READY.register(ClientLifecycleEvents.Ready { m: Minecraft? -> ModernUITextMC.setupClient() })
    }

}
