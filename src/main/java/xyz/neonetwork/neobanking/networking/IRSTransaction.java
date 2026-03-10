package xyz.neonetwork.neobanking.networking;

public class IRSTransaction {
	private final String transactionID;
	private final IRSPlayer from;
	private final IRSPlayer to;
	private final int amount;
	private final String reference;
	private final long timestamp;
	private final IRSPaymentState state;

	public IRSTransaction(String transactionID, IRSPlayer from, IRSPlayer to, int amount, String reference, long timestamp, IRSPaymentState state) {
		this.transactionID = transactionID;
		this.from = from;
		this.to = to;
		this.amount = amount;
		this.reference = reference;
		this.timestamp = timestamp;
		this.state = state;
	}

	public String getTransactionID() {
		return transactionID;
	}
	public IRSPlayer getFrom() {
		return from;
	}
	public IRSPlayer getTo() {
		return to;
	}
	public int getAmount() {
		return amount;
	}
	public String getReference() {
		return reference;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public IRSPaymentState getState() {
		return state;
	}
}
