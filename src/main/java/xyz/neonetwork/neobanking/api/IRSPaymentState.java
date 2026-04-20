package xyz.neonetwork.neobanking.api;

import java.io.Serializable;

public enum IRSPaymentState implements Serializable {
	DECLINED(-1, "Declined"),
	PENDING(0, "Pending"),
	ACCEPTED(1, "Accepted"),
	UNKNOWN(2, "Unknown"),
	TIMED_OUT(3, "Timed Out"),
	INSUFFICIENT_FUNDS(4, "Insufficient Funds"),
	INVALID_REFERENCE(5, "Reference Invalid"),
	TO_PLAYER_INVALID(6, "To player not found"),
	FROM_PLAYER_INVALID(7, "From player not found"),
	TARGET_PLAYER_INVALID(8, "Target player not found"),
	TO_FROM_PLAYER_SAME(9, "To and From player cannot be the same"),
	CANNOT_SEND_SERVER(10, "Cannot send money to the server"),
	CANNOT_REQUEST_SERVER(11, "Cannot request money from the server"),
	INVALID_AMOUNT(12, "Number specified is not a valid amount");

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