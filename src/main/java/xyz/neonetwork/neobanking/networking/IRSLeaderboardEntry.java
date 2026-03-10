package xyz.neonetwork.neobanking.networking;

public class IRSLeaderboardEntry {
	private final IRSPlayer player;
	private final int balance;

	public IRSLeaderboardEntry(IRSPlayer player, int balance) {
		this.player = player;
		this.balance = balance;
	}

	public IRSPlayer getPlayer() {
		return player;
	}

	public int getBalance() {
		return balance;
	}
}
