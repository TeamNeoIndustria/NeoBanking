package xyz.neonetwork.neobanking.paymentprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSPaymentState;
import xyz.neonetwork.neobanking.api.IRSPlayer;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.toast.NeoToastData;
import xyz.neonetwork.neolib.toast.NeoToastPacket;
import xyz.neonetwork.neolib.utilities.NeoString;

public class WebsocketHandler {
	// {"event":"migrator","data":{"type":"request","txID":"227","from":"02c0f0725e8f46f8a4004b1722b293f0","to":"380df991f603344ca090369bad2a924a","amount":"1","reference":"Oi, give me money!"}}
	public static void migratorEvent(JsonNode dataNode) {
		if (!dataNode.isObject() || !dataNode.has("type") || !dataNode.get("type").isTextual()) {
			NeoBanking.LOGGER.warn("WebsocketHandler.migratorEvent data node invalid");
			return;
		}
		String type = dataNode.get("type").asText("!UNKNOWN!");
		try { // Harry please forgive me, I have to write like 10x as much code to avoid doing this
			IRSPaymentState state;
			Player player;
			switch (type) {
				case "approve": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string, approved: boolean
					state = (dataNode.get("approved").asBoolean()) ? IRSPaymentState.ACCEPTED : IRSPaymentState.DECLINED;
					break;
				case "request": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.PENDING;
					break;
				case "send": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.ACCEPTED;
					break;
				default:
					NeoBanking.LOGGER.info("WebsocketHandler#migratorEvent type ({}) not recognised", type);
					return;
			}
			IRSTransaction transaction = new IRSTransaction(dataNode.get("txID").asText(), new IRSPlayer(dataNode.get("from").asText()), new IRSPlayer(dataNode.get("to").asText()), dataNode.get("amount").asInt(), dataNode.get("reference").asText(), 0L, state);
			NeoToastData toastData;
			switch (type) {
				case "approve":
					ResolveShopPayment.addToRequestResolverQueue(transaction);
					return;
				case "request":
					ServerPlayer fromPlayer = (ServerPlayer) transaction.getFrom().getPlayer();
					if (fromPlayer == null) return;
					toastData = new NeoToastData(
						Component.literal("Payment Request").withStyle(ChatFormatting.BOLD),
						Component.literal(NeoString.formatCurrency(transaction.getAmount()) + " to " + transaction.getTo().getPlayerDisplayName()),
						NeoTexture.BANK,
						5000);
					PacketDistributor.sendToPlayer(fromPlayer, new NeoToastPacket(toastData));
					break;
				case "send":
					ServerPlayer toPlayer = (ServerPlayer) transaction.getTo().getPlayer();
					if (toPlayer == null) return;
					toastData = new NeoToastData(
						Component.literal("Payment Received").withStyle(ChatFormatting.BOLD),
						Component.literal(NeoString.formatCurrency(transaction.getAmount()) + " from " + transaction.getFrom().getPlayerDisplayName()),
						NeoTexture.BANK,
						5000);
					PacketDistributor.sendToPlayer(toPlayer, new NeoToastPacket(toastData));
					break;
			}
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("WebsocketHandler.migratorEvent type {} had invalid Data", type);
			e.printStackTrace();
		}
	}
}
