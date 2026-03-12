package xyz.neonetwork.neobanking.paymentprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.networking.IRSPaymentState;
import xyz.neonetwork.neobanking.networking.IRSPlayer;
import xyz.neonetwork.neobanking.networking.IRSTransaction;

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
			switch (type) {
				case "request": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.PENDING;
					break;
				case "approve": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string, approved: boolean
					state = (dataNode.get("approved").asBoolean()) ? IRSPaymentState.ACCEPTED : IRSPaymentState.DECLINED;
					break;
				case "send": // IRSTransaction - txID:string, from:string, to:string, amount:int, reference:string
					state = IRSPaymentState.ACCEPTED;
					break;
				default:
					NeoBanking.LOGGER.info("WebsocketHandler.migratorEvent type ({}) not recognised", type);
					return;
			}
			IRSTransaction requestTransaction = new IRSTransaction(dataNode.get("txID").asText(), new IRSPlayer(dataNode.get("from").asText()), new IRSPlayer(dataNode.get("to").asText()), dataNode.get("amount").asInt(), dataNode.get("reference").asText(), 0L, state);
			ResolveShopPayment.addToRequestResolverQueue(requestTransaction);
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("WebsocketHandler.migratorEvent type {} had invalid Data", type);
			e.printStackTrace();
		}
	}
}
