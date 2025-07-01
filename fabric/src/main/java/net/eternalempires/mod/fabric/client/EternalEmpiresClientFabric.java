package net.eternalempires.mod.fabric.client;

import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.fabric.network.PacketHandlersFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class EternalEmpiresClientFabric implements ClientModInitializer {
    private static String lastServerIP = null;

    @Override
    public void onInitializeClient() {
        EternalEmpiresClient.init();
        PacketHandlersFabric.register(); // move packet logic here

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData serverData = Minecraft.getInstance().getCurrentServer();

            if (serverData != null) {
                String ip = serverData.ip;
                Constants.LOGGER.fine("Joined server: " + ip);

                if (!ip.equals(lastServerIP)) {
                    if (Constants.SERVER_IPS.contains(ip)) {
                        Constants.LOGGER.fine("✅ IP matched! Starting Discord RPC.");
                        DiscordRPCManager.start();
                    }
                } else {
                    Constants.LOGGER.fine("🔁 Bungee switch detected. Keeping Discord RPC running.");
                }

                lastServerIP = ip;
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // If IP is known and not a Bungee switch
            if (lastServerIP != null && DiscordRPCManager.isStarted()) {
                Constants.LOGGER.fine("🛑 Disconnected from server: " + lastServerIP + ". Stopping Discord RPC.");
                DiscordRPCManager.stop();
                lastServerIP = null;
            }
        });
    }
}
