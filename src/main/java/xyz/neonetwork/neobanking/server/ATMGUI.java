package xyz.neonetwork.neobanking.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRS;
import xyz.neonetwork.neobanking.api.IRSPaymentState;
import xyz.neonetwork.neobanking.paymentprocessor.CurrencyHandler;
import xyz.neonetwork.neolib.gui.EditBoxType;
import xyz.neonetwork.neolib.gui.NeoStringAlign;
import xyz.neonetwork.neolib.servergui.NeoServerScreen;
import xyz.neonetwork.neolib.servergui.NeoServerScreenGrid;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.utilities.NeoString;

import java.util.List;

public class ATMGUI {

	private static final NeoTexture texture = NeoTexture.BANK;

	public static void showHomePage(@NotNull ServerPlayer player) {
		int balance = IRS.getUserBalance(player.getStringUUID());
		if (balance < 0) {
			showErrorPage(player, "You were not found in the banking system.");
			return;
		}
		new NeoServerScreen(player, Component.literal("ATM - " + player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 12, 10)
				.addStringWidget(0, 0, 12, 1, "title", Component.literal("Home Page").withStyle(ChatFormatting.BOLD))
				.addButtonWidget(1, 4, 4, 1, "deposit", Component.literal("Deposit"), null, false, (finalScreen, finalGrid) -> {
					showDepositPage(player);
				})
				.addButtonWidget(7, 4, 4, 1, "withdraw", Component.literal("Withdraw"), null, false, (finalScreen, finalGrid) -> {
					showWithdrawPage(player);
				})
				.addButtonWidget(0, 9, 2, 1, "exit", Component.literal("Exit"), null, false, (finalScreen, finalGrid) -> {
					finalScreen.close();
				}),
			(finalPlayer, finalGrid) -> {

			}
		).show(true);
	}

	public static void showDepositPage(@NotNull ServerPlayer player) {
		int inventoryAmount = CurrencyHandler.calculateSimpleInventoryValue(player);
		NeoServerScreenGrid grid = new NeoServerScreenGrid(20, 20, 2, 12, 10)
			.addStringWidget(0, 0, 12, 1, "title", Component.literal("Deposit Coins"))
			.addButtonWidget(0, 9, 2, 1, "back", Component.literal("Back"), null, false, (finalScreen, finalGrid) -> {
				showHomePage(player);
			}).addButtonWidget(1, 2, 4, 1, "deposit1", Component.literal("Deposit " + NeoString.formatCurrency(1)), null, inventoryAmount < 1, (finalScreen, finalGrid) -> {
				depositCoins(player, 1);
			}).addButtonWidget(7, 2, 4, 1, "deposit5", Component.literal("Deposit " + NeoString.formatCurrency(5)), null, inventoryAmount < 5, (finalScreen, finalGrid) -> {
				depositCoins(player, 5);
			}).addButtonWidget(1, 4, 4, 1, "deposit10", Component.literal("Deposit " + NeoString.formatCurrency(10)), null, inventoryAmount < 10, (finalScreen, finalGrid) -> {
				depositCoins(player, 10);
			}).addButtonWidget(7, 4, 4, 1, "deposit25", Component.literal("Deposit " + NeoString.formatCurrency(25)), null, inventoryAmount < 25, (finalScreen, finalGrid) -> {
				depositCoins(player, 25);
			}).addButtonWidget(1, 6, 4, 1, "deposit50", Component.literal("Deposit " + NeoString.formatCurrency(50)), null, inventoryAmount < 50, (finalScreen, finalGrid) -> {
				depositCoins(player, 50);
			}).addButtonWidget(7, 6, 4, 1, "deposit100", Component.literal("Deposit " + NeoString.formatCurrency(100)), null, inventoryAmount < 100, (finalScreen, finalGrid) -> {
				depositCoins(player, 100);
			}).addButtonWidget(7, 8, 4, 1, "depositall", Component.literal("Deposit All"), null, inventoryAmount < 1, (finalScreen, finalGrid) -> {
				depositCoins(player, inventoryAmount);
			});
		new NeoServerScreen(player, Component.literal("ATM - " + player.getScoreboardName() + "'s Bank Account"), texture, grid,
			(finalPlayer, finalGrid) -> {

			}
		).show(true);
	}

	public static void depositCoins(ServerPlayer player, int amount) {
		if (!CurrencyHandler.removeValueFromInventory(player, amount)) {
			showErrorPage(player, List.of(Component.literal("You do not have enough currency in your inventory.")));
			return;
		}
		showProcessingPage(player);
		if (IRS.serverSendMoney(player.getStringUUID(), amount, "*ATM Deposit*").getState() != IRSPaymentState.ACCEPTED) {
			showErrorPage(player, "There was an error processing the transaction of " + NeoString.formatCurrency(amount) + ".");
			return;
		}
		showMessagePage(player, Component.literal("Deposited " + NeoString.formatCurrency(amount) + " into your account."));
	}

	public static void showWithdrawPage(@NotNull ServerPlayer player) {
		int balance = IRS.getUserBalance(player.getStringUUID());
		if (balance == -1) {
			showErrorPage(player, "There was an issue loading your balance.");
			return;
		}
		NeoServerScreenGrid grid = new NeoServerScreenGrid(20, 20, 2, 12, 10)
			.addStringWidget(0, 0, 12, 1, "title", Component.literal("Withdraw Coins - Balance: " + NeoString.formatCurrency(balance)))
			.addButtonWidget(0, 9, 2, 1, "back", Component.literal("Back"), null, false, (finalScreen, finalGrid) -> {
				showHomePage(player);
			}).addButtonWidget(1, 2, 4, 1, "withdraw1", Component.literal("Withdraw " + NeoString.formatCurrency(1)), null, balance < 1, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 1, balance);
			}).addButtonWidget(7, 2, 4, 1, "withdraw5", Component.literal("Withdraw " + NeoString.formatCurrency(5)), null, balance < 5, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 5, balance);
			}).addButtonWidget(1, 4, 4, 1, "withdraw10", Component.literal("Withdraw " + NeoString.formatCurrency(10)), null, balance < 10, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 10, balance);
			}).addButtonWidget(7, 4, 4, 1, "withdraw25", Component.literal("Withdraw " + NeoString.formatCurrency(25)), null, balance < 25, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 25, balance);
			}).addButtonWidget(1, 6, 4, 1, "withdraw50", Component.literal("Withdraw " + NeoString.formatCurrency(50)), null, balance < 50, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 50, balance);
			}).addButtonWidget(7, 6, 4, 1, "withdraw100", Component.literal("Withdraw " + NeoString.formatCurrency(100)), null, balance < 100, (finalScreen, finalGrid) -> {
				withdrawCoins(player, 100, balance);
			}).addStringWidget(2, 8, 3, 1, "withdrawtext", Component.literal("Custom").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.RIGHT, NeoStringAlign.Vertical.MIDDLE)
			.addEditBoxWidget(5, 8, 3, 1, "withdrawcustom", Component.literal(""), 9, EditBoxType.UINT)
			.addButtonWidget(8, 8, 3, 1, "submit", Component.literal("Withdraw"), null, balance < 1, (finalScreen, finalGrid) -> {
				try {
					String withdrawRawAmount = finalGrid.get("withdrawcustom");
					int withdrawAmount = Integer.parseInt(withdrawRawAmount);
					withdrawCoins(player, withdrawAmount, balance);
				} catch (Exception e) {
					// oopsie poopsie, fucky wucky
				}
			});
		new NeoServerScreen(player, Component.literal("ATM - " + player.getScoreboardName() + "'s Bank Account"), texture, grid,
			(finalPlayer, finalGrid) -> {

			}
		).show(true);
	}

	public static void withdrawCoins(ServerPlayer player, int amount, int balance) {
		if (amount > balance) {
			showErrorPage(player, List.of(Component.literal("You do not have enough currency in your account.")));
			return;
		}
		IRSPaymentState paymentState = IRS.serverReceiveMoney(player.getStringUUID(), amount, "*ATM Withdrawal*").getState();
		switch (paymentState) {
			case ACCEPTED:
				CurrencyHandler.addValueToInventory(player, amount);
				showMessagePage(player, Component.literal("Withdrew " + NeoString.formatCurrency(amount) + " from your account."));
				return;
			case INSUFFICIENT_FUNDS:
				showErrorPage(player, List.of(Component.literal("You do not have enough currency in your account.")));
				return;
			default:
				showErrorPage(player, "There was an error processing the transaction of " + NeoString.formatCurrency(amount) + ".");
		}
	}

	public static void showProcessingPage(@NotNull ServerPlayer player) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 12, 10)
				.addStringWidget(0, 2, 12, 7, "message", Component.literal("Processing..."), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showMessagePage(@NotNull ServerPlayer player, Component message) {
		showMessagePage(player, List.of(message));
	}

	public static void showMessagePage(@NotNull ServerPlayer player, List<Component> message) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 12, 10)
				.addStringWidget(0, 2, 12, 7, "message", message, NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12)
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showErrorPage(@NotNull ServerPlayer player, String errorMessage) {
		showErrorPage(player, List.of(Component.literal("Tell an admin the following error message").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), Component.literal(errorMessage)));
	}

	public static void showErrorPage(@NotNull ServerPlayer player, Component errorMessage) {
		showErrorPage(player, List.of(Component.literal("Tell an admin the following error message").withStyle(ChatFormatting.BOLD, ChatFormatting.RED), errorMessage));
	}

	public static void showErrorPage(@NotNull ServerPlayer player, List<Component> errorMessage) {
		NeoBanking.LOGGER.warn("Error thrown in BankGUI for player ({}) message: {}", player.getScoreboardName(), errorMessage);
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 12, 10)
				.addStringWidget(0, 0, 12, 1, "title", Component.literal("Oops, there was an error!").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
				.addStringWidget(0, 2, 12, 7, "message", errorMessage, NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12)
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}
}
