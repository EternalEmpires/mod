package net.eternalempires.mod.fabric.network;

import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.UpdateDiscordRpcPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PacketHandlersFabric {
    @SuppressWarnings("resource")
    public static void register() {
        // Register packet type
        PayloadTypeRegistry.playS2C().register(UpdateDiscordRpcPayload.TYPE, UpdateDiscordRpcPayload.FABRIC_CODEC);

        // Register receiver
        ClientPlayNetworking.registerGlobalReceiver(UpdateDiscordRpcPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    Constants.LOGGER.fine("Received payload: " + payload.toString());
                    payload.handlePayload();

                } catch (Exception e) {
                    Constants.LOGGER.severe("Failed to handle payload: " + e.getMessage());
                    Constants.LOGGER.severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
                }
            });
        });
    }
}