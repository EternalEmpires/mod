package net.eternalempires.mod.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.ExampleModClient;

import java.nio.charset.StandardCharsets;

public class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExampleModClient.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData serverData = Minecraft.getInstance().getCurrentServer();

            if (serverData != null) {
                String ip = serverData.ip;
                Constants.LOGGER.fine("Joined server: " + ip);

                if (Constants.SERVER_IPS.contains(ip)) {
                    Constants.LOGGER.fine("✅ IP matched! Starting Discord RPC.");
                    DiscordRPCManager.start();
                }
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (DiscordRPCManager.isStarted()) {
                Constants.LOGGER.fine("🛑 Discord RPC was running. Stopping...");
                DiscordRPCManager.stop();
            }
        });

        // Register the payload type for server -> client communication
        PayloadTypeRegistry.playS2C().register(EternalEmpiresPayload.TYPE, EternalEmpiresPayload.CODEC);

        // Register the receiver
        ClientPlayNetworking.registerGlobalReceiver(EternalEmpiresPayload.TYPE, (payload, context) -> {
            // Execute on main client thread
            context.client().execute(() -> {
                byte[] data = payload.data();

                try {
                    // Parse the length-prefixed string format
                    // First 2 bytes are the length (big-endian short)
                    int length = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);

                    // Extract the JSON string
                    String jsonString = new String(data, 2, length, StandardCharsets.UTF_8);

                    Constants.LOGGER.fine("Received JSON: " + jsonString);

                    // Parse the JSON manually to extract the region name
                    String regionName = extractRegionName(jsonString);

                    if (regionName != null) {
                        Constants.LOGGER.fine("Updating Discord RPC with region: " + regionName);
                        DiscordRPCManager.updateLocation(regionName);
                    }

                } catch (Exception e) {
                    Constants.LOGGER.fine("Failed to parse packet data: " + e.getMessage());
                    e.printStackTrace();

                    // Fallback: try to use raw string
                    String fallback = new String(data, StandardCharsets.UTF_8);
                    DiscordRPCManager.updateLocation(fallback);
                }
            });
        });
    }

    // Define the custom payload record for raw plugin message data
    public record EternalEmpiresPayload(byte[] data) implements CustomPacketPayload {
        public static final Type<EternalEmpiresPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"));

        // Simple codec that just reads all remaining bytes
        public static final StreamCodec<FriendlyByteBuf, EternalEmpiresPayload> CODEC =
                StreamCodec.of(
                        (buf, value) -> buf.writeBytes(value.data),
                        (buf) -> {
                            byte[] data = new byte[buf.readableBytes()];
                            buf.readBytes(data);
                            return new EternalEmpiresPayload(data);
                        }
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // Helper method to extract region name from JSON string
    private static String extractRegionName(String json) {
        try {
            // Simple JSON parsing to extract the "name" field from the "data" object
            // Looking for: "data":{"id":"...","name":"Jungle of the lost"}

            int dataStart = json.indexOf("\"data\":");
            if (dataStart == -1) return null;

            int nameStart = json.indexOf("\"name\":", dataStart);
            if (nameStart == -1) return null;

            int valueStart = json.indexOf("\"", nameStart + 7); // After "name":
            if (valueStart == -1) return null;

            int valueEnd = json.indexOf("\"", valueStart + 1);
            if (valueEnd == -1) return null;

            return json.substring(valueStart + 1, valueEnd);

        } catch (Exception e) {
            Constants.LOGGER.fine("Failed to parse JSON: " + e.getMessage());
            return null;
        }
    }
}
