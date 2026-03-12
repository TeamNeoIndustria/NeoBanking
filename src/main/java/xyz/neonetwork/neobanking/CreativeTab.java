package xyz.neonetwork.neobanking;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.neonetwork.neobanking.items.NeoItems;

public class CreativeTab {
	public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoBanking.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NEO_CREATIVE_TAB = REGISTER.register(NeoBanking.MODID + "-creativetab",
		() -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.neobanking"))
			.icon(NeoItems.NEO_SYSTEM_ITEMS.get("base_coin")::asStack)
			.displayItems((param, output) -> {
				NeoItems.NEO_COINS.forEach((id, item) -> output.accept(item.asStack()));
			})
			.build()
	);

	public static void register(IEventBus eventBus) {
		REGISTER.register(eventBus);
	}
}
