package xyz.neonetwork.neobanking.api;

import com.mojang.authlib.yggdrasil.ProfileResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neolib.utilities.NeoString;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IRSPlayer implements Serializable {
	private static final String serverUUID = "@Server";
	private final UUID playerUUID;
	private String playerName;
	private boolean isServerUUID = false;

	public static final StreamCodec<ByteBuf, IRSPlayer> STREAM_CODEC =
		ByteBufCodecs.BYTE_ARRAY.map(IRSPlayer::fromByteArray, IRSPlayer::toByteArray);
	public static final StreamCodec<ByteBuf, List<IRSPlayer>> LIST_STREAM_CODEC =
		IRSPlayer.STREAM_CODEC.apply(ByteBufCodecs.list());

	public IRSPlayer(String playerUUID) {
		if (Objects.equals(playerUUID, serverUUID)) this.isServerUUID = true;
		this.playerUUID = (playerUUID != null) ? NeoString.UUIDFromString(playerUUID) : null;

		if (!isServerUUID) {
			ProfileResult irsPlayerProfile = NeoBanking.server.getSessionService().fetchProfile(this.playerUUID, false);
			if (irsPlayerProfile != null) {
				this.playerName = irsPlayerProfile.profile().getName();
			}
		} else {
			this.playerName = null;
		}
	}
	public IRSPlayer(String playerUUID, String playerName) {
		if (Objects.equals(playerUUID, serverUUID)) this.isServerUUID = true;
		this.playerUUID = (playerUUID != null) ? NeoString.UUIDFromString(playerUUID) : null;
		if (!isServerUUID && playerName == null) {
			ProfileResult irsPlayerProfile = NeoBanking.server.getSessionService().fetchProfile(this.playerUUID, false);
			if (irsPlayerProfile != null) {
				this.playerName = irsPlayerProfile.profile().getName();
			}
		} else {
			this.playerName = playerName;
		}
	}

	public UUID getPlayerUUID() {
		return this.playerUUID;
	}

	public boolean isServer() {
		return this.isServerUUID;
	}

	public Player getPlayer() {
		if (this.isServer() || this.playerUUID == null) return null;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || !server.isDedicatedServer()) return null;
		return server.getPlayerList().getPlayer(Objects.requireNonNull(this.playerUUID));
	}

	public String getPlayerDisplayName() {
		if (this.isServerUUID) return serverUUID;
		if (this.playerName != null) return this.playerName;
		Player player = this.getPlayer();
		if (player != null) return player.getDisplayName().getString();
		return "!UNKNOWN!";
	}

	public void setPlayerDisplayName(String displayName) {
		this.playerName = displayName;
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
	public static IRSPlayer fromByteArray(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(bais);
			return (IRSPlayer) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
