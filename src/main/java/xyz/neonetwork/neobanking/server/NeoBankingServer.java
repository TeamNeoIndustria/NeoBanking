package xyz.neonetwork.neobanking.server;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSWebsocket;

@Mod(value = NeoBanking.MODID, dist = Dist.DEDICATED_SERVER)
@EventBusSubscriber(modid = NeoBanking.MODID, value = Dist.DEDICATED_SERVER)
public class NeoBankingServer {
	public NeoBankingServer(IEventBus eventBus, ModContainer container) {

	}

	@SubscribeEvent
	static void onServerSetup(FMLDedicatedServerSetupEvent event) {
		// Some client setup code
		IRSWebsocket.connect();
	}
}
