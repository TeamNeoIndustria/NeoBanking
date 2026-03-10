package xyz.neonetwork.neobanking.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import xyz.neonetwork.neobanking.networking.IRS;

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
					}))));
	}

}
