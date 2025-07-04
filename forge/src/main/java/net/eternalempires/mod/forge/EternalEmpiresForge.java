package net.eternalempires.mod.forge;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.EternalEmpires;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.forge.network.PacketHandler;

@Slf4j
@Mod(Constants.MOD_ID)
public class EternalEmpiresForge {
    public EternalEmpiresForge() {
        EternalEmpires.init();
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            EternalEmpiresClient.init();
            event.enqueueWork(PacketHandler::register);
        }
    }

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerConnectionHandler {
        private static String lastServerIP = null;

        @SubscribeEvent
        public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggingIn event) {
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
        }

        @SubscribeEvent
        public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            // If IP is known and not a Bungee switch
            if (lastServerIP != null && DiscordRPCManager.isStarted()) {
                log.info("Disconnected from server: {}. Stopping Discord RPC.", lastServerIP);
                DiscordRPCManager.stop();
                lastServerIP = null;
            }
        }
    }
}