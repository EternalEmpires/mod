package net.eternalempires.mod.fabric.network;

import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.network.UpdateDiscordRpcPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

@Slf4j
public class PacketHandlersFabric {
    @SuppressWarnings("resource")
    public static void register() {
        PayloadTypeRegistry.playS2C().register(UpdateDiscordRpcPayload.TYPE, UpdateDiscordRpcPayload.FABRIC_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(UpdateDiscordRpcPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    log.info("Received payload: {}", payload.toString());
                    payload.handlePayload();

                } catch (Exception e) {
                    log.warn("Failed to handle payload: {}", e.getMessage());
                    log.warn("Failed to handle payload ", e);
                }
            });
        });
    }
}