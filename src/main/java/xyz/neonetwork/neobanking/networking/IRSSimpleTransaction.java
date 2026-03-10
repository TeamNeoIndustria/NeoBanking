package xyz.neonetwork.neobanking.networking;

public class IRSSimpleTransaction {
	private final String transactionID;
	private final IRSPaymentState state;

	public IRSSimpleTransaction(String transactionID, IRSPaymentState state) {
		this.transactionID = transactionID;
		this.state = state;
	}

	public String getTransactionID() {
		return transactionID;
	}
	public IRSPaymentState getState() {
		return state;
	}
}
