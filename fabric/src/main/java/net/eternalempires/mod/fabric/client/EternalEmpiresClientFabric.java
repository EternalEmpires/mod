package net.eternalempires.mod.fabric.client;

import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.fabric.network.PacketHandlersFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Slf4j
public class EternalEmpiresClientFabric implements ClientModInitializer {
    private static String lastServerIP = null;

    @Override
    public void onInitializeClient() {
        EternalEmpiresClient.init();
        PacketHandlersFabric.register();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ServerData serverData = Minecraft.getInstance().getCurrentServer();

            if (serverData != null) {
                String ip = serverData.ip;
                log.info("Joined server: {}", ip);

                if (!ip.equals(lastServerIP)) {
                    if (Constants.SERVER_IPS.contains(ip)) {
                        log.info("IP matched! Starting Discord RPC.");
                        DiscordRPCManager.start();
                    }
                } else {
                    log.info("Bungee switch detected. Keeping Discord RPC running.");
                }

                lastServerIP = ip;
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (lastServerIP != null && DiscordRPCManager.isStarted()) {
                log.info("Disconnected from server: {}. Stopping Discord RPC.", lastServerIP);
                DiscordRPCManager.stop();
                lastServerIP = null;
            }
        });
    }
}