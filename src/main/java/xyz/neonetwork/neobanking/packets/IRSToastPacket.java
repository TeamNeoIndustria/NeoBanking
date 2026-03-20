package xyz.neonetwork.neobanking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSTransaction;

public record IRSToastPacket(String packetType, IRSTransaction transaction) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<IRSToastPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "toast"));

	public static final StreamCodec<ByteBuf, IRSToastPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		IRSToastPacket::packetType,
		IRSTransaction.STREAM_CODEC,
		IRSToastPacket::transaction,
		IRSToastPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
