package xyz.neonetwork.neobanking.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;

public class BankingToast implements Toast {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "textures/gui/toast.png");
	private static final int WIDTH = 160; // Width of the vanilla toast
	private static final int HEIGHT = 32;  // Height of the vanilla toast

	private final Component title;
	private final Component message;

	public BankingToast(Component title, Component message) {
		this.title = title;
		this.message = message;
	}

	@Override
	public @NotNull Visibility render(GuiGraphics guiGraphics, @NotNull ToastComponent toastComponent, long delta) {
		guiGraphics.blit(TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT); // Draw background
		Font font = Minecraft.getInstance().font;
		guiGraphics.drawString(font, title.getString(), 33, 7, 0xFFFFFF); // Title position
		guiGraphics.drawString(font, message.getString(), 33, 17, 0xFFFFFF); // Message position
		// Logic to handle when to remove the toast
		if (delta > 5000) { // for example, display for 5 seconds
			return Visibility.HIDE; // Hide after duration
		}
		return Visibility.SHOW; // Show while active
	}
}
