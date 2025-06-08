package net.eternalempires.mod.forge.network;

import net.eternalempires.mod.common.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.eternalempires.mod.common.client.DiscordRPCManager;

import java.nio.charset.StandardCharsets;

public class EternalEmpiresPacket {
    private final byte[] data;
    private final String json;


    public EternalEmpiresPacket(FriendlyByteBuf buffer) {
        this.data = new byte[buffer.readableBytes()];
        buffer.readBytes(this.data);
        this.json = new String(this.data, StandardCharsets.UTF_8);
    }

    public void encode(FriendlyByteBuf buffer) {}

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            Constants.LOGGER.fine("[EternalEmpires] Received JSON: " + this.json);

            String regionName = extractRegionName(this.json);
            if (regionName != null) {
                Constants.LOGGER.fine("[EternalEmpires] Updating location: " + regionName);
                DiscordRPCManager.updateLocation(regionName);
            }
        });
        context.setPacketHandled(true);
    }

    private static String extractRegionName(String json) {
        try {
            int dataStart = json.indexOf("\"data\":");
            if (dataStart == -1) return null;

            int nameStart = json.indexOf("\"name\":", dataStart);
            if (nameStart == -1) return null;

            int valueStart = json.indexOf("\"", nameStart + 7);
            int valueEnd = json.indexOf("\"", valueStart + 1);
            return json.substring(valueStart + 1, valueEnd);

        } catch (Exception e) {
            return null;
        }
    }
}
