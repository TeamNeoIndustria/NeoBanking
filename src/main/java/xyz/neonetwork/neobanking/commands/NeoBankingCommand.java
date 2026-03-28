package xyz.neonetwork.neobanking.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import xyz.neonetwork.neobanking.api.IRS;
import xyz.neonetwork.neobanking.api.IRSPlayer;
import xyz.neonetwork.neobanking.api.IRSTransaction;
import xyz.neonetwork.neobanking.api.IRSWebsocket;
import xyz.neonetwork.neobanking.packets.IRSClientboundPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NeoBankingCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("neobanking")
			.then(Commands.literal("newkey").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.executes(context -> {
						Player player = EntityArgument.getPlayer(context, "playerName");
						String newKey = IRS.serverNewKey(player.getStringUUID());
						if (newKey == null) {
							player.createCommandSourceStack().sendSuccess(() -> Component.literal("Failed to get new api key"), false);
							if (context.getSource().isPlayer()) context.getSource().sendSuccess(() -> Component.literal("Failed to get new api key"), false);
							return 0;
						}
						player.createCommandSourceStack().sendSuccess(() -> Component.literal("Your banking API key is '" + newKey + "' (Click to copy)").withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, newKey)).withUnderlined(true).withColor(ChatFormatting.GREEN)), false);
						return 1;
					})
				)
			).then(Commands.literal("forcereconnect").requires(source -> source.hasPermission(2))
				.executes(context -> {
					IRSWebsocket.connect();
					return 1;
				})
			).then(Commands.literal("test").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.executes(context -> {
						Player player = EntityArgument.getPlayer(context, "playerName");
						IRSPlayer irsPlayer = new IRSPlayer(player.getStringUUID());
						context.getSource().sendSuccess(() -> Component.literal("Player Name From UUID: " + irsPlayer.getPlayer().getDisplayName().getString() + " (" + irsPlayer.getPlayerUUID().toString() + ")"), false);
						return 1;
					})
				)
			).executes(context -> {
				if (context.getSource().getServer().isDedicatedServer()) {
					List<IRSTransaction> history = IRS.getTransactionHistory("02c0f072-5e8f-46f8-a400-4b1722b293f0");
					if (history.isEmpty()) return 1;
//					PacketDistributor.sendToPlayer(Objects.requireNonNull(context.getSource().getPlayer()), new IRSClientboundPacket("history", 0, history, new ArrayList<>()));
					PacketDistributor.sendToAllPlayers(new IRSClientboundPacket("history", 0, history, new ArrayList<>()));
				}
				return 1;
			})
		);
	}

}
