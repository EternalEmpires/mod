package net.eternalempires.mod.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import org.jetbrains.annotations.NotNull;

public class UpdateDiscordRpcPayload extends AbstractEternalEmpiresPayload {

    public static final StreamCodec<ByteBuf, UpdateDiscordRpcPayload> BYTEBUF_CODEC =
            StreamCodec.of(
                    (buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new UpdateDiscordRpcPayload(data);
                    });

    public static final StreamCodec<FriendlyByteBuf, UpdateDiscordRpcPayload> FABRIC_CODEC =
            StreamCodec.of(
                    (buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new UpdateDiscordRpcPayload(data);
                    });

    public UpdateDiscordRpcPayload(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public UpdateDiscordRpcPayload(byte[] data) {
        super(data);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private String extractRegionName() {
        return extractJsonField("data.name");
    }

    @Override
    public void handlePayload() {
        Constants.LOGGER.fine("[EternalEmpires] Received JSON: " + json);

        String type = getTypeField();
        if (!"player_enter_region".equalsIgnoreCase(type)) {
            Constants.LOGGER.fine("[EternalEmpires] Ignoring non-region payload: type=" + type);
            return;
        }

        String regionName = extractRegionName();
        if (regionName != null) {
            Constants.LOGGER.fine("[EternalEmpires] Updating location: " + regionName);
            DiscordRPCManager.updateLocation(regionName);
        } else {
            Constants.LOGGER.warning("[EternalEmpires] Failed to extract region name from JSON");
        }
    }
}