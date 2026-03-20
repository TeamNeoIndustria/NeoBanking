package xyz.neonetwork.neobanking.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.io.*;
import java.util.List;

public class IRSSimpleTransaction implements Serializable {
	private final String transactionID;
	private final IRSPaymentState state;

	public static final StreamCodec<ByteBuf, IRSSimpleTransaction> STREAM_CODEC =
		ByteBufCodecs.BYTE_ARRAY.map(IRSSimpleTransaction::fromByteArray, IRSSimpleTransaction::toByteArray);
	public static final StreamCodec<ByteBuf, List<IRSSimpleTransaction>> LIST_STREAM_CODEC =
		IRSSimpleTransaction.STREAM_CODEC.apply(ByteBufCodecs.list());

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

	public byte[] toByteArray() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.flush();
		} catch (IOException e) {
			return new byte[0];
		}
		return baos.toByteArray();
	}
	public static IRSSimpleTransaction fromByteArray(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			return (IRSSimpleTransaction) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
