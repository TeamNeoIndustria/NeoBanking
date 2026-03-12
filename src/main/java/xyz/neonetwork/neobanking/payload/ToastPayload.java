package xyz.neonetwork.neobanking.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.neonetwork.neobanking.NeoBanking;

public record ToastPayload(String text, int exampleNumber) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ToastPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "toast"));

	public static final StreamCodec<ByteBuf, ToastPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ToastPayload::text,
		ByteBufCodecs.VAR_INT,
		ToastPayload::exampleNumber,
		ToastPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
