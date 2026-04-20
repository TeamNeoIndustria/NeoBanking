package xyz.neonetwork.neobanking.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import xyz.neonetwork.neobanking.Config;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.paymentprocessor.ShopResolver;
import xyz.neonetwork.neobanking.paymentprocessor.WebsocketHandler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class IRSWebsocket {
	private static final String neoNetworkWebsocketEndpoint = Config.IRS_SOCKET_ENDPOINT.get();
	private static final String apiKey = Config.IRS_SOCKET_APIKEY.get();
	private static WebSocket webSocket;
	private static int reconnectAttempts = 0;
	private static final int maxReconnectAttempts = 5;
	private static boolean shouldBeConnected = false;
	private static boolean connected = false;

	public static void connect() {
		webSocket = HttpClient.newHttpClient()
			.newWebSocketBuilder()
			.buildAsync(URI.create(neoNetworkWebsocketEndpoint), new WebSocket.Listener() {
				@Override
				public void onOpen(WebSocket webSocket) {
					WebSocket.Listener.super.onOpen(webSocket);
					NeoBanking.LOGGER.info("IRSWebsocket connected");
					reconnectAttempts = 0;
					shouldBeConnected = true;
					webSocket.sendText("{\"command\": \"auth\", \"apikey\": \"" + apiKey + "\"}", true);
				}

				@Override
				public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
					try {
//						NeoBanking.LOGGER.info("IRSWebsocket String received: {}", data.toString());
						JsonObject node = JsonParser.parseString(data.toString()).getAsJsonObject();
						switch (node.get("event").getAsString()) {
							case "error":
								NeoBanking.LOGGER.error("IRSWebsocket#onMessage error {} - {}", node.get("errorCode").getAsString(), node.get("errorMessage").getAsString());
								break;
							case "auth":
								boolean authValid = node.get("valid").getAsBoolean();
								if (!authValid) {
									NeoBanking.LOGGER.error("IRSWebsocket#onMessage auth failed");
									break;
								}
								connected = true;
								break;
							case "migrator":
								WebsocketHandler.migratorEvent(node.get("data").getAsJsonObject());
								break;
							default:
								break;
						}
					} catch (Exception e) {
						NeoBanking.LOGGER.warn("IRSWebsocket#onMessage failed to parse response");
					}
					return WebSocket.Listener.super.onText(webSocket, data, last);
				}

				@Override
				public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
					connected = false;
					NeoBanking.LOGGER.info("IRSWebsocket closed, switching to fallback");
					if (shouldBeConnected) {
						attemptReconnect();
					}
					ShopResolver.fallbackShouldProcess = true;
					return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
				}

				@Override
				public void onError(WebSocket webSocket, Throwable throwable) {
					connected = false;
					NeoBanking.LOGGER.info("There was a websocket error, whoops :S");
					attemptReconnect();
				}
			}).join();
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

	public static void close() {
		shouldBeConnected = false;
		if (webSocket != null) {
			webSocket.abort();
			webSocket = null;
		}
	}

	public static boolean isConnected() {
		return connected;
	}
}
