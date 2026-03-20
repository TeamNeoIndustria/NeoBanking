package xyz.neonetwork.neobanking.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.io.*;
import java.util.List;

public class IRSLeaderboardEntry implements Serializable {
	private final IRSPlayer player;
	private final int balance;

	public static final StreamCodec<ByteBuf, IRSLeaderboardEntry> STREAM_CODEC =
		ByteBufCodecs.BYTE_ARRAY.map(IRSLeaderboardEntry::fromByteArray, IRSLeaderboardEntry::toByteArray);
	public static final StreamCodec<ByteBuf, List<IRSLeaderboardEntry>> LIST_STREAM_CODEC =
		IRSLeaderboardEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

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
	public static IRSLeaderboardEntry fromByteArray(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			return (IRSLeaderboardEntry) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
