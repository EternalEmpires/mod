/*
 * MIT License
 *
 * Copyright (c) 2025 EternalEmpires.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.eternalempires.mod.common.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.AbstractEternalEmpiresPayload;
import net.eternalempires.mod.common.util.discord.RichPresenceService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This is effectively a custom-payload-packet that is sent from the minecraft server through the plugin-message-channel.
 * This packet that gets send to the client if the Discord Rich-presence is required to update it state.
 * It's data will be (de)serialized as json and transferred byte-wise.
 *
 * @see AbstractEternalEmpiresPayload
 *
 * @since 07/02/2025
 * @author EternalEmpires
 */
@Slf4j
public final class UpdateDiscordRpcPayload extends AbstractEternalEmpiresPayload {

    @NotNull
    public static final CustomPacketPayload.Type<@NotNull UpdateDiscordRpcPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"));

    @NotNull
    public static final StreamCodec<@NotNull ByteBuf, @NotNull UpdateDiscordRpcPayload> BYTEBUF_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];

                        buf.readBytes(data);

                        return new UpdateDiscordRpcPayload(data);
                    });

    @NotNull
    public static final StreamCodec<@NotNull RegistryFriendlyByteBuf, @NotNull UpdateDiscordRpcPayload> FORGE_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBytes(packet.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];

                        buf.readBytes(data);

                        return new UpdateDiscordRpcPayload(data);
                    }
            );

    @NotNull
    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull UpdateDiscordRpcPayload> FABRIC_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];

                        buf.readBytes(data);

                        return new UpdateDiscordRpcPayload(data);
                    });

    public UpdateDiscordRpcPayload(final @NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    public UpdateDiscordRpcPayload(final byte[] data) {
        super(data);
    }

    /**
     * This will return the type of the custom-payload-packet.
     *
     * @return the type
     */
    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * This will extract the region name out of the data of the custom-payload-packet
     *
     * @return the region name as String
     */
    @Nullable
    private String extractRegionName() {
        return extractJsonField("data.name");
    }

    /**
     * This will handle the payload.
     *
     * @apiNote Will likely be moved in the future to a more robust handling.
     */
    @Override
    public void handlePayload(final @NotNull RichPresenceService service) {
        log.debug("Received JSON: {}", json);

        final String type = getTypeField();

        if (!"player_enter_region".equalsIgnoreCase(type)) {
            log.debug("Ignoring non-region payload: type={}", type);
            return;
        }

        final String regionName = extractRegionName();

        if (regionName != null) {
            log.debug("Updating location: {}", regionName);

            service.updateLocation(regionName);
        } else {
            log.debug("Failed to extract region name from JSON");
        }
    }
}