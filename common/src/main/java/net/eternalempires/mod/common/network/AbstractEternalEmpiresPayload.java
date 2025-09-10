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

package net.eternalempires.mod.common.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.packet.UpdateDiscordRpcPayload;
import net.eternalempires.mod.common.util.discord.RichPresenceService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * This is effectively a custom-payload-packet that is sent from the minecraft server through the plugin-message-channel.
 * It's data will be (de)serialized as json and transferred byte-wise.
 *
 * @since 07/02/2025
 * @author EternalEmpires
 */
@Slf4j
public abstract class AbstractEternalEmpiresPayload implements CustomPacketPayload {
    protected final byte[] data;

    @Nullable
    protected final String json;

    public AbstractEternalEmpiresPayload(final @NotNull FriendlyByteBuf buffer) {
        this.data = new byte[buffer.readableBytes()];
        this.json = new String(this.data, StandardCharsets.UTF_8);

        buffer.readBytes(this.data);
    }

    public AbstractEternalEmpiresPayload(byte[] data) {
        this.data = data;
        this.json = new String(data, StandardCharsets.UTF_8);
    }

    /**
     * This will encode the data into a ByteBuf.
     *
     * @param buffer the ByteBuf to write to.
     */
    public void encode(final @NotNull FriendlyByteBuf buffer) {
        buffer.writeBytes(data);
    }

    /**
     * This returns the serialized data.
     *
     * @return the data
     */
    public byte[] data() {
        return data;
    }

    /**
     * This will return the json that is effectively the data as string
     *
     * @return the data as jsonString
     */
    @Nullable
    public String json() {
        return json;
    }

    /**
     * This will return the type of the packet
     *
     * @return the packet type as String
     */
    @Nullable
    public String getTypeField() {
        return extractJsonField("type");
    }

    /**
     * This method is used to extract a field value by a key directly out of the data of the custom-payload-packet
     *
     * @param fieldName the key
     * @return the value as String
     */
    @Nullable
    protected String extractJsonField(final @NotNull String fieldName) {
        if (this.json == null) {
            return null;
        }

        final int jsonStart = this.json.indexOf('{');

        if (jsonStart == -1) {
            return null;
        }

        final String rawJson = this.json.substring(jsonStart);

        final JsonElement root;

        try {
            final JsonReader reader = new JsonReader(new StringReader(rawJson));

            reader.setStrictness(Strictness.LENIENT);

            root = JsonParser.parseReader(reader);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse JSON field '{}': {}", fieldName, e.getMessage());

            return null;
        }

        final String[] parts = fieldName.split("\\.");

        JsonElement current = root;

        for (String part : parts) {
            if (!current.isJsonObject()) {
                return null;
            }

            current = current.getAsJsonObject().get(part);

            if (current == null) {
                return null;
            }
        }

        return current.isJsonPrimitive() ? current.getAsString() : current.toString();
    }

    /**
     * This will handle the payload of the packet.
     *
     * @apiNote Will likely be moved in the future to a more robust handling.
     * @param service this is an instance of the RichPresenceService.
     */
    public void handlePayload(@NotNull RichPresenceService service) {};

    //Parameter-less handlePayload() for ModCheckPayload
    public void handlePayload() {};
}
