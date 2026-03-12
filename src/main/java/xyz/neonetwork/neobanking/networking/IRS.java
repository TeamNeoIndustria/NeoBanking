package xyz.neonetwork.neobanking.networking;

import com.fasterxml.jackson.databind.JsonNode;
import xyz.neonetwork.neobanking.Config;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neolib.api.APIRequest;
import xyz.neonetwork.neolib.api.APIResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class IRS {
	public static final String neoNetworkIRSEndpoint = Config.IRS_WEB_ENDPOINT.get();
	public static final String apiKey = Config.IRS_WEB_APIKEY.get();

	public static List<IRSLeaderboardEntry> getLeaderboard() {
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "leaderboard", new HashMap<String, String>() {{
			put("apikey", apiKey);
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return null;
		}
		if (!response.getDataNode().isArray()) return null;
		List<IRSLeaderboardEntry> leaderboardEntries = new ArrayList<>();
		for (JsonNode jsonNode : response.getDataNode()) {
			try {
				IRSPlayer player = new IRSPlayer(jsonNode.get("user").get("id").asText(), jsonNode.get("user").get("name").asText());
				IRSLeaderboardEntry leaderboardEntry = new IRSLeaderboardEntry(player, jsonNode.get("balance").asInt());
				leaderboardEntries.add(leaderboardEntry);
			} catch (Exception e) {
				NeoBanking.LOGGER.warn("IRS#getLeaderboard skipped leaderboard entry due to invalid data");
				NeoBanking.LOGGER.debug(e.getMessage());
			}
		}
		return leaderboardEntries;
	}

	public static int getUserBalance(String playerUUID) {
		if (playerUUID == null || playerUUID.isEmpty()) return -1;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "balance", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#getUserBalance failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return -1;
		}
		if (!response.getDataNode().has("uuid")) return -1;
		JsonNode jsonNode = response.getDataNode().get("uuid");
		if (!jsonNode.isInt()) return -1;
		return jsonNode.asInt();
	}

	public static List<IRSTransaction> getTransactionHistory(String playerUUID) {
		return getTransactionHistory(playerUUID, 10);
	}
	public static List<IRSTransaction> getTransactionHistory(String playerUUID, int historyLength) {
		if (playerUUID == null || playerUUID.isEmpty() || historyLength < 1 || historyLength > 100) return null;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "history", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
			put("limit", String.valueOf(historyLength));
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#getTransactionHistory failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return null;
		}
		if (!response.getDataNode().isArray()) return null;
		List<IRSTransaction> transactions = new ArrayList<>();
		for (JsonNode jsonNode : response.getDataNode()) {
			try {
				IRSPlayer fromPlayer = new IRSPlayer(jsonNode.get("from").get("id").asText(), jsonNode.get("from").get("name").asText());
				IRSPlayer toPlayer = new IRSPlayer(jsonNode.get("to").get("id").asText(), jsonNode.get("to").get("name").asText());
				IRSTransaction transaction = new IRSTransaction(jsonNode.get("txID").asText(), fromPlayer, toPlayer,
					jsonNode.get("amount").asInt(), jsonNode.get("reference").asText(), jsonNode.get("timestamp").asLong(), IRSPaymentState.ACCEPTED);
				transactions.add(transaction);
			} catch (Exception e) {
				NeoBanking.LOGGER.warn("IRS#getTransactionHistory skipped history entry due to invalid data");
				NeoBanking.LOGGER.debug(e.getMessage());
			}
		}
		return transactions;
	}

	public static IRSPaymentState getTransactionStatus(String playerUUID, String transactionID) {
		if (playerUUID == null || playerUUID.isEmpty()) return IRSPaymentState.UNKNOWN;
		if (transactionID == null || transactionID.isEmpty()) return IRSPaymentState.UNKNOWN;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "verify", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
			put("txID", transactionID);
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#getTransactionStatus failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return IRSPaymentState.UNKNOWN;
		}
		if (!response.getDataNode().has("state")) return IRSPaymentState.UNKNOWN;
		JsonNode jsonNode = response.getDataNode().get("state");
		if (!jsonNode.isInt()) return IRSPaymentState.UNKNOWN;
		try {
			return IRSPaymentState.fromStateID(jsonNode.asInt());
		} catch (IllegalArgumentException e) {
			NeoBanking.LOGGER.warn("IRS#getTransactionStatus failed. Bad state ID: {}", jsonNode.asInt());
			NeoBanking.LOGGER.debug(e.getMessage());
		}
		return IRSPaymentState.UNKNOWN;
	}

	public static List<IRSTransaction> getPendingTransactions(String playerUUID) {
		if (playerUUID == null || playerUUID.isEmpty()) return null;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "pending", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#getPendingTransactions failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return null;
		}
		if (!response.getDataNode().isArray()) return null;
		List<IRSTransaction> transactions = new ArrayList<>();
		for (JsonNode jsonNode : response.getDataNode()) {
			try {
				IRSPlayer fromPlayer = new IRSPlayer(playerUUID);
				IRSPlayer toPlayer = new IRSPlayer(jsonNode.get("user").get("id").asText(), jsonNode.get("user").get("name").asText());
				IRSTransaction transaction = new IRSTransaction(jsonNode.get("txID").asText(), fromPlayer, toPlayer,
					jsonNode.get("amount").asInt(), jsonNode.get("reference").asText(), jsonNode.get("timestamp").asLong(), IRSPaymentState.PENDING);
				transactions.add(transaction);
			} catch (Exception e) {
				NeoBanking.LOGGER.warn("IRS#getPendingTransactions skipped history entry due to invalid data");
				NeoBanking.LOGGER.debug(e.getMessage());
			}
		}
		return transactions;
	}

	public static IRSSimpleTransaction approveTransaction(String playerUUID, String transactionID, boolean approve) {
		if (playerUUID == null || playerUUID.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "approve", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
			put("txID", transactionID);
			put("approve", approve ? "true" : "false");
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#approveTransaction failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		}
		if (!response.getDataNode().has("accepted")) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (!Objects.equals(response.getDataNode().get("accepted").asText("false"), "true")) return new IRSSimpleTransaction(null, IRSPaymentState.DECLINED);
		if (!response.getDataNode().has("txID")) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN) ;
		return new IRSSimpleTransaction(response.getDataNode().get("txID").asText(), IRSPaymentState.ACCEPTED);
	}

	public static IRSSimpleTransaction sendMoney(String playerUUID, String toNameOrUUID, int amount, String reference) {
		return sendMoney(playerUUID, toNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSSimpleTransaction sendMoney(String playerUUID, String toNameOrUUID, String amount, String reference) {
		return sendMoney(playerUUID, toNameOrUUID, amount, reference, false);
	}
	public static IRSSimpleTransaction serverSendMoney(String toNameOrUUID, int amount, String reference) {
		return serverSendMoney(toNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSSimpleTransaction serverSendMoney(String toNameOrUUID, String amount, String reference) {
		return sendMoney(null, toNameOrUUID, amount, reference, true);
	}
	private static IRSSimpleTransaction sendMoney(String playerUUID, String toNameOrUUID, String amount, String reference, boolean fromServer) {
		if (!fromServer && (playerUUID == null || playerUUID.isEmpty())) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (toNameOrUUID == null || toNameOrUUID.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (amount == null || amount.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (reference == null || reference.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		HashMap<String, String> parameters = new HashMap<>() {{
			put("apikey", apiKey);
			put("to", toNameOrUUID);
			put("amount", amount);
			put("reference", reference);
		}};
		if (!fromServer) {
			parameters.put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
		}
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "send", parameters);
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#sendMoney failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		}
		if (!response.getDataNode().has("txID") || !response.getDataNode().get("txID").isTextual()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN) ;
		return new IRSSimpleTransaction(response.getDataNode().get("txID").asText(), IRSPaymentState.ACCEPTED);
	}

	public static IRSSimpleTransaction requestMoney(String playerUUID, String fromNameOrUUID, int amount, String reference) {
		return requestMoney(playerUUID, fromNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSSimpleTransaction requestMoney(String playerUUID, String fromNameOrUUID, String amount, String reference) {
		return requestMoney(playerUUID, fromNameOrUUID, amount, reference, false);
	}
	public static IRSSimpleTransaction serverRequestMoney(String fromNameOrUUID, int amount, String reference) {
		return serverRequestMoney(fromNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSSimpleTransaction serverRequestMoney(String fromNameOrUUID, String amount, String reference) {
		return requestMoney(null, fromNameOrUUID, amount, reference, true);
	}
	private static IRSSimpleTransaction requestMoney(String playerUUID, String fromNameOrUUID, String amount, String reference, boolean fromServer) {
		if (!fromServer && (playerUUID == null || playerUUID.isEmpty())) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (fromNameOrUUID == null || fromNameOrUUID.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (amount == null || amount.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		if (reference == null || reference.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		HashMap<String, String> parameters = new HashMap<>() {{
			put("apikey", apiKey);
			put("from", fromNameOrUUID);
			put("amount", amount);
			put("reference", reference);
		}};
		if (!fromServer) {
			parameters.put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
		}
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "request", parameters);
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#requestMoney failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		}
		if (!response.getDataNode().has("txID") || !response.getDataNode().get("txID").isTextual()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN) ;
		return new IRSSimpleTransaction(response.getDataNode().get("txID").asText(), IRSPaymentState.PENDING);
	}

	public static String serverNewKey(String playerUUID) {
		if (playerUUID == null || playerUUID.isEmpty()) return null;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "newkey", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("uuid", playerUUID); // Runs the api as if the specified user provided their own apikey
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#serverNewKey failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return null;
		}
		if (!response.getDataNode().has("uuid") || !response.getDataNode().has("apikey")) return null;
		JsonNode jsonNode = response.getDataNode().get("apikey");
		if (!jsonNode.isTextual()) return null;
		return jsonNode.asText();
	}

	public static boolean serverCreateUser(String playerUUID) {
		if (playerUUID == null || playerUUID.isEmpty()) return false;
		APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "createuser", new HashMap<String, String>() {{
			put("apikey", apiKey);
			put("uuid", playerUUID); // Runs the api as if the specified user provided their own apikey
		}});
		if (!response.getSuccess()) {
			NeoBanking.LOGGER.warn("IRS#serverCreateUser failed. Code: {}, Message: {}",
				response.getStatusCode(), response.getStatusMessage());
			return false;
		}
		if (!response.getDataNode().has("uuid")) return false;
		return response.getDataNode().get("uuid").isTextual();
	}
}
