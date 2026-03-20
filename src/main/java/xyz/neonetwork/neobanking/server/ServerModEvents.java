package xyz.neonetwork.neobanking.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;
import xyz.neonetwork.neobanking.packets.IRSToastPacket;

public class ServerModEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(
			IRSToastPacket.TYPE,
			IRSToastPacket.STREAM_CODEC,
			ServerPayloadHandler::handleDataOnMain
		);

		registrar.executesOn(HandlerThread.NETWORK);
		registrar.playToClient(
			IRSClientboundPacket.TYPE,
			IRSClientboundPacket.STREAM_CODEC,
			ServerPayloadHandler::handleDataOnMain
		);
		registrar.playToServer(
			IRSServerboundPacket.TYPE,
			IRSServerboundPacket.STREAM_CODEC,
			ServerPayloadHandler::handleDataOnMain
		);
	}
}
