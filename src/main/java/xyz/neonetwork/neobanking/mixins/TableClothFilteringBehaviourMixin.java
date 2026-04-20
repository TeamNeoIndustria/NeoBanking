package xyz.neonetwork.neobanking.mixins;

import com.simibubi.create.content.logistics.tableCloth.TableClothFilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.neonetwork.neobanking.blockitems.CoinItem;
import xyz.neonetwork.neobanking.blockitems.NeoItems;
import xyz.neonetwork.neobanking.paymentprocessor.CurrencyHandler;
import xyz.neonetwork.neolib.utilities.NeoComponent;

import java.util.List;

@Mixin(com.simibubi.create.content.logistics.tableCloth.TableClothFilteringBehaviour.class)
public class TableClothFilteringBehaviourMixin {

	@Inject(
		method = "setFilter",
		at = @At("HEAD"),
		cancellable = true
	)
	private void neobanking$setFilter(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
		if (!CurrencyHandler.isValidCurrency(itemStack)) {
			return;
		}

		FilteringBehaviour self = (FilteringBehaviour) (Object) this;
		FilteringBehaviourAccessor accessor = (FilteringBehaviourAccessor) self;
		if (itemStack.getItem() instanceof CoinItem coinItem) {
			accessor.setCount(coinItem.getValue());
		}
		boolean result = self.setFilter(new ItemStack(NeoItems.NEO_SYSTEM_ITEMS.get("base_coin").get()));
		cir.setReturnValue(result);
	}

	@Inject(
		method = "createBoard",
		at = @At("HEAD"),
		cancellable = true
	)
	private void neobanking$createBoard(Player player, BlockHitResult hitResult, CallbackInfoReturnable<ValueSettingsBoard> cir) {
		TableClothFilteringBehaviour self = (TableClothFilteringBehaviour) (Object) this;

		if (!self.getFilter().is(NeoItems.NEO_SYSTEM_ITEMS.get("base_coin"))) {
			return;
		}

		List<Component> currencyRows = List.of(
			NeoComponent.formatString("x1"),
			NeoComponent.formatString("x5"),
			NeoComponent.formatString("x10"),
			NeoComponent.formatString("x100")
		);
		ValueSettingsBoard board = new ValueSettingsBoard(
			self.getLabel(), 100, 10, currencyRows,
			new ValueSettingsFormatter(value -> {
				int[] steps = {1, 5, 10, 100};
				int[] offsets = {0, 100, 600, 1000};
				int step = steps[value.row()];
				int offset = offsets[value.row()];
				int minimum = offsets[value.row()] + steps[value.row()];
				int actualValue = Math.max(minimum, offset + value.value() * step);

				return Component.literal(String.valueOf(actualValue));
			})
		);

		cir.setReturnValue(board);
	}

	@Inject(
		method = "setValueSettings",
		at = @At("HEAD"),
		cancellable = true
	)
	private void neobanking$setValueSettings(Player player, ValueSettingsBehaviour.ValueSettings settings, boolean ctrlDown, CallbackInfo ci) {
		TableClothFilteringBehaviour self = (TableClothFilteringBehaviour) (Object) this;

		if (!self.getFilter().is(NeoItems.NEO_SYSTEM_ITEMS.get("base_coin"))) {
			return;
		}
		if (self.getValueSettings().equals(settings)) {
			return;
		}

		int[] steps = {1, 5, 10, 100};
		int[] offsets = {0, 100, 600, 1000};
		int step = steps[settings.row()];
		int offset = offsets[settings.row()];
		int minimum = offsets[settings.row()] + steps[settings.row()];
		int actualValue = Math.max(minimum, offset + settings.value() * step);

		FilteringBehaviourAccessor accessor = (FilteringBehaviourAccessor) self;
		accessor.setCount(Math.min(actualValue, 11600));

		ci.cancel();
	}
}