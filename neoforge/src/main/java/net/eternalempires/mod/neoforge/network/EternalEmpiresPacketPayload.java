package net.eternalempires.mod.neoforge.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.eternalempires.mod.common.Constants;

public record EternalEmpiresPacketPayload(byte[] data, String json) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<EternalEmpiresPacketPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"));

    public static final StreamCodec<ByteBuf, EternalEmpiresPacketPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY,
            EternalEmpiresPacketPayload::data,
            ByteBufCodecs.STRING_UTF8,
            EternalEmpiresPacketPayload::json,
            EternalEmpiresPacketPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
