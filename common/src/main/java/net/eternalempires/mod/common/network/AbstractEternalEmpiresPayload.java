package net.eternalempires.mod.common.network;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AbstractEternalEmpiresPayload implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateDiscordRpcPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"));

    protected final byte[] data;
    protected final String json;

    public AbstractEternalEmpiresPayload(FriendlyByteBuf buffer) {
        this.data = new byte[buffer.readableBytes()];
        buffer.readBytes(this.data);
        this.json = new String(this.data, StandardCharsets.UTF_8);
    }

    public AbstractEternalEmpiresPayload(byte[] data) {
        this.data = data;
        this.json = new String(data, StandardCharsets.UTF_8);
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBytes(data);
    }

    public byte[] data() {
        return data;
    }

    public String json() {
        return json;
    }

    public String getTypeField() {
        return extractJsonField("type");
    }

    protected String extractJsonField(String fieldName) {
        if (this.json == null) return null;

        int jsonStart = this.json.indexOf('{');
        if (jsonStart == -1) return null;

        String rawJson = this.json.substring(jsonStart);

        JsonElement root;
        try {
            JsonReader reader = new JsonReader(new StringReader(rawJson));
            reader.setStrictness(Strictness.LENIENT);
            root = JsonParser.parseReader(reader);
        } catch (JsonSyntaxException e) {
            log.warn("Failed to parse JSON field '{}': {}", fieldName, e.getMessage());
            return null;
        }

        String[] parts = fieldName.split("\\.");
        JsonElement current = root;

        for (String part : parts) {
            if (!current.isJsonObject()) return null;
            current = current.getAsJsonObject().get(part);
            if (current == null) return null;
        }

        return current.isJsonPrimitive() ? current.getAsString() : current.toString();
    }

    public abstract void handlePayload();
}
