package calebzhou.rdi.core.client;

import calebzhou.rdi.core.client.emoji.EmojiClientProxy;
import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import calebzhou.rdi.core.client.screen.RdiDebugOverlay;
import calebzhou.rdi.core.client.util.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

public class RdiCore implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("RDI-Core");
	public static RdiDebugOverlay RDI_DEBUG_OVERLAY;
	@Override
	public void onInitialize(ModContainer container) {
		new EventRegister();
		RdiNetworkReceiver.INSTANCE.register();
		EmojiClientProxy.INSTANCE.init();
		ClientLifecycleEvents.READY.register(ready->{
			RDI_DEBUG_OVERLAY = new RdiDebugOverlay(ready.gameRenderer.getMinecraft());
		});

	}


}
