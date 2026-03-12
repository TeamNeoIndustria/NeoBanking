package xyz.neonetwork.neobanking.networking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import xyz.neonetwork.neobanking.Utilities;

import java.util.Objects;
import java.util.UUID;

public class IRSPlayer {
	private static final String serverUUID = "@Server";
	private final String playerUUID;
	private final String playerName;

	public IRSPlayer(String playerUUID) {
		this.playerUUID = (playerUUID != null) ? playerUUID.replace("-", "") : null;
		this.playerName = null;
	}
	public IRSPlayer(String playerUUID, String playerName) {
		this.playerUUID = (playerUUID != null) ? playerUUID.replace("-", "") : null;
		this.playerName = playerName;
	}

	public String getPlayerUUID() {
		return this.playerUUID;
	}

	public boolean isServer() {
		if (this.playerUUID == null) return false;
		return this.playerUUID.equals(serverUUID);
	}

	public Player getPlayer() {
		if (this.isServer() || this.playerUUID == null) return null;
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || !server.isDedicatedServer()) return null;
		return server.getPlayerList().getPlayer(Objects.requireNonNull(Utilities.UUIDFromString(this.playerUUID)));
	}

	public String getPlayerDisplayName() {
		if (this.isServer()) return serverUUID;
		if (this.playerName != null) return this.playerName;
		return this.getPlayer().getDisplayName().getString();
	}
}
