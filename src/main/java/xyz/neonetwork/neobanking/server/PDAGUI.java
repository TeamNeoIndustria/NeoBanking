package xyz.neonetwork.neobanking.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.*;
import xyz.neonetwork.neolib.gui.EditBoxType;
import xyz.neonetwork.neolib.gui.NeoStringAlign;
import xyz.neonetwork.neolib.servergui.NeoServerScreen;
import xyz.neonetwork.neolib.servergui.NeoServerScreenGrid;
import xyz.neonetwork.neolib.textures.NeoTexture;
import xyz.neonetwork.neolib.utilities.NeoComponent;
import xyz.neonetwork.neolib.utilities.NeoString;

import java.util.List;

public class PDAGUI {

	private static final NeoTexture texture = NeoTexture.BANK;

	public static void showHomePage(@NotNull ServerPlayer player) {
		int balance = IRS.getUserBalance(player.getStringUUID());
		if (balance < 0) {
			showErrorPage(player, "You were not found in the banking system.");
			return;
		}
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Home Page").withStyle(ChatFormatting.BOLD))
				.addStringWidget(1, 1, 6, 2, "balance", List.of(Component.literal("Your Balance").withStyle(ChatFormatting.BOLD), Component.literal(NeoString.formatCurrency(balance)).withStyle(ChatFormatting.GREEN)), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.BOTTOM, 12)
				.addButtonWidget(1, 3, 6, 1, "history", Component.literal("History"), null, false, (finalScreen, finalGrid) -> {
					showHistoryPage(player, null, 0);
				})
				.addButtonWidget(1, 4, 6, 1, "leaderboard", Component.literal("Leaderboard"), null, false, (finalScreen, finalGrid) -> {
					showLeaderboardPage(player);
				})
				.addButtonWidget(1, 5, 6, 1, "send", Component.literal("Send Money"), null, false, (finalScreen, finalGrid) -> {
					showSendMoneyPage(player);
				})
				.addButtonWidget(1, 6, 6, 1, "request", Component.literal("Request Money"), null, false, (finalScreen, finalGrid) -> {
					showRequestMoneyPage(player);
				})
				.addButtonWidget(1, 7, 6, 1, "pending", Component.literal("Pending"), null, false, (finalScreen, finalGrid) -> {
					showPendingPage(player, null, 0);
				})
				.addButtonWidget(0, 9, 2, 1, "exit", Component.literal("Exit"), null, false, (finalScreen, finalGrid) -> {
					finalScreen.close();
				}),
			(finalPlayer, finalGrid) -> {

			}
		).show(true);
	}

	public static void showHistoryPage(@NotNull ServerPlayer player, List<IRSTransaction> history, int page) {
		List<IRSTransaction> transactionHistory;
		if (history == null) {
			transactionHistory = IRS.getTransactionHistory(player.getStringUUID(), 100);
		} else {
			transactionHistory = history;
		}
		if (transactionHistory == null) {
			showErrorPage(player, "There was an error loading your transaction history.");
			return;
		}
		if (transactionHistory.isEmpty()) {
			showMessagePage(player, Component.literal("No historical transactions found."));
			return;
		} else if (page >= transactionHistory.size()) {
			showErrorPage(player, "Tried to access invalid transaction history page.");
			return;
		}
		IRSTransaction currentHistory = transactionHistory.get(page);
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Transaction History").withStyle(ChatFormatting.BOLD))
				.addStringWidget(0, 1, 4, 2, "from", List.of(Component.literal("From").withStyle(ChatFormatting.BOLD), Component.literal(currentHistory.getFrom().getPlayerDisplayName()).withStyle(ChatFormatting.GREEN)), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.BOTTOM)
				.addStringWidget(4, 1, 4, 2, "to", List.of(Component.literal("To").withStyle(ChatFormatting.BOLD), Component.literal(currentHistory.getTo().getPlayerDisplayName()).withStyle(ChatFormatting.GREEN)), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.BOTTOM)
				.addStringWidget(0, 3, 4, 2, "amount", List.of(Component.literal("Amount").withStyle(ChatFormatting.BOLD), Component.literal(NeoString.formatCurrency(currentHistory.getAmount())).withStyle(ChatFormatting.GREEN)))
				.addStringWidget(4, 3, 4, 2, "id", List.of(Component.literal("Tx ID").withStyle(ChatFormatting.BOLD), Component.literal(currentHistory.getTransactionID()).withStyle(ChatFormatting.GREEN)))
				.addStringWidget(0, 5, 8, 3, "reference", List.of(Component.literal("Reference").withStyle(ChatFormatting.BOLD), Component.literal(currentHistory.getReference()).withStyle(ChatFormatting.GREEN)), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP)
				.addStringWidget(4, 8, 4, 1, "pagenumber", List.of(Component.literal("Page").withStyle(ChatFormatting.BOLD), Component.literal((page + 1) + "/" + transactionHistory.size())))
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				})
				.addButtonWidget(4, 9, 2, 1, "pageback", Component.literal("<"), null, page == 0, (finalScreen, finalGrid) -> {
					showHistoryPage(player, transactionHistory, page - 1);
				})
				.addButtonWidget(6, 9, 2, 1, "pageforward", Component.literal(">"), null, page + 1 >= transactionHistory.size(), (finalScreen, finalGrid) -> {
					showHistoryPage(player, transactionHistory, page + 1);
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showLeaderboardPage(@NotNull ServerPlayer player) {
		List<IRSLeaderboardEntry> leaderboard = IRS.getLeaderboard();
		if (leaderboard == null) {
			showErrorPage(player, "There was an issue getting the leaderboard.");
			return;
		}

		NeoServerScreenGrid grid = new NeoServerScreenGrid(20, 20, 2, 8, 10)
			.addStringWidget(0, 0, 8, 1, "title", Component.literal("Leaderboard").withStyle(ChatFormatting.BOLD))
			.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
				showHomePage(player);
			});

		int leaderboardSize = Math.min(leaderboard.size(), 3);
		switch(leaderboardSize) {
			case 3:
				grid.addStringWidget(0, 5, 8, 2, "place3",
					List.of(Component.literal("#3 - " + leaderboard.get(2).getPlayer().getPlayerDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), Component.literal(NeoString.formatCurrency(leaderboard.get(2).getBalance()))),
					NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.MIDDLE, 12);
			case 2:
				grid.addStringWidget(0, 3, 8, 2, "place2",
					List.of(Component.literal("#2 - " + leaderboard.get(1).getPlayer().getPlayerDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), Component.literal(NeoString.formatCurrency(leaderboard.get(1).getBalance()))),
					NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.MIDDLE, 12);
			case 1:
				grid.addStringWidget(0, 1, 8, 2, "place1",
					List.of(Component.literal("#1 - " + leaderboard.get(0).getPlayer().getPlayerDisplayName()).withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), Component.literal(NeoString.formatCurrency(leaderboard.get(0).getBalance()))),
					NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.MIDDLE, 12);
				break;
			default:
				showMessagePage(player, Component.literal("The leaderboard contains no entries."));
				break;
		}

		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture, grid, (finalPlayer, finalGrid) -> {}

		).show(true);
	}

	public static void showSendMoneyPage(@NotNull ServerPlayer player) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Send Money").withStyle(ChatFormatting.BOLD))
				.addStringWidget(1, 1, 6, 1, "labelto", Component.literal("To").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addEditBoxWidget(1, 2, 6, 1, "to", Component.literal(""), 36)
				.addStringWidget(1, 3, 6, 1, "labelamount", Component.literal("Amount").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addEditBoxWidget(1, 4, 6, 1, "amount", Component.literal(""), 9, EditBoxType.UINT)
				.addStringWidget(1, 5, 6, 1, "labelreference", Component.literal("Reference").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addMultiLineEditBox(1, 6, 6, 2, "reference", Component.literal(""), 64, EditBoxType.ASCII)
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				})
				.addButtonWidget(4, 9, 4, 1, "submit", Component.literal("Send"), null, false, (finalScreen, finalGrid) -> {
					String to = finalGrid.get("to");
					String amountRaw = finalGrid.get("amount");
					String reference = finalGrid.get("reference");
					if (to == null || to.isBlank() || amountRaw == null || amountRaw.isBlank() || reference == null) return;
					int amount = 0;
					try {
						amount = Integer.parseInt(amountRaw);
					} catch (Exception ignored) {
						showErrorPage(player, List.of(Component.literal("There was an issue processing your payment").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal(IRSPaymentState.INVALID_AMOUNT.getDescription())));
						return;
					}
					if (reference.isBlank()) {
						showErrorPage(player, List.of(Component.literal("There was an issue processing your payment").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal("No text in reference")));
						return;
					}
					showProcessingPage(player);
					IRSTransaction transaction = IRS.sendMoney(player.getStringUUID(), to, amount, reference);
					if (transaction.isValid() && transaction.getState() == IRSPaymentState.ACCEPTED) {
						showMessagePage(player, List.of(Component.literal("Payment sent!").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), Component.literal("Your payment of ").append(Component.literal(NeoString.formatCurrency(amount)).withStyle(ChatFormatting.GREEN)).append(Component.literal(" has been sent to ")).append(Component.literal(transaction.getTo().getPlayerDisplayName()).withStyle(ChatFormatting.GREEN)), Component.literal(""), Component.literal("Transaction ID: " + transaction.getTransactionID()).withStyle(ChatFormatting.DARK_GRAY)));
					} else {
						showErrorPage(player, List.of(Component.literal("There was an issue processing your payment").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal(transaction.getState().getDescription())));
					}
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showRequestMoneyPage(@NotNull ServerPlayer player) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Request Money").withStyle(ChatFormatting.BOLD))
				.addStringWidget(1, 1, 6, 1, "labelfrom", Component.literal("From").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addEditBoxWidget(1, 2, 6, 1, "from", Component.literal(""), 36)
				.addStringWidget(1, 3, 6, 1, "labelamount", Component.literal("Amount").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addEditBoxWidget(1, 4, 6, 1, "amount", Component.literal(""), 9, EditBoxType.UINT)
				.addStringWidget(1, 5, 6, 1, "labelreference", Component.literal("Reference").withStyle(ChatFormatting.BOLD), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.BOTTOM)
				.addMultiLineEditBox(1, 6, 6, 2, "reference", Component.literal(""), 64, EditBoxType.ASCII)
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				})
				.addButtonWidget(4, 9, 4, 1, "submit", Component.literal("Request"), null, false, (finalScreen, finalGrid) -> {
					String from = finalGrid.get("from");
					String amountRaw = finalGrid.get("amount");
					String reference = finalGrid.get("reference");
					if (from == null || from.isBlank() || amountRaw == null || amountRaw.isBlank() || reference == null) return;
					int amount = 0;
					try {
						amount = Integer.parseInt(amountRaw);
					} catch (Exception ignored) {
						showErrorPage(player, List.of(Component.literal("There was an issue making your payment request").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal(IRSPaymentState.INVALID_AMOUNT.getDescription())));
						return;
					}
					if (reference.isBlank()) {
						showErrorPage(player, List.of(Component.literal("There was an issue processing your payment").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal("No text in reference")));
						return;
					}
					showProcessingPage(player);
					IRSTransaction transaction = IRS.requestMoney(player.getStringUUID(), from, amount, reference);
					if (transaction.isValid() && transaction.getState() == IRSPaymentState.PENDING) {
						showMessagePage(player, List.of(Component.literal("Request sent!").withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN), Component.literal("Your request of ").append(Component.literal(NeoString.formatCurrency(amount)).withStyle(ChatFormatting.GREEN)).append(Component.literal(" has been sent to ")).append(Component.literal(transaction.getFrom().getPlayerDisplayName()).withStyle(ChatFormatting.GREEN)), Component.literal(""), Component.literal("Transaction ID: " + transaction.getTransactionID()).withStyle(ChatFormatting.DARK_GRAY)));
					} else {
						showErrorPage(player, List.of(Component.literal("There was an issue making your payment request").withStyle(ChatFormatting.RED), Component.literal(""), Component.literal(transaction.getState().getDescription())));
					}
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	private static final int pageSize = 5;

	public static void showPendingPage(@NotNull ServerPlayer player, List<IRSTransaction> pending, int page) {
		List<IRSTransaction> pendingTransactions;
		if (pending == null) {
			pendingTransactions = IRS.getPendingTransactions(player.getStringUUID());
			page = 0;
		} else {
			pendingTransactions = pending;
		}

		if (pendingTransactions == null) {
			showErrorPage(player, "There was an error loading your pending transactions.");
			return;
		}
		if (pendingTransactions.isEmpty()) {
			showMessagePage(player, Component.literal("No pending transactions here :)"));
			return;
		}
		int indexMin = page * pageSize;
		int indexMax = indexMin + pageSize;
		int maxPage = (int) (Math.ceil((double) pendingTransactions.size() / pageSize)); // Visually (counting from 1)
		List<IRSTransaction> currentTransactionPage = pendingTransactions.subList(indexMin, Math.min(indexMax, pendingTransactions.size()));

		int finalPage = page;
		NeoServerScreenGrid grid = new NeoServerScreenGrid(20, 20, 2, 8, 10)
			.addStringWidget(0, 0, 8, 1, "title", Component.literal("Pending Transactions").withStyle(ChatFormatting.BOLD))
			.addStringWidget(4, 8, 4, 1, "pagenumber", List.of(Component.literal("Page").withStyle(ChatFormatting.BOLD), Component.literal((page + 1) + "/" + maxPage)))
			.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
				showHomePage(player);
			})
			.addButtonWidget(4, 9, 2, 1, "pageback", Component.literal("<"), null, page == 0, (finalScreen, finalGrid) -> {
				showPendingPage(player, pendingTransactions, finalPage - 1);
			})
			.addButtonWidget(6, 9, 2, 1, "pageforward", Component.literal(">"), null, page == maxPage - 1, (finalScreen, finalGrid) -> {
				showPendingPage(player, pendingTransactions, finalPage + 1);
			});
		switch (currentTransactionPage.size()) {
			case 5:
				grid.addStringWidget(2, 6, 6, 1, "item5", List.of(NeoComponent.formatString("&lTo &r&a%s", currentTransactionPage.get(4).getTo().getPlayerDisplayName()), NeoComponent.formatString("&lAmount &r&a%s", NeoString.formatCurrency(currentTransactionPage.get(4).getAmount()))), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.MIDDLE)
					.addButtonWidget(0, 6, 2, 1, "button5", Component.literal("Details"), null, false, (finalScreen, finalGrid) -> {
						showPendingDetailsPage(player, currentTransactionPage.get(4));
				});
			case 4:
				grid.addStringWidget(2, 5, 6, 1, "item4", List.of(NeoComponent.formatString("&lTo &r&a%s", currentTransactionPage.get(3).getTo().getPlayerDisplayName()), NeoComponent.formatString("&lAmount &r&a%s", NeoString.formatCurrency(currentTransactionPage.get(3).getAmount()))), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.MIDDLE)
					.addButtonWidget(0, 5, 2, 1, "button4", Component.literal("Details"), null, false, (finalScreen, finalGrid) -> {
						showPendingDetailsPage(player, currentTransactionPage.get(3));
				});
			case 3:
				grid.addStringWidget(2, 4, 6, 1, "item3", List.of(NeoComponent.formatString("&lTo &r&a%s", currentTransactionPage.get(2).getTo().getPlayerDisplayName()), NeoComponent.formatString("&lAmount &r&a%s", NeoString.formatCurrency(currentTransactionPage.get(2).getAmount()))), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.MIDDLE)
					.addButtonWidget(0, 4, 2, 1, "button3", Component.literal("Details"), null, false, (finalScreen, finalGrid) -> {
						showPendingDetailsPage(player, currentTransactionPage.get(2));
				});
			case 2:
				grid.addStringWidget(2, 3, 6, 1, "item2", List.of(NeoComponent.formatString("&lTo &r&a%s", currentTransactionPage.get(1).getTo().getPlayerDisplayName()), NeoComponent.formatString("&lAmount &r&a%s", NeoString.formatCurrency(currentTransactionPage.get(1).getAmount()))), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.MIDDLE)
					.addButtonWidget(0, 3, 2, 1, "button2", Component.literal("Details"), null, false, (finalScreen, finalGrid) -> {
						showPendingDetailsPage(player, currentTransactionPage.get(1));
				});
			case 1:
				grid.addStringWidget(2, 2, 6, 1, "item1", List.of(NeoComponent.formatString("&lTo &r&a%s", currentTransactionPage.get(0).getTo().getPlayerDisplayName()), NeoComponent.formatString("&lAmount &r&a%s", NeoString.formatCurrency(currentTransactionPage.get(0).getAmount()))), NeoStringAlign.Horizontal.LEFT, NeoStringAlign.Vertical.MIDDLE)
					.addButtonWidget(0, 2, 2, 1, "button1", Component.literal("Details"), null, false, (finalScreen, finalGrid) -> {
						showPendingDetailsPage(player, currentTransactionPage.get(0));
				});
		}
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture, grid,
			(finalPlayer, finalGrid) -> {}).show(true);
	}

	public static void showPendingDetailsPage(@NotNull ServerPlayer player, @NotNull IRSTransaction pending) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Pending Transaction").withStyle(ChatFormatting.BOLD))
				.addStringWidget(0, 2, 4, 2, "to", List.of(Component.literal("To").withStyle(ChatFormatting.BOLD), Component.literal(pending.getTo().getPlayerDisplayName())))
				.addStringWidget(4, 2, 4, 2, "amount", List.of(Component.literal("Amount").withStyle(ChatFormatting.BOLD), Component.literal(NeoString.formatCurrency(pending.getAmount()))))
				.addStringWidget(0, 4, 8, 3, "reference", List.of(Component.literal("Reference").withStyle(ChatFormatting.BOLD), Component.literal(pending.getReference())), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP)
				.addButtonWidget(0, 9, 2, 1, "back", Component.literal("Back"), null, false, (finalScreen, finalGrid) -> {
					showPendingPage(player, null, 0);
				})
				.addButtonWidget(1, 7, 3, 1, "decline", Component.literal("Decline").withStyle(ChatFormatting.RED), null, false, (finalScreen, finalGrid) -> {
					IRSSimpleTransaction declineResponse = IRS.approveTransaction(player.getStringUUID(), pending.getTransactionID(), false);
					if (declineResponse.getState() != IRSPaymentState.DECLINED) {
						showErrorPage(player, Component.literal("There was an issue declining this transaction, it may have expired (They only last 2 mintutes)"));
						return;
					}
					showMessagePage(player, Component.literal("Request declined"));
				})
				.addButtonWidget(4, 7, 3, 1, "accept", Component.literal("Accept").withStyle(ChatFormatting.GREEN), null, false, (finalScreen, finalGrid) -> {
					IRSSimpleTransaction acceptResponse = IRS.approveTransaction(player.getStringUUID(), pending.getTransactionID(), true);
					if (acceptResponse.getState() != IRSPaymentState.ACCEPTED) {
						showErrorPage(player, Component.literal("There was an issue accepting this transaction, it may have expired (They only last 2 mintutes)"));
						return;
					}
					showMessagePage(player, Component.literal("Request accepted"));
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showProcessingPage(@NotNull ServerPlayer player) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 2, 8, 7, "message", Component.literal("Processing..."), NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

	public static void showMessagePage(@NotNull ServerPlayer player, Component message) {
		showMessagePage(player, List.of(message));
	}

	public static void showMessagePage(@NotNull ServerPlayer player, List<Component> message) {
		new NeoServerScreen(player, Component.literal(player.getScoreboardName() + "'s Bank Account"), texture,
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 2, 8, 7, "message", message, NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12)
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
			new NeoServerScreenGrid(20, 20, 2, 8, 10)
				.addStringWidget(0, 0, 8, 1, "title", Component.literal("Oops, there was an error!").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
				.addStringWidget(0, 2, 8, 7, "message", errorMessage, NeoStringAlign.Horizontal.CENTER, NeoStringAlign.Vertical.TOP, 12)
				.addButtonWidget(0, 9, 2, 1, "home", Component.literal("Home"), null, false, (finalScreen, finalGrid) -> {
					showHomePage(player);
				}),
			(finalPlayer, finalGrid) -> {}
		).show(true);
	}

}
