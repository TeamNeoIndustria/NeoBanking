package xyz.neonetwork.neobanking.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.neonetwork.neobanking.payload.ToastPayload;

public class ClientPayloadHandler {
	public static void handleDataOnMain(final ToastPayload toastPayload, final IPayloadContext context) {
		String title = toastPayload.title();
		String message = toastPayload.message();
		if (title == null || message == null) return;

		Toast toast = new BankingToast(Component.literal(title), Component.literal(message));
		Minecraft.getInstance().getToasts().addToast(toast);
	}
}
