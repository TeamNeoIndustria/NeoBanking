package xyz.neonetwork.neobanking.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.*;
import xyz.neonetwork.neobanking.blockitems.CoinItem;
import xyz.neonetwork.neobanking.paymentprocessor.CurrencyHandler;
import xyz.neonetwork.neolib.NeoLib;
import xyz.neonetwork.neolib.servergui.NeoServerScreen;
import xyz.neonetwork.neolib.servergui.NeoServerScreenGrid;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.utilities.NeoComponent;
import xyz.neonetwork.neolib.utilities.NeoString;

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
					IRSWebsocket.close();
					IRSWebsocket.connect();
					return 1;
				})
			).then(Commands.literal("calculate").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.executes(context -> {
						if (!context.getSource().getServer().isDedicatedServer()) return 0;
						Player player = EntityArgument.getPlayer(context, "playerName");
						int[] rawAmount = CurrencyHandler.calculateComplexInventoryValue(player);
						int amount = CurrencyHandler.calculateSimpleInventoryValue(rawAmount);
						for (int i = 0; i < CurrencyHandler.COINS.size(); i++) {
							CoinItem coinItem = CurrencyHandler.COINS.get(i);
							player.sendSystemMessage(NeoComponent.formatString("&l%s&r: %sx (%s)", coinItem.getName(new ItemStack(coinItem)).getString(), rawAmount[i], NeoString.formatCurrency(rawAmount[i] * CurrencyHandler.COINS.get(i).getValue())));
						}
						player.sendSystemMessage(NeoComponent.formatString("&lTotal&r: %s", NeoString.formatCurrency(amount)));
						return 1;
					})
				)
			).then(Commands.literal("calculateraw").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.executes(context -> {
						if (!context.getSource().getServer().isDedicatedServer()) return 0;
						Player player = EntityArgument.getPlayer(context, "playerName");
						int amount = CurrencyHandler.calculateSimpleInventoryValue(player);
						context.getSource().sendSuccess(() -> Component.literal(String.valueOf(amount)), false);
						return 1;
					})
				)
			).then(Commands.literal("givecoin").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							boolean success = CurrencyHandler.addValueToInventory(player, amount);
							if (!success) {
								context.getSource().sendFailure(Component.literal("&cFailed to add value to inventory."));
								return 0;
							}
							if (context.getSource().getPlayer() == null || context.getSource().getPlayer().getUUID() != player.getUUID()) {
								context.getSource().sendSuccess(() -> NeoComponent.formatString("&7Added &6%s &7to &6%s&7's inventory.", NeoString.formatCurrency(amount), player.getScoreboardName()), false);
							}
							player.sendSystemMessage(NeoComponent.formatString("&7Added &6%s &7to your inventory.", NeoString.formatCurrency(amount)));
							return 1;
						})
					)
				)
			).then(Commands.literal("takecoin").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							boolean success = CurrencyHandler.removeValueFromInventory(player, amount);
							if (!success) {
								context.getSource().sendFailure(Component.literal("&cFailed to remove value from inventory."));
								return 0;
							}
							if (context.getSource().getPlayer() == null || context.getSource().getPlayer().getUUID() != player.getUUID()) {
								context.getSource().sendSuccess(() -> NeoComponent.formatString("&7Removed &6%s &7from &6%s&7's inventory.", NeoString.formatCurrency(amount), player.getScoreboardName()), false);
							}
							player.sendSystemMessage(NeoComponent.formatString("&7Removed &6%s &7from your inventory.", NeoString.formatCurrency(amount)));
							return 1;
						})
					)
				)
			).then(Commands.literal("giveirs").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							if (amount < 1) {
								// Fail
								return 0;
							}
							IRSTransaction transaction = IRS.serverSendMoney(player.getStringUUID(), amount, "*Manual Operator Transaction*");
							if (transaction.getState() != IRSPaymentState.ACCEPTED) {
								context.getSource().sendFailure(Component.literal("&cFailed to add value to bank balance."));
								return 0;
							}
							if (context.getSource().getPlayer() == null || context.getSource().getPlayer().getUUID() != player.getUUID()) {
								context.getSource().sendSuccess(() -> NeoComponent.formatString("&7Added &6%s &7to &6%s&7's bank balance.", NeoString.formatCurrency(amount), player.getScoreboardName()), false);
							}
							player.sendSystemMessage(NeoComponent.formatString("&7Manually added &6%s &7to your bank balance.", NeoString.formatCurrency(amount)));
							return 1;
						})
					)
				)
			).then(Commands.literal("takeirs").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							if (amount < 1) {
								context.getSource().sendFailure(Component.literal("&cAmount must be greater than 0."));
								return 0;
							}
							IRSTransaction transaction = IRS.serverReceiveMoney(player.getStringUUID(), amount, "*Manual Operator Transaction*");
							if (transaction.getState() != IRSPaymentState.ACCEPTED) {
								context.getSource().sendFailure(Component.literal("&cFailed to take value from bank balance."));
								return 0;
							}
							if (context.getSource().getPlayer() == null || context.getSource().getPlayer().getUUID() != player.getUUID()) {
								context.getSource().sendSuccess(() -> NeoComponent.formatString("&7Removed &6%s &7from &6%s&7's bank balance.", NeoString.formatCurrency(amount), player.getScoreboardName()), false);
							}
							player.sendSystemMessage(NeoComponent.formatString("&7Manually removed &6%s &7from your bank balance.", NeoString.formatCurrency(amount)));
							return 1;
						})
					)
				)
			)
		);
	}

}
