package xyz.neonetwork.neobanking.server;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import xyz.neonetwork.neobanking.NeoBanking;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = NeoBanking.MODID, dist = Dist.DEDICATED_SERVER)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = NeoBanking.MODID, value = Dist.DEDICATED_SERVER)
public class NeoBankingServer {
	public NeoBankingServer(IEventBus modEventBus, ModContainer container) {
		// Allows NeoForge to create a config screen for this mod's configs.
		// The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
		// Do not forget to add translations for your config options to the en_us.json file.
		modEventBus.register(ServerModEvents.class);
	}

	@SubscribeEvent
	static void onServerSetup(FMLDedicatedServerSetupEvent event) {
		// Some client setup code
	}
}
