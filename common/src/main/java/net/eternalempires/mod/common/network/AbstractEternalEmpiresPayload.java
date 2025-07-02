package net.eternalempires.mod.common.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.eternalempires.mod.common.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

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
        try {
            JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
            return jsonObj.has(fieldName) ? jsonObj.get(fieldName).getAsString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public abstract void handlePayload();
}
