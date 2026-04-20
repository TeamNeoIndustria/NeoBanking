package xyz.neonetwork.neobanking.paymentprocessor;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSPaymentState;
import xyz.neonetwork.neobanking.api.IRSPlayer;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.toast.NeoToastData;
import xyz.neonetwork.neolib.toast.NeoToastPacket;
import xyz.neonetwork.neolib.utilities.NeoComponent;
import xyz.neonetwork.neolib.utilities.NeoString;

public class WebsocketHandler {
	public static void migratorEvent(JsonObject dataNode) {
		try { // Harry please forgive me, I have to write like 10x as much code to avoid doing this
			String type = dataNode.get("type").getAsString();
			IRSPaymentState state;
			switch (type) {
				case "approve": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string, approved: boolean
					state = (dataNode.get("approved").getAsBoolean()) ? IRSPaymentState.ACCEPTED : IRSPaymentState.DECLINED;
					break;
				case "request": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.PENDING;
					break;
				case "send": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.ACCEPTED;
					break;
				default:
					NeoBanking.LOGGER.warn("WebsocketHandler#migratorEvent type ({}) not recognised", type);
					return;
			}
			IRSTransaction transaction = new IRSTransaction(dataNode.get("txID").getAsString(), new IRSPlayer(dataNode.get("from").getAsString()), new IRSPlayer(dataNode.get("to").getAsString()), dataNode.get("amount").getAsInt(), dataNode.get("reference").getAsString(), 0L, state);
			NeoToastData toastData;
			switch (type) {
				case "request":
					ServerPlayer fromPlayer = (ServerPlayer) transaction.getFrom().getPlayer();
					if (fromPlayer == null) return;
					toastData = new NeoToastData(
						Component.literal("Payment Request").withStyle(ChatFormatting.BOLD),
						NeoComponent.formatString( "&a%s &rto &a%s", NeoString.formatCurrency(transaction.getAmount()), transaction.getTo().getPlayerDisplayName()),
						NeoTexture.BANK,
						5000);
					PacketDistributor.sendToPlayer(fromPlayer, new NeoToastPacket(toastData));
					break;
				case "approve":
					if (transaction.getState() == IRSPaymentState.DECLINED) break;
				case "send":
					ShopResolver.processPayment(transaction);
					ServerPlayer toPlayer = (ServerPlayer) transaction.getTo().getPlayer();
					if (toPlayer == null) return;
					toastData = new NeoToastData(
						Component.literal("Payment Received").withStyle(ChatFormatting.BOLD),
						NeoComponent.formatString("&a%s &rfrom &a%s", NeoString.formatCurrency(transaction.getAmount()), transaction.getFrom().getPlayerDisplayName()),
						NeoTexture.BANK,
						5000);
					PacketDistributor.sendToPlayer(toPlayer, new NeoToastPacket(toastData));
					break;
			}
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("WebsocketHandler.migratorEvent received invalid data. {}", e.getMessage());
		}
	}
}
