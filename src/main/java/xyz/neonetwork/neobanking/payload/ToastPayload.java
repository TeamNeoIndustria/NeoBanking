package xyz.neonetwork.neobanking.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import xyz.neonetwork.neobanking.NeoBanking;

public record ToastPayload(String title, String message) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ToastPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoBanking.MODID, "toast"));

	public static final StreamCodec<ByteBuf, ToastPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ToastPayload::title,
		ByteBufCodecs.STRING_UTF8,
		ToastPayload::message,
		ToastPayload::new
	);
//	public static final StreamCodec<ByteBuf, ToastPayload> STREAM_CODEC = StreamCodec.composite(
//		ByteBufCodecs.STRING_UTF8,
//		ToastPayload::myStringVar,
//		ByteBufCodecs.VAR_INT,
//		ToastPayload::myIntVar,
//		ToastPayload::new
//	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
