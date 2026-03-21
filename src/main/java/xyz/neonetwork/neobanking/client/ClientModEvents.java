package xyz.neonetwork.neobanking.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;

public class ClientModEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.executesOn(HandlerThread.NETWORK);
		registrar.playToClient(
			IRSClientboundPacket.TYPE,
			IRSClientboundPacket.STREAM_CODEC,
			ClientPayloadHandler::handleDataOnMain
		);
		registrar.playToServer(
			IRSServerboundPacket.TYPE,
			IRSServerboundPacket.STREAM_CODEC,
			ClientPayloadHandler::handleDataOnMain
		);
	}
}
