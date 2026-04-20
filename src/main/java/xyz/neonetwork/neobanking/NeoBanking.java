package xyz.neonetwork.neobanking;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import xyz.neonetwork.neobanking.api.IRS;
import xyz.neonetwork.neobanking.api.IRSWebsocket;
import xyz.neonetwork.neobanking.blockitems.CoinItem;
import xyz.neonetwork.neobanking.commands.NeoBankingCommand;
import xyz.neonetwork.neobanking.blockitems.NeoItems;
import xyz.neonetwork.neobanking.paymentprocessor.CurrencyHandler;
import xyz.neonetwork.neobanking.paymentprocessor.ShopResolver;

import java.util.Objects;

@Mod(NeoBanking.MODID)
public class NeoBanking {
	public static final String MODID = "neobanking";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final NeoRegistrate REGISTRATE = NeoRegistrate.create(MODID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
	public static MinecraftServer server;

	private int tickTimer = 0;

	public NeoBanking(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);
		NeoForge.EVENT_BUS.register(this);

		modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

		REGISTRATE.registerEventListeners(modEventBus);
		NeoItems.register();
		CreativeTab.register(modEventBus);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		CurrencyHandler.COINS = NeoItems.NEO_COINS.values().stream()
			.map(entry -> (CoinItem) entry.get())
			.sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
			.toList();
	}

	@SubscribeEvent
	public void onServerStart(ServerStartedEvent event) {
		server = event.getServer();
	}

	@SubscribeEvent
	public void onServerTick(ServerTickEvent.Post event) {
		if (++tickTimer == 20) {
			tickTimer = 0;
			ShopResolver.tickQueue();
			if (ShopResolver.fallbackShouldProcess) ShopResolver.tickFallbackEndpoints();
		}
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!Objects.requireNonNull(event.getEntity().getServer()).isDedicatedServer()) return;
		if (!IRS.serverCreateUser(event.getEntity().getStringUUID())) {
			LOGGER.warn("Failed to create user in IRS database");
		} else {
			LOGGER.info("Validated transaction in IRS database");
		}
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		NeoBankingCommand.register(event.getDispatcher());
	}
}
