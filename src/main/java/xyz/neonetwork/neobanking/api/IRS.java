package xyz.neonetwork.neobanking.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.Config;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neolib.api.APIRequest;
import xyz.neonetwork.neolib.api.APIResponse;

import java.util.*;

public class IRS {
	public static final String neoNetworkIRSEndpoint = Config.IRS_WEB_ENDPOINT.get();
	public static final String apiKey = Config.IRS_WEB_APIKEY.get();

	public static List<IRSLeaderboardEntry> getLeaderboard() {
		try {
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "leaderboard", new HashMap<>() {{
				put("apikey", apiKey);
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#getLeaderboard failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return null;
			}
			List<IRSLeaderboardEntry> leaderboardEntries = new ArrayList<>();
			for (JsonElement entry : response.getDataNode().getAsJsonArray()) {
				JsonObject jsonNode = entry.getAsJsonObject();
				IRSPlayer player = new IRSPlayer(jsonNode.get("user").getAsJsonObject().get("id").getAsString(), jsonNode.get("user").getAsJsonObject().get("name").getAsString());
				IRSLeaderboardEntry leaderboardEntry = new IRSLeaderboardEntry(player, jsonNode.get("balance").getAsInt());
				leaderboardEntries.add(leaderboardEntry);
			}
			return leaderboardEntries;
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse leaderboard");
			NeoBanking.LOGGER.debug(e.getMessage());
			return null;
		}
	}

	public static int getUserBalance(String playerUUID) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return -1;
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "balance", new HashMap<>() {{
				put("apikey", apiKey);
				put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#getUserBalance failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return -1;
			}
			return response.getDataNode().getAsJsonObject().get("balance").getAsInt();
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse user balance");
			NeoBanking.LOGGER.debug(e.getMessage());
			return -1;
		}
	}

	public static List<IRSTransaction> getTransactionHistory(String playerUUID) {
		return getTransactionHistory(playerUUID, 10);
	}
	public static List<IRSTransaction> getTransactionHistory(String playerUUID, int historyLength) {
		try {
			if (playerUUID == null || playerUUID.isEmpty() || historyLength < 1 || historyLength > 100) return null;
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "history", new HashMap<>() {{
				put("apikey", apiKey);
				put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
				put("limit", String.valueOf(historyLength));
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#getTransactionHistory failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return null;
			}
			List<IRSTransaction> transactions = new ArrayList<>();
			for (JsonElement entry : response.getDataNode().getAsJsonArray()) {
				JsonObject jsonNode = entry.getAsJsonObject();
				IRSPlayer fromPlayer = new IRSPlayer(jsonNode.get("from").getAsJsonObject().get("id").getAsString(), jsonNode.get("from").getAsJsonObject().get("name").getAsString());
				IRSPlayer toPlayer = new IRSPlayer(jsonNode.get("to").getAsJsonObject().get("id").getAsString(), jsonNode.get("to").getAsJsonObject().get("name").getAsString());
				IRSTransaction transaction = new IRSTransaction(jsonNode.get("txID").getAsString(), fromPlayer, toPlayer,
					jsonNode.get("amount").getAsInt(), jsonNode.get("reference").getAsString(), jsonNode.get("timestamp").getAsLong(), IRSPaymentState.ACCEPTED);
				transactions.add(transaction);
			}
			return transactions;
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getTransactionHistory failed to parse transaction history");
			NeoBanking.LOGGER.debug(e.getMessage());
			return null;
		}
	}
	public static IRSTransaction getTransactionStatus(String playerUUID, String transactionID) {
		return getTransactionStatus(playerUUID, transactionID, false);
	}
	public static IRSTransaction serverGetTransactionStatus(String playerUUID, String transactionID) {
		return getTransactionStatus(playerUUID, transactionID, true);
	}
	private static IRSTransaction getTransactionStatus(String playerUUID, String transactionID, boolean fromServer) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return null;
			if (transactionID == null || transactionID.isEmpty()) return null;
			Map<String, String> parameters = new HashMap<>() {{
				put("apikey", apiKey);
				put("txID", transactionID);
			}};
			if (!fromServer) parameters.put("as", playerUUID);
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "verify", parameters);
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#getTransactionStatus failed. Code: {}, Message: {}", response.getStatusCode(), response.getStatusMessage());
				return switch (response.getStatusCode()) {
					case "408" -> new IRSTransaction(null, IRSPaymentState.TIMED_OUT);
					default -> new IRSTransaction(null, IRSPaymentState.UNKNOWN);
				};
			}
			JsonObject jsonNode = response.getDataNode().getAsJsonObject();
			return new IRSTransaction(
				jsonNode.get("txID").getAsString(),
				new IRSPlayer(jsonNode.get("from").getAsString()),
				new IRSPlayer(jsonNode.get("to").getAsString()),
				jsonNode.get("amount").getAsInt(),
				jsonNode.get("reference").getAsString(),
				jsonNode.get("timestamp").getAsLong(),
				IRSPaymentState.fromStateID(jsonNode.get("state").getAsInt())
			);

		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse transaction status");
			NeoBanking.LOGGER.debug(e.getMessage());
			return null;
		}
	}

	public static List<IRSTransaction> getPendingTransactions(String playerUUID) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return null;
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "pending", new HashMap<>() {{
				put("apikey", apiKey);
				put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#getPendingTransactions failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return null;
			}
			List<IRSTransaction> transactions = new ArrayList<>();
			for (JsonElement entry : response.getDataNode().getAsJsonArray()) {
				JsonObject jsonNode = entry.getAsJsonObject();
				IRSPlayer fromPlayer = new IRSPlayer(playerUUID);
				IRSPlayer toPlayer = new IRSPlayer(jsonNode.get("user").getAsJsonObject().get("id").getAsString(), jsonNode.get("user").getAsJsonObject().get("name").getAsString());
				IRSTransaction transaction = new IRSTransaction(jsonNode.get("txID").getAsString(), fromPlayer, toPlayer,
					jsonNode.get("amount").getAsInt(), jsonNode.get("reference").getAsString(), jsonNode.get("timestamp").getAsLong(), IRSPaymentState.PENDING);
				transactions.add(transaction);
			}
			return transactions;
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse pending transactions");
			NeoBanking.LOGGER.debug(e.getMessage());
			return null;
		}
	}

	public static IRSSimpleTransaction approveTransaction(String playerUUID, String transactionID, boolean approve) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "approve", new HashMap<>() {{
				put("apikey", apiKey);
				put("as", playerUUID); // Runs the api as if the specified user provided their own apikey
				put("txID", transactionID);
				put("approve", Boolean.toString(approve));
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#approveTransaction failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return switch (response.getStatusCode()) {
					case "402" -> new IRSSimpleTransaction(null, IRSPaymentState.INSUFFICIENT_FUNDS);
					default -> new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
				};
			}
			if (!Objects.equals(response.getDataNode().getAsJsonObject().get("accepted").getAsString(), "true")) return new IRSSimpleTransaction(null, IRSPaymentState.DECLINED);
			return new IRSSimpleTransaction(response.getDataNode().getAsJsonObject().get("txID").getAsString(), IRSPaymentState.ACCEPTED);
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse approve transaction");
			NeoBanking.LOGGER.debug(e.getMessage());
			return new IRSSimpleTransaction(null, IRSPaymentState.UNKNOWN);
		}
	}

	/**
	 * @param playerUUID UUID of FROM player
	 * @param toNameOrUUID UUID or Name of TO player
	 * @param amount Amount (positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction sendMoney(String playerUUID, String toNameOrUUID, int amount, String reference) {
		return sendMoney(playerUUID, toNameOrUUID, String.valueOf(amount), reference);
	}
	/**
	 * @param playerUUID UUID of FROM player
	 * @param toNameOrUUID UUID or Name of TO player
	 * @param amount Amount (string value of positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction sendMoney(String playerUUID, String toNameOrUUID, String amount, String reference) {
		return sendMoney(playerUUID, toNameOrUUID, amount, reference, false);
	}
	/**
	 * @param toNameOrUUID UUID or Name of TO player
	 * @param amount Amount (positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction serverSendMoney(String toNameOrUUID, int amount, String reference) {
		return serverSendMoney(toNameOrUUID, String.valueOf(amount), reference);
	}
	/**
	 * @param toNameOrUUID UUID or Name of TO player
	 * @param amount Amount (string value of positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction serverSendMoney(String toNameOrUUID, String amount, String reference) {
		return sendMoney(null, toNameOrUUID, amount, reference, true);
	}
	/**
	 * @param fromUUID UUID of FROM player
	 * @param amount Amount (positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction serverReceiveMoney(String fromUUID, int amount, String reference) {
		return serverReceiveMoney(fromUUID, String.valueOf(amount), reference);
	}
	/**
	 * @param fromUUID UUID of FROM player
	 * @param amount Amount (string value of positive integer between 1 and Integer.MAX_VALUE inclusive)
	 * @param reference Reference between 1 and 64 in length (inclusive)
	 * @return IRSTransaction of payment details
	 */
	public static @NotNull IRSTransaction serverReceiveMoney(String fromUUID, String amount, String reference) {
		return sendMoney(fromUUID, "@Server", amount, reference, false);
	}
	private static @NotNull IRSTransaction sendMoney(String playerUUID, String toNameOrUUID, String amount, String reference, boolean fromServer) {
		try {
			if (!fromServer && (playerUUID == null || playerUUID.isEmpty())) return new IRSTransaction(null, IRSPaymentState.FROM_PLAYER_INVALID);
			if (toNameOrUUID == null || toNameOrUUID.isEmpty()) return new IRSTransaction(null, IRSPaymentState.TO_PLAYER_INVALID);
			if (amount == null || amount.isEmpty()) return new IRSTransaction(null, IRSPaymentState.INVALID_AMOUNT);
			if (reference == null || reference.isEmpty()) return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
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
				NeoBanking.LOGGER.warn("IRS#sendMoney failed. Code: {}, Message: {}", response.getStatusCode(), response.getStatusMessage());
				return switch (response.getStatusCode()) {
					case "402" -> new IRSTransaction(null, IRSPaymentState.INSUFFICIENT_FUNDS);
					case "461" -> new IRSTransaction(null, IRSPaymentState.INVALID_REFERENCE);
					case "462" -> new IRSTransaction(null, IRSPaymentState.TO_PLAYER_INVALID);
					case "465" -> new IRSTransaction(null, IRSPaymentState.TO_FROM_PLAYER_SAME);
					case "467" -> new IRSTransaction(null, IRSPaymentState.CANNOT_SEND_SERVER);
					default -> new IRSTransaction(null, IRSPaymentState.UNKNOWN);
				};
			}
			return new IRSTransaction(response.getDataNode().getAsJsonObject().get("txID").getAsString(),
				new IRSPlayer(response.getDataNode().getAsJsonObject().get("from").getAsString()),
				new IRSPlayer(response.getDataNode().getAsJsonObject().get("to").getAsString()),
				response.getDataNode().getAsJsonObject().get("amount").getAsInt(),
				response.getDataNode().getAsJsonObject().get("reference").getAsString(),
				response.getDataNode().getAsJsonObject().get("timestamp").getAsLong(),
				IRSPaymentState.ACCEPTED);
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse send money");
			NeoBanking.LOGGER.debug(e.getMessage());
			return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
		}
	}

	public static IRSTransaction requestMoney(String playerUUID, String fromNameOrUUID, int amount, String reference) {
		return requestMoney(playerUUID, fromNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSTransaction requestMoney(String playerUUID, String fromNameOrUUID, String amount, String reference) {
		return requestMoney(playerUUID, fromNameOrUUID, amount, reference, false);
	}
	public static IRSTransaction serverRequestMoney(String fromNameOrUUID, int amount, String reference) {
		return serverRequestMoney(fromNameOrUUID, String.valueOf(amount), reference);
	}
	public static IRSTransaction serverRequestMoney(String fromNameOrUUID, String amount, String reference) {
		return requestMoney(null, fromNameOrUUID, amount, reference, true);
	}
	private static IRSTransaction requestMoney(String playerUUID, String fromNameOrUUID, String amount, String reference, boolean fromServer) {
		try {
			if (!fromServer && (playerUUID == null || playerUUID.isEmpty())) return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
			if (fromNameOrUUID == null || fromNameOrUUID.isEmpty()) return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
			if (amount == null || amount.isEmpty()) return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
			if (reference == null || reference.isEmpty()) return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
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
				NeoBanking.LOGGER.warn("IRS#requestMoney failed. Code: {}, Message: {}", response.getStatusCode(), response.getStatusMessage());
				return switch (response.getStatusCode()) {
					case "402" -> new IRSTransaction(null, IRSPaymentState.INSUFFICIENT_FUNDS);
					case "461" -> new IRSTransaction(null, IRSPaymentState.INVALID_REFERENCE);
					case "463" -> new IRSTransaction(null, IRSPaymentState.FROM_PLAYER_INVALID);
					case "465" -> new IRSTransaction(null, IRSPaymentState.TO_FROM_PLAYER_SAME);
					case "466" -> new IRSTransaction(null, IRSPaymentState.CANNOT_REQUEST_SERVER);
					default -> new IRSTransaction(null, IRSPaymentState.UNKNOWN);
				};
			}
			return new IRSTransaction(response.getDataNode().getAsJsonObject().get("txID").getAsString(),
				new IRSPlayer(response.getDataNode().getAsJsonObject().get("from").getAsString()),
				new IRSPlayer(response.getDataNode().getAsJsonObject().get("to").getAsString()),
				response.getDataNode().getAsJsonObject().get("amount").getAsInt(),
				response.getDataNode().getAsJsonObject().get("reference").getAsString(),
				response.getDataNode().getAsJsonObject().get("timestamp").getAsLong(),
				IRSPaymentState.PENDING);
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#getLeaderboard failed to parse request money");
			NeoBanking.LOGGER.debug(e.getMessage());
			return new IRSTransaction(null, IRSPaymentState.UNKNOWN);
		}
	}

	public static String serverNewKey(String playerUUID) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return null;
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "newkey", new HashMap<>() {{
				put("apikey", apiKey);
				put("uuid", playerUUID); // Runs the api as if the specified user provided their own apikey
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#serverNewKey failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return null;
			}
			return response.getDataNode().getAsJsonObject().get("apikey").getAsString();
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#serverNewKey failed to parse new key");
			NeoBanking.LOGGER.debug(e.getMessage());
			return null;
		}
	}

	public static boolean serverCreateUser(String playerUUID) {
		try {
			if (playerUUID == null || playerUUID.isEmpty()) return false;
			APIResponse response = APIRequest.apiRequest(neoNetworkIRSEndpoint + "createuser", new HashMap<>() {{
				put("apikey", apiKey);
				put("uuid", playerUUID); // Runs the api as if the specified user provided their own apikey
			}});
			if (!response.getSuccess()) {
				NeoBanking.LOGGER.warn("IRS#serverCreateUser failed. Code: {}, Message: {}",
					response.getStatusCode(), response.getStatusMessage());
				return false;
			}
			response.getDataNode().getAsJsonObject().get("uuid").getAsString();
			return true;
		} catch (Exception e) {
			NeoBanking.LOGGER.warn("IRS#serverCreateUser failed to parse user");
			NeoBanking.LOGGER.debug(e.getMessage());
			return false;
		}
	}
}
