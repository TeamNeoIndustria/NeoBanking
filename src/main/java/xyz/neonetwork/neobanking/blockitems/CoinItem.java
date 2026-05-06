package xyz.neonetwork.neobanking.blockitems;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neolib.utilities.NeoString;

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
	public @NotNull Component getName(@NotNull ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack));
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
		String valueTooltip = Component.translatable("tooltip.neobanking.coin_item_value").getString().replace("$value", NeoString.formatCurrency(this.value));
		tooltipComponents.add(Component.literal(valueTooltip));
		if (stack.getCount() > 1) {
			String totalTooltip = Component.translatable("tooltip.neobanking.coin_item_total_value").getString().replace("$value", NeoString.formatCurrency(this.value * stack.getCount()));
			tooltipComponents.add(Component.literal(totalTooltip));
		}
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
	}
}
