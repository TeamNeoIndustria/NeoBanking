package xyz.neonetwork.neobanking.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import xyz.neonetwork.neobanking.NeoBanking;

@Mod(value = NeoBanking.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = NeoBanking.MODID, value = Dist.CLIENT)
public class NeoBankingClient {

	public NeoBankingClient(IEventBus eventBus, ModContainer container) {
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {

	}
}
