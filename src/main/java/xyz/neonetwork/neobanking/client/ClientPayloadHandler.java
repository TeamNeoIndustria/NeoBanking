package xyz.neonetwork.neobanking.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.gui.ScreenGrid;
import xyz.neonetwork.neobanking.gui.ScreenTest;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;
import xyz.neonetwork.neobanking.packets.IRSServerboundPacket;

import java.util.UUID;

public class ClientPayloadHandler {
	public static void handleDataOnMain(final IRSClientboundPacket packet, final IPayloadContext context) {
		Minecraft.getInstance().setScreen(new ScreenTest(UUID.randomUUID(), Component.literal("Hello there, this is a really long dymaic title!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
			new ScreenGrid(64, 16, 2, 2, 8)
				.addEditBoxWidget(0, 0, 2, 1, "ihaveaname", Component.literal("Placeholder"), 16)
				.addStringWidget(0, 1, 2, 1, "placeholderthingy", Component.literal("Test title that is wayyyyyyyyyy too long").withStyle(ChatFormatting.AQUA))
				.addButtonWidget(0, 2, 2, 1, "imabutton", Component.literal("Button text").withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE), Component.literal("This is an example tooltip").withStyle(ChatFormatting.OBFUSCATED), (grid, button) -> {
					NeoBanking.LOGGER.info("Button pressed yay!");
					String text = grid.getEditBoxValue("ihaveaname");
					if (text == null) return;
					NeoBanking.LOGGER.info("Contents of text box (ihaveaname): {}", text);
				})
				.addItemWidget(0, 3, 1, 1, "myitem", BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")))
				.addItemWidget(1, 3, 1, 4, "myitem2", BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt")))
				.addButtonWidget(0, 7, 2, 1, "finalbutton", Component.literal("I am a button").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), null, (grid, button) -> {})
		));
	}

	public static void handleDataOnMain(final IRSServerboundPacket packet, final IPayloadContext context) {
		// Ignored on client
	}
}
