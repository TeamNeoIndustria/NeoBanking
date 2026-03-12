package xyz.neonetwork.neobanking.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class CoinItem extends Item {
	private final int value;

	public CoinItem(int value, Properties properties) {
		super(properties);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		String tooltip = Component.translatable("tooltip.neobanking.coin_item").getString().replace("$value", String.valueOf(this.value));
		tooltipComponents.add(Component.literal(tooltip));
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
	}
}
