package xyz.neonetwork.neobanking.paymentprocessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.blockitems.CoinItem;

import java.util.List;

public class CurrencyHandler {
	public static List<CoinItem> COINS;

	public static boolean isValidCurrency(ItemStack itemStack) {
		Item item = itemStack.getItem();
		for (CoinItem coinItem : COINS) {
			if (coinItem.equals(item)) return true;
		}
		return false;
	}

	public static int calculateSimpleInventoryValue(@NotNull Player player) {
		int[] rawAmount = calculateComplexInventoryValue(player);
		return calculateSimpleInventoryValue(rawAmount);
	}
	public static int calculateSimpleInventoryValue(int[] rawAmount) {
		if (rawAmount.length != COINS.size()) return 0;
		int amount = 0;
		for (int i = 0; i < COINS.size(); i++) {
			amount += rawAmount[i] * COINS.get(i).getValue();
		}
		return amount;
	}

	public static int[] calculateComplexInventoryValue(@NotNull Player player) {
		int[] amount = new int[COINS.size()];

		for (int i = 0; i < COINS.size(); i++) {
			amount[i] = player.getInventory().countItem(COINS.get(i));
		}

		return amount;
	}

	/**
	 * @param player Target player
	 * @param item Item to remove
	 * @param quantity Quantity of items to remove from player
	 * @return Quantity of items that were removed (not always the same as quantity parameter)
	 */
	public static int removeItemFromInventory(@NotNull Player player, @NotNull Item item, int quantity) {
		if (quantity < 1) return 0;
		int removedTotal = 0;
		for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
			ItemStack itemStack = player.getInventory().getItem(index);
			if (itemStack.isEmpty() || itemStack.getItem() != item) continue;

			int toRemove = Math.min(itemStack.getCount(), quantity);
			itemStack.shrink(toRemove);
			removedTotal += toRemove;
			quantity -= toRemove;

			if (itemStack.isEmpty()) player.getInventory().setItem(index, ItemStack.EMPTY);
			if (quantity <= 0) break;
		}
		player.getInventory().setChanged();
		if (!player.level().isClientSide) {
			player.containerMenu.broadcastChanges();
		}
		return removedTotal;
	}

	public static boolean removeValueFromInventory(Player player, int amount) {
		int playerAmount = calculateSimpleInventoryValue(player);
		if (amount > playerAmount) return false;

		for (CoinItem coinItem : COINS) {
			if (amount == 0) break;
			int currentCoinCount = player.getInventory().countItem(coinItem);
			int toRemove = Math.clamp(amount / coinItem.getValue(), 0, currentCoinCount);
			if (toRemove == 0) continue;
			int removed = removeItemFromInventory(player, coinItem, toRemove);
			amount -= removed * coinItem.getValue();
//			NeoBanking.LOGGER.info("{} removed: {}/{}", coinItem.getName(new ItemStack(coinItem)).getString(), removed, toRemove);
		}
		for (int currencyReverseIndex = COINS.size() - 1; currencyReverseIndex >= 0; currencyReverseIndex--) {
			if (amount <= 0) break;
			CoinItem coinItem = COINS.get(currencyReverseIndex);
			if (amount > coinItem.getValue()) continue;
			int currentCoinCount = player.getInventory().countItem(coinItem);
			int toRemove = Math.clamp((int) Math.ceil((double) amount / coinItem.getValue()), 0, currentCoinCount);
			if (toRemove == 0) continue;
			int removed = removeItemFromInventory(player, coinItem, toRemove);
			amount -= removed * coinItem.getValue();
//			NeoBanking.LOGGER.info("2nd Pass - {} removed: {}/{}", coinItem.getName(new ItemStack(coinItem)).getString(), removed, toRemove);
		}
		if (amount < 0) {
//			NeoBanking.LOGGER.info("Undervalue ({}), adding back.", amount);
			addValueToInventory(player, Math.abs(amount));
		}
//		NeoBanking.LOGGER.info("Amount remaining: {}", amount);
		return true;
	}

	public static boolean addValueToInventory(Player player, int amount) {
		for (CoinItem coinItem : COINS) {
			while (amount >= coinItem.getValue()) {
				ItemStack coinStack = new ItemStack(coinItem);
				boolean added = player.addItem(coinStack);

				if (!added || !coinStack.isEmpty()) {
					player.drop(coinStack, false);
				}

				amount -= coinItem.getValue();
			}
		}
		return true;
	}
}
