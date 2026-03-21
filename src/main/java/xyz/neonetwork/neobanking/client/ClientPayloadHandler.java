package xyz.neonetwork.neobanking.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.neonetwork.neobanking.gui.ScreenTest;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;

public class ClientPayloadHandler {
	public static void handleDataOnMain(final IRSClientboundPacket packet, final IPayloadContext context) {
//		context.player().createCommandSourceStack().sendSuccess(() -> Component.literal(packet.packetType()), false);
//		for (IRSTransaction t : packet.irsTransactions()) {
//			context.player().createCommandSourceStack().sendSuccess(() -> Component.literal(
//				"#" + t.getTransactionID() + " | From: " + t.getFrom().getPlayerDisplayName() + " | To: " + t.getTo().getPlayerDisplayName()
//					+ " | Amount: " + t.getAmount() + " | Ref: " + t.getReference()
//				), false);
//		}
		Minecraft.getInstance().setScreen(new ScreenTest(Component.literal("Hello there")));
	}

	public static void handleDataOnMain(final IRSServerboundPacket packet, final IPayloadContext context) {
		// Ignored on client
	}
}
