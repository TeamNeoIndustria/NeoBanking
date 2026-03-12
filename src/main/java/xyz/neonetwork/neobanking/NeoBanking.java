package xyz.neonetwork.neobanking;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
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
import org.slf4j.Logger;
import xyz.neonetwork.neobanking.commands.NeoBankingCommand;
import xyz.neonetwork.neobanking.items.NeoItems;
import xyz.neonetwork.neobanking.networking.IRS;

import java.util.Objects;

@Mod(NeoBanking.MODID)
public class NeoBanking {
	public static final String MODID = "neobanking";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final NeoRegistrate REGISTRATE = NeoRegistrate.create(MODID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);

	public NeoBanking(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);
		NeoForge.EVENT_BUS.register(this);

		modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

		REGISTRATE.registerEventListeners(modEventBus);
		NeoItems.register();
		CreativeTab.register(modEventBus);
	}

	private void commonSetup(FMLCommonSetupEvent event) {

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
