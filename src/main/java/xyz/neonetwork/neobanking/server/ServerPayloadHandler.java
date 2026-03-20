package xyz.neonetwork.neobanking.server;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;
import xyz.neonetwork.neobanking.packets.IRSToastPacket;

public class ServerPayloadHandler {
	public static void handleDataOnMain(final IRSToastPacket IRSToastPacket, final IPayloadContext context) {
		// Ignored on server
	}

	public static void handleDataOnMain(final IRSClientboundPacket packet, final IPayloadContext context) {
		// Ignored on server
	}

	public static void handleDataOnMain(final IRSServerboundPacket packet, final IPayloadContext context) {

	}
}
