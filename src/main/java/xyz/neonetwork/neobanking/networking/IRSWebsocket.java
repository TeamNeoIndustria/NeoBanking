package xyz.neonetwork.neobanking.networking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.neonetwork.neobanking.Config;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.paymentprocessor.WebsocketHandler;

import java.util.concurrent.TimeUnit;

public class IRSWebsocket {
	private static final String neoNetworkWebsocketEndpoint = Config.IRS_SOCKET_ENDPOINT.get();
	private static final String apiKey = Config.IRS_SOCKET_APIKEY.get();
	private static final OkHttpClient client = new OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build();;
	private static WebSocket webSocket;
	private static int reconnectAttempts = 0;
	private static final int maxReconnectAttempts = 5;
	private static boolean shouldBeConnected = false;

	public static void connect() {
		shouldBeConnected = true;
		Request request = new Request.Builder().url(neoNetworkWebsocketEndpoint).build();
		webSocket = client.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
				NeoBanking.LOGGER.info("IRSWebsocket connected");
				reconnectAttempts = 0;
				sendMessage("{\"command\": \"auth\", \"apikey\": \"" + apiKey + "\"}");
			}

			@Override
			public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
				NeoBanking.LOGGER.info("IRSWebsocket String received: {}", text);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode node;
				try {
					node = mapper.readTree(text);
				} catch (Exception e) {
					NeoBanking.LOGGER.warn("IRSWebsocket#onMessage failed to parse response");
					return;
				}
				if (!node.has("event") || !node.get("event").isTextual()) return;
				switch (node.get("event").asText()) {
					case "error":
						if (!node.has("errorCode") || !node.has("errorMessage")) break;
						NeoBanking.LOGGER.error("IRSWebsocket#onMessage error {} - {}", node.get("errorCode").asText("!499!"), node.get("errorMessage").asText("!Missing Error Message!"));
						break;
					case "auth":
						boolean authValid = (node.has("valid") && node.get("valid").asBoolean());
						if (!authValid) {
							NeoBanking.LOGGER.error("IRSWebsocket#onMessage auth failed");
							break;
						}
						break;
					case "migrator":
						if (!node.has("data")) {
							NeoBanking.LOGGER.error("IRSWebsocket#onMessage migrator missing data node");
							break;
						}
						WebsocketHandler.migratorEvent(node.get("data"));
						break;
					default:
						break;
				}
			}

			@Override
			public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
				NeoBanking.LOGGER.info("IRSWebsocket closing");
				webSocket.close(1000, null);
			}

			@Override
			public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
				NeoBanking.LOGGER.info("IRSWebsocket closed");
				if (shouldBeConnected) {
					attemptReconnect();
				}
			}

			@Override
			public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
				NeoBanking.LOGGER.info("IRSWebsocket failed");
				attemptReconnect();
			}
		});
	}

	private static void attemptReconnect() {
		if (reconnectAttempts < maxReconnectAttempts) {
			reconnectAttempts++;
			NeoBanking.LOGGER.info("IRSWebsocket reconnecting ({}/{})",  reconnectAttempts, maxReconnectAttempts);
			new Thread(() -> {
				try {
					Thread.sleep(1000L * reconnectAttempts);
					connect();
				} catch (InterruptedException ignored) {}
			}).start();
		} else {
			NeoBanking.LOGGER.error("IRSWebsocket failed to reconnect within {} attempts", maxReconnectAttempts);
		}
	}

	public static void sendMessage(String message) {
		if (webSocket == null) {
			NeoBanking.LOGGER.warn("Attempted to send message to closed websocket");
			return;
		}
		webSocket.send(message);
	}

	public static void close() {
		shouldBeConnected = false;
		if (webSocket != null) {
			webSocket.close(1000, "Closing connection");
		}
	}
}
