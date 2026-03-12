package xyz.neonetwork.neobanking.networking;

public enum IRSPaymentState {
	DECLINED(-1, "Declined"),
	PENDING(0, "Pending"),
	ACCEPTED(1, "Accepted"),
	UNKNOWN(2, "Unknown"),
	TIMED_OUT(3, "Timed Out"),
	INSUFFICIENT_FUNDS(4, "Insufficient Funds");

	private final int stateID;
	private final String description;

	IRSPaymentState(int stateID, String description) {
		this.stateID = stateID;
		this.description = description;
	}

	public int getStateID() {
		return stateID;
	}

	public String getDescription() {
		return description;
	}

	private static final IRSPaymentState[] VALUES = values();
	public static IRSPaymentState fromStateID(int stateID) {
		for (IRSPaymentState state : VALUES) {
			if (state.getStateID() == stateID) return state;
		}
		throw new IllegalArgumentException("Unknown state ID: " + stateID);
	}
}