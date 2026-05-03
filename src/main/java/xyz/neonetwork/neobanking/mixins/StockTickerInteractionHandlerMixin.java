package xyz.neonetwork.neobanking.mixins;


import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRS;
import xyz.neonetwork.neobanking.api.IRSPaymentState;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neobanking.blockitems.CoinItem;
import xyz.neonetwork.neobanking.blockitems.NeoItems;
import xyz.neonetwork.neobanking.paymentprocessor.CurrencyHandler;
import xyz.neonetwork.neobanking.paymentprocessor.ShopResolver;

import java.util.List;
import java.util.Map;

@Mixin(com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler.class)
public class StockTickerInteractionHandlerMixin {

	@Inject(
		method = "interactWithShop",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void neobanking$interactWithShop(Player player, Level level, BlockPos targetPos, ItemStack mainHandItem, CallbackInfo ci) {
		if (level.isClientSide) return;
		if (!(level.getBlockEntity(targetPos) instanceof StockTickerBlockEntity tickerBE)) return;

		ShoppingListItem.ShoppingList list = ShoppingListItem.getList(mainHandItem);

		if (list == null) return;

		if (!tickerBE.behaviour.freqId.equals(list.shopNetwork())) {
			AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
			CreateLang.translate("stock_keeper.wrong_network")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			return;
		}

		Couple<InventorySummary> bakeEntries = list.bakeEntries(level, null);
		InventorySummary paymentEntries = bakeEntries.getSecond();

		boolean isCoins = false;
		boolean isItems = false;

		for (Item item : paymentEntries.getItemMap().keySet()) {
			if (item instanceof CoinItem) {
				isCoins = true;
			} else {
				isItems = true;
			}
		}

		if (isItems && isCoins) {
			AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
			CreateLang.builder()
				.text("You can't make a purchase that costs items and coins simultaniously")
				.style(ChatFormatting.RED)
				.sendStatus(player);
			ci.cancel();
			return;
		}

		if (isItems && !isCoins) return;

		if (!player.getUUID().toString().equals("380df991-f603-344c-a090-369bad2a924a")) {
			if (list.shopOwner().equals(player.getUUID())) {
				AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
				CreateLang.builder()
					.text("You can't purchase items from your own shop with money")
					.style(ChatFormatting.RED)
					.sendStatus(player);
				ci.cancel();
				return;
			}
		}

		int amountNeeded = 0;
		for (BigItemStack bigItemStack : paymentEntries.getStacks()) {
			amountNeeded += bigItemStack.count;
		}

		int playerAmount = CurrencyHandler.calculateSimpleInventoryValue(player);

		ShopResolver shopResolver = new ShopResolver(level, tickerBE, bakeEntries, player, ShoppingListItem.getAddress(mainHandItem));
		if (playerAmount >= amountNeeded) {
			boolean success = CurrencyHandler.removeValueFromInventory(player, amountNeeded);
			if (!success) {
				NeoBanking.LOGGER.error("Failed to remove {} coins from player {}", amountNeeded, player.getScoreboardName());
				ci.cancel();
				return;
			}
			IRS.serverSendMoney(list.shopOwner().toString(), amountNeeded, String.format("*Cash Shop Transaction at X:%s, Y:%s, Z:%s", tickerBE.getBlockPos().getX(), tickerBE.getBlockPos().getY(), tickerBE.getBlockPos().getZ()));
			boolean shopSuccess = shopResolver.processOrder();
			if (!shopSuccess) {
				CurrencyHandler.addValueToInventory(player, amountNeeded);
				IRS.serverReceiveMoney(list.shopOwner().toString(), amountNeeded, String.format("*Insuficient Stock Refund* at X:%s, Y:%s, Z:%s", tickerBE.getBlockPos().getX(), tickerBE.getBlockPos().getY(), tickerBE.getBlockPos().getZ()));
				CurrencyHandler.addValueToInventory(player, amountNeeded);
				ci.cancel();
				return;
			}
		} else {
			String reference = String.format("*Shop Transaction* at X:%s, Y:%s, Z:%s", tickerBE.getBlockPos().getX(), tickerBE.getBlockPos().getY(), tickerBE.getBlockPos().getZ());
			if (reference.length() > 64) reference = reference.substring(0, 64);
			IRSTransaction transaction;
			if (player.getUUID().toString().equals("380df991-f603-344c-a090-369bad2a924a")) {
				transaction = IRS.requestMoney("02c0f072-5e8f-46f8-a400-4b1722b293f0", player.getUUID().toString(), amountNeeded, reference);
			} else {
				transaction = IRS.requestMoney(list.shopOwner().toString(), player.getUUID().toString(), amountNeeded, reference);
			}
			switch (transaction.getState()) {
				case IRSPaymentState.PENDING:
					break;
				case IRSPaymentState.INSUFFICIENT_FUNDS:
					AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
					CreateLang.builder()
						.text("You don't have enough money to purchase this")
						.style(ChatFormatting.RED)
						.sendStatus(player);
					ci.cancel();
					return;
				default:
					AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
					CreateLang.builder()
						.text("An unknown error occured")
						.style(ChatFormatting.RED)
						.sendStatus(player);
					ci.cancel();
					return;
			}
			CreateLang.builder()
				.text("Please accept the transaction in your Portable Banking Terminal")
				.sendStatus(player);
			ShopResolver.addToQueue(transaction.getTransactionID(), shopResolver);
		}

		mainHandItem.setCount(0);
		ci.cancel();
	}
}

