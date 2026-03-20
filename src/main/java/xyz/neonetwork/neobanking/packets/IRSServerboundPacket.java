package xyz.neonetwork.neobanking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.api.IRSSimpleTransaction;
import xyz.neonetwork.neobanking.api.IRSTransaction;

public record IRSServerboundPacket(String packetType, IRSTransaction irsTransaction, IRSSimpleTransaction irsSimpleTransaction) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<IRSServerboundPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "irs-server"));

	public static final StreamCodec<ByteBuf, IRSServerboundPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		IRSServerboundPacket::packetType,
		IRSTransaction.STREAM_CODEC,
		IRSServerboundPacket::irsTransaction,
		IRSSimpleTransaction.STREAM_CODEC,
		IRSServerboundPacket::irsSimpleTransaction,
		IRSServerboundPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
