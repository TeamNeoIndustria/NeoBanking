package xyz.neonetwork.neobanking.paymentprocessor;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRS;
import xyz.neonetwork.neobanking.api.IRSPaymentState;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neobanking.api.IRSWebsocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopResolver {

	private static final Map<String, ShopResolver> resolverQueue = new HashMap<>();
	private static final Map<String, IRSTransaction> toProcessQueue = new HashMap<>();

	private static final List<String> removalQueue = new ArrayList<>();
	private static final List<String> fallbackRemovalQueue = new ArrayList<>();

	private Level level;
	private StockTickerBlockEntity tickerBE;
	private Couple<InventorySummary> bakeEntries;
	private Player player;
	private String address;
	private long expiryTime = -1;
	private String pendingTransactionID;

	public ShopResolver(Level level, StockTickerBlockEntity tickerBE, Couple<InventorySummary> bakeEntries, Player player, String address) {
		this.level = level;
		this.tickerBE = tickerBE;
		this.bakeEntries = bakeEntries;
		this.player = player;
		this.address = address;
	}

	public boolean processOrder() {
		if (this.level.isClientSide) return false;

		InventorySummary orderEntries = bakeEntries.getFirst();
		PackageOrder order = new PackageOrder(orderEntries.getStacksByCount());

		InventorySummary recentSummary = tickerBE.getAccurateSummary();

		for (BigItemStack entry : order.stacks()) {
			if (recentSummary.getCountOf(entry.stack) >= entry.count) continue;

			if (player == null) {
				AllSoundEvents.DENY.playOnServer(level, tickerBE.getBlockPos());
			} else {
				AllSoundEvents.DENY.playOnServer(level, player.blockPosition());
				CreateLang.translate("stock_keeper.stock_level_too_low")
					.style(ChatFormatting.RED)
					.sendStatus(player);
			}
			return false;
		}

		boolean success = tickerBE.broadcastPackageRequest(LogisticallyLinkedBehaviour.RequestType.PLAYER, order, null, this.address);
		if (!order.isEmpty()) {
			AllSoundEvents.STOCK_TICKER_TRADE.playOnServer(level, tickerBE.getBlockPos());
		}
		return success;
	}

	public static void addToQueue(@NotNull String pendingTransactionID, @NotNull ShopResolver shopResolver) {
		shopResolver.expiryTime = System.currentTimeMillis() + 120000;
		shopResolver.pendingTransactionID = pendingTransactionID;
		resolverQueue.put(pendingTransactionID, shopResolver);
	}

	public static void tickQueue() {
		if (fallbackTickProcessing) return;
		for (Map.Entry<String, IRSTransaction> entry : toProcessQueue.entrySet()) { // Process payments
			ShopResolver shopResolver = resolverQueue.get(entry.getKey());
			IRSTransaction processingTransaction = entry.getValue();
			if (shopResolver == null || processingTransaction.getState() != IRSPaymentState.ACCEPTED) {
				removalQueue.add(entry.getKey());
				continue;
			}
			// Process shopResolver
			boolean success = shopResolver.processOrder();
			if (!success) {
				IRS.sendMoney(
					processingTransaction.getTo().getPlayerUUID().toString(),
					processingTransaction.getFrom().getPlayerUUID().toString(),
					processingTransaction.getAmount(),
					String.format("*Automatic Shop Refund* Couldn't process order TxID: %s", processingTransaction.getTransactionID()));
				removalQueue.add(entry.getKey());
				continue;
			}

			removalQueue.add(entry.getKey());
		}

		long currentTime = System.currentTimeMillis();
		for (Map.Entry<String, ShopResolver> entry : resolverQueue.entrySet()) {
			ShopResolver shopResolver = entry.getValue();
			if (shopResolver.expiryTime > currentTime) break;
			removalQueue.add(entry.getKey());
		}

		for (String item : removalQueue) {
			resolverQueue.remove(item);
			toProcessQueue.remove(item);
		}
	}

	private static boolean fallbackTickProcessing = false;
	public static boolean fallbackShouldProcess = true;
	public static void tickFallbackEndpoints() {
		if (fallbackTickProcessing) return;
		fallbackTickProcessing = true;
		if (IRSWebsocket.isConnected()) fallbackShouldProcess = false;
		new Thread(() -> {
			for (Map.Entry<String, ShopResolver> entry : resolverQueue.entrySet()) {
				if (toProcessQueue.containsKey(entry.getKey())) continue;
				IRSTransaction transaction = IRS.serverGetTransactionStatus(entry.getValue().player.getStringUUID(), entry.getKey());
				if (transaction == null) continue;
				if (transaction.getState() == IRSPaymentState.ACCEPTED) {
					processPayment(transaction);
				} else if (transaction.getState() != IRSPaymentState.PENDING) {
					fallbackRemovalQueue.add(entry.getKey());
				}
			}

			for (String item : fallbackRemovalQueue) {
				resolverQueue.remove(item);
				toProcessQueue.remove(item);
			}
			fallbackTickProcessing = false;
		}).start();
	}

	public static void processPayment(@NotNull IRSTransaction transaction) {
		if (!resolverQueue.containsKey(transaction.getTransactionID())) return;
		toProcessQueue.put(transaction.getTransactionID(), transaction);
	}

}
