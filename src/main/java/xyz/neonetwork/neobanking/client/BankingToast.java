package xyz.neonetwork.neobanking.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;

public class BankingToast implements Toast {
	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "textures/gui/banktoast.png");
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
		guiGraphics.drawString(font, Component.literal(title.getString()).withStyle(ChatFormatting.BOLD), 12, 7, 0xFFFFFF);
		guiGraphics.drawString(font, message, 12, 17, 0xFFFFFF);
		if (delta > 5000) { // for example, display for 5 seconds
			return Visibility.HIDE; // Hide after duration
		}
		return Visibility.SHOW; // Show while active
	}
}
