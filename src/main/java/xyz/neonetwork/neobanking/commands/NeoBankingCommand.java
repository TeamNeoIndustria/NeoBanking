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
import xyz.neonetwork.neobanking.networking.IRS;
import xyz.neonetwork.neobanking.networking.IRSPlayer;
import xyz.neonetwork.neobanking.networking.IRSWebsocket;
import xyz.neonetwork.neobanking.payload.ToastPayload;

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
						context.getSource().sendSuccess(() -> Component.literal("Player Name From UUID: " + irsPlayer.getPlayer().getDisplayName().getString() + " (" + irsPlayer.getPlayerUUID() + ")"), false);
						return 1;
					})
				)
			).executes(context -> {
				if (context.getSource().getServer().isDedicatedServer()) {
					PacketDistributor.sendToPlayer(Objects.requireNonNull(context.getSource().getPlayer()), new ToastPayload("Test", 1));
				}
				return 1;
			})
		);
	}

}
