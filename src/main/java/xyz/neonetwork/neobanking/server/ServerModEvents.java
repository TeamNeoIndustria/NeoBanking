package xyz.neonetwork.neobanking.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.neonetwork.neobanking.payload.ToastPayload;

public class ServerModEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(
			ToastPayload.TYPE,
			ToastPayload.STREAM_CODEC,
			ServerPayloadHandler::handleDataOnMain
		);
	}
}
