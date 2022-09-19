package calebzhou.rdi.core.client;

import calebzhou.rdi.core.client.emoji.EmojiClientProxy;
import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import calebzhou.rdi.core.client.util.ThreadPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class RdiCore implements ModInitializer {
	static {
		ThreadPool.newThread(LoadProgressDisplay.INSTANCE::start);
	}
	public static final Logger LOGGER = LogManager.getLogger("RDI-Core");

	@Override
	public void onInitialize(ModContainer container) {
		new EventRegister();
		RdiNetworkReceiver.INSTANCE.register();
		EmojiClientProxy.INSTANCE.init();

	}


}
