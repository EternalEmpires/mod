package net.eternalempires.mod.fabric.network;

import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.UpdateDiscordRpcPayload;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import java.nio.charset.StandardCharsets;

public class PacketHandlersFabric {
    public static void register() {
        // Register packet type
        PayloadTypeRegistry.playS2C().register(UpdateDiscordRpcPayload.TYPE, UpdateDiscordRpcPayload.FABRIC_CODEC);

        // Register receiver
        ClientPlayNetworking.registerGlobalReceiver(UpdateDiscordRpcPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                byte[] data = payload.data();

                try {
                    int length = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                    String jsonString = new String(data, 2, length, StandardCharsets.UTF_8);

                    Constants.LOGGER.fine("Received JSON: " + jsonString);
                    payload.handlePayload();

                } catch (Exception e) {
                    Constants.LOGGER.fine("Failed to parse packet data: " + e.getMessage());
                    e.printStackTrace();

                    // Fallback to raw string
                    String fallback = new String(data, StandardCharsets.UTF_8);
                    DiscordRPCManager.updateLocation(fallback);
                }
            });
        });
    }
}
