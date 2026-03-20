package xyz.neonetwork.neobanking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSSimpleTransaction;
import xyz.neonetwork.neobanking.api.IRSTransaction;

import java.util.List;

public record IRSClientboundPacket(String packetType, int balance, List<IRSTransaction> irsTransactions, List<IRSSimpleTransaction> irsSimpleTransactions) implements CustomPacketPayload {

	public static final Type<IRSClientboundPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "irs-client"));

	public static final StreamCodec<ByteBuf, IRSClientboundPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		IRSClientboundPacket::packetType,
		ByteBufCodecs.INT,
		IRSClientboundPacket::balance,
		IRSTransaction.LIST_STREAM_CODEC,
		IRSClientboundPacket::irsTransactions,
		IRSSimpleTransaction.LIST_STREAM_CODEC,
		IRSClientboundPacket::irsSimpleTransactions,
		IRSClientboundPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
