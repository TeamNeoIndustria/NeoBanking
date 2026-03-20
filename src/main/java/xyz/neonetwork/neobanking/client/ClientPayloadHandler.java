package xyz.neonetwork.neobanking.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neobanking.gui.ScreenTest;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;
import xyz.neonetwork.neobanking.packets.IRSToastPacket;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.toast.NeoToast;
import xyz.neonetwork.neolib.utilities.NeoString;

public class ClientPayloadHandler {
	public static void handleDataOnMain(final IRSToastPacket toastPacket, final IPayloadContext context) {
		String packetType = toastPacket.packetType();
		IRSTransaction transaction = toastPacket.transaction();
		if (packetType == null || transaction == null) return;
		MutableComponent toastLine1;
		MutableComponent toastLine2;
		switch (packetType) {
			case "request":
				String toLine = NeoString.formatCurrency(transaction.getAmount()) + " to " + transaction.getTo().getPlayerDisplayName();
				if (Minecraft.getInstance().font.width(toLine) > 122) {
					toLine = Minecraft.getInstance().font.plainSubstrByWidth(toLine, 116) + "...";
				}
				toastLine1 = Component.literal("Payment Request");
				toastLine2 = Component.literal(toLine);
				break;
			case "send":
				String fromLine = NeoString.formatCurrency(transaction.getAmount()) + " from " + transaction.getFrom().getPlayerDisplayName();
				if (Minecraft.getInstance().font.width(fromLine) > 136) {
					fromLine = Minecraft.getInstance().font.plainSubstrByWidth(fromLine, 130) + "...";
				}
				toastLine1 = Component.literal("Payment Received");
				toastLine2 = Component.literal(fromLine);
				break;
			default:
				return;
		}
		Toast toast = new NeoToast(toastLine1.withStyle(ChatFormatting.BOLD), toastLine2, NeoTexture.RESEARCH, 5000);
		Minecraft.getInstance().getToasts().addToast(toast);
	}

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
