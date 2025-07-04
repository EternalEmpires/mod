package net.eternalempires.mod.common.network;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class UpdateDiscordRpcPayload extends AbstractEternalEmpiresPayload {

    public static final StreamCodec<ByteBuf, UpdateDiscordRpcPayload> BYTEBUF_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];

                        buf.readBytes(data);

                        return new UpdateDiscordRpcPayload(data);
                    });

    public static final StreamCodec<FriendlyByteBuf, UpdateDiscordRpcPayload> FABRIC_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];

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
        log.info("[EternalEmpires] Received JSON: {}", json);

        final String type = getTypeField();

        if (!"player_enter_region".equalsIgnoreCase(type)) {
            log.info("[EternalEmpires] Ignoring non-region payload: type={}", type);
            return;
        }

        final String regionName = extractRegionName();

        if (regionName != null) {
            log.info("[EternalEmpires] Updating location: {}", regionName);

            DiscordRPCManager.updateLocation(regionName);
        } else {
            log.info("[EternalEmpires] Failed to extract region name from JSON");
        }
    }
}