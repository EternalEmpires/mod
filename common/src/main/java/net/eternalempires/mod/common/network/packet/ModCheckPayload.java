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
 *s
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
import net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This is effectively a custom-payload-packet that is sent from the minecraft server through the plugin-message-channel.
 * This packet that gets send to the client upon joining so client can validate present mods.
 * It's data will be (de)serialized as json and transferred byte-wise.
 *
 * @see AbstractEternalEmpiresPayload
 *
 * @since 09/07/2025
 * @author EternalEmpires
 */
@Slf4j
public class ModCheckPayload extends AbstractEternalEmpiresPayload {

    public static final @NotNull CustomPacketPayload.Type<ModCheckPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "modcheck"));

    public static final @NotNull StreamCodec<ByteBuf, ModCheckPayload> BYTEBUF_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    });

    public static final @NotNull StreamCodec<RegistryFriendlyByteBuf, ModCheckPayload> FORGE_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBytes(packet.data),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    }
            );

    public static final @NotNull StreamCodec<FriendlyByteBuf, ModCheckPayload> FABRIC_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    });

    /**
     * Constructs a new payload by reading all remaining bytes
     * from the provided {@link FriendlyByteBuf}.
     *
     * @param buffer the buffer containing the raw packet data
     */
    public ModCheckPayload(@NotNull FriendlyByteBuf buffer) {
        super(buffer);
    }

    /**
     * Constructs a new payload from a raw byte array.
     *
     * @param data the raw packet data
     */
    public ModCheckPayload(byte @NotNull [] data) {
        super(data);
    }

    /**
     * Returns the unique type identifier for this custom payload.
     * Used by Minecraft’s networking system to distinguish packet types.
     *
     * @return the {@link Type} of this payload
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Extracts a list of mod IDs from the JSON data contained in this payload.
     * <p>
     * Looks for a {@code "modIds"} array inside the {@code "data"} object.
     * If found, the array is parsed into a list of trimmed strings.
     * <p>
     * In case of malformed JSON or unexpected structure, an empty list is returned.
     *
     * @return a list of mod IDs provided by the server
     */
    private @NotNull List<String> extractModIds() {
        try {
            List<String> modIds = new ArrayList<>();

            // Extract the "data" object first, then get "modIds" from within it
            String dataJson = extractJsonField("data");
            if (dataJson != null) {
                // Parse the nested modIds array from the data object
                // The dataJson should contain something like: {"modIds":["mod1","mod2","mod3"]}
                int modIdsStart = dataJson.indexOf("\"modIds\":");
                if (modIdsStart != -1) {
                    int arrayStart = dataJson.indexOf("[", modIdsStart);
                    int arrayEnd = dataJson.indexOf("]", arrayStart);

                    if (arrayStart != -1 && arrayEnd != -1) {
                        String modIdsArray = dataJson.substring(arrayStart + 1, arrayEnd);
                        String[] mods = modIdsArray.replace("\"", "").split(",");
                        for (String mod : mods) {
                            String trimmed = mod.trim();
                            if (!trimmed.isEmpty()) {
                                modIds.add(trimmed);
                            }
                        }
                    }
                }
            }

            return modIds;
        } catch (Exception e) {
            log.error("[EternalEmpires] Failed to extract mod IDs from JSON", e);
            return new ArrayList<>();
        }
    }

    /**
     * Processes this payload on the client.
     * <p>
     * Specifically, it checks whether the payload type matches
     * {@code "prohibited_mod_ids"}. If so, it extracts the mod IDs and
     * delegates validation to {@link ModCheckHandler}.
     * <p>
     * Logs the JSON, handles empty or malformed cases, and ensures the
     * client is disconnected if disallowed mods are detected.
     */
    @Override
    public void handlePayload() {
        log.info("[EternalEmpires] Received JSON: {}", json);

        final String type = getTypeField();

        if (!"prohibited_mod_ids".equalsIgnoreCase(type)) {
            log.info("[EternalEmpires] Ignoring non-disallowed_mod_ids payload: type={}", type);
            return;
        }

        List<String> serverDisallowedMods = extractModIds();
        if (serverDisallowedMods.isEmpty()) {
            log.warn("[EternalEmpires] No mod IDs found in server packet");
            return;
        }

        log.info("[EternalEmpires] Server disallowed mods: {}", serverDisallowedMods);

        // Delegate to the loader-specific handler
        ModCheckHandler.performModCheck(serverDisallowedMods);
    }
}