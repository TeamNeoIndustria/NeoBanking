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

import java.util.List;

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
			).then(Commands.literal("tellerdeposit").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							int balance = CurrencyHandler.calculateSimpleInventoryValue(player);
							if (amount == -1) {
								amount = balance;
							} else if (amount < 1) {
								context.getSource().sendFailure(NeoComponent.formatString("&cAmount must be greater than 0."));
								// Target player, there was an unknown error
								player.sendSystemMessage(NeoComponent.formatString("&cFailed to deposit as amount was less than 1. Please inform an admin."));
								return 0;
							}
							if (!CurrencyHandler.removeValueFromInventory(player, amount)) {
								// Target player, not enough currency in inv
								player.sendSystemMessage(NeoComponent.formatString("Bank Clerk : &cYou don't have enough coins to deposit that much."));
								return 0;
							}
							if (IRS.serverSendMoney(player.getStringUUID(), amount, "*Teller Deposit*").getState() != IRSPaymentState.ACCEPTED) {
								// Target player, failed processing
								player.sendSystemMessage(NeoComponent.formatString("&cThere was an issue adding the amount to your account. Please inform an admin."));
								NeoBanking.LOGGER.warn("There was an issue adding {} to {}'s bank account", NeoString.formatCurrency(amount), player.getScoreboardName());
								return 0;
							}
							// Target player, Deposit complete
							player.sendSystemMessage(NeoComponent.formatString("Bank Clerk : &a%s &rhas been deposited into your bank account.", NeoString.formatCurrency(amount)));
							return 1;
						})
					)
				)
			)
			.then(Commands.literal("tellerwithdraw").requires(source -> source.hasPermission(2))
				.then(Commands.argument("playerName", EntityArgument.player())
					.then(Commands.argument("amount", IntegerArgumentType.integer())
						.executes(context -> {
							if (!context.getSource().getServer().isDedicatedServer()) return 0;
							Player player = EntityArgument.getPlayer(context, "playerName");
							int amount = context.getArgument("amount", Integer.class);
							if (amount < 1) {
								context.getSource().sendFailure(NeoComponent.formatString("&cAmount must be greater than 0."));
								// Target player, there was an unknown error
								player.sendSystemMessage(NeoComponent.formatString("&cFailed to deposit as amount was less than 1. Please inform an admin."));
								return 0;
							}
							int balance = IRS.getUserBalance(player.getStringUUID());
							if (amount > balance) {
								// Target player, not enough balance
								player.sendSystemMessage(NeoComponent.formatString("Bank Clerk : &cYou don't have enough balance to withdraw that much."));
								return 0;
							}
							IRSPaymentState paymentState = IRS.serverReceiveMoney(player.getStringUUID(), amount, "*Teller Withdrawal*").getState();
							switch (paymentState) {
								case ACCEPTED:
									CurrencyHandler.addValueToInventory(player, amount);
									// Target player, withdrew moneys
									player.sendSystemMessage(NeoComponent.formatString("Bank Clerk : &a%s &rhas been withdrawn from your bank account.", NeoString.formatCurrency(amount)));
									return 1;
								case INSUFFICIENT_FUNDS:
									// Target player, you're too poor
									player.sendSystemMessage(NeoComponent.formatString("Bank Clerk : &cYou don't have enough balance to withdraw that much."));
									return 0;
							}
							return 0;
						})
					)
				)
			)
		);
	}

}
