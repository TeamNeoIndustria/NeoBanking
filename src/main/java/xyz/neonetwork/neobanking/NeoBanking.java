package xyz.neonetwork.neobanking;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import xyz.neonetwork.neobanking.commands.NeoBankingCommand;
import xyz.neonetwork.neobanking.networking.IRS;
import xyz.neonetwork.neobanking.networking.IRSLeaderboardEntry;
import xyz.neonetwork.neobanking.networking.IRSSimpleTransaction;
import xyz.neonetwork.neobanking.networking.IRSTransaction;

import java.util.List;
import java.util.Objects;

@Mod(NeoBanking.MODID)
public class NeoBanking {
	public static final String MODID = "neobanking";
	public static final Logger LOGGER = LogUtils.getLogger();

	public NeoBanking(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);

		NeoForge.EVENT_BUS.register(this);

		modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event) {

	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("NeoBanking Server Loaded");
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!Objects.requireNonNull(event.getEntity().getServer()).isDedicatedServer()) return;
		if (!IRS.serverCreateUser(event.getEntity().getStringUUID())) {
			LOGGER.warn("Failed to create user in IRS database");
		} else {
			LOGGER.info("Validated player in IRS database");
		}
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		NeoBankingCommand.register(event.getDispatcher());
	}
}
