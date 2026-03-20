package xyz.neonetwork.neobanking.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.io.*;
import java.util.List;

public class IRSTransaction implements Serializable {
	private final String transactionID;
	private final IRSPlayer from;
	private final IRSPlayer to;
	private final int amount;
	private final String reference;
	private final long timestamp;
	private final IRSPaymentState state;

	public static final StreamCodec<ByteBuf, IRSTransaction> STREAM_CODEC =
		ByteBufCodecs.BYTE_ARRAY.map(IRSTransaction::fromByteArray, IRSTransaction::toByteArray);
	public static final StreamCodec<ByteBuf, List<IRSTransaction>> LIST_STREAM_CODEC =
		IRSTransaction.STREAM_CODEC.apply(ByteBufCodecs.list());

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

	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
		return baos.toByteArray();
	}
	public static IRSTransaction fromByteArray(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			return (IRSTransaction) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
