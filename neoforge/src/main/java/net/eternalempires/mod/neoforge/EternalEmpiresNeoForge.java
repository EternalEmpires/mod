package net.eternalempires.mod.neoforge;

import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.network.UpdateDiscordRpcPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.EternalEmpires;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.EternalEmpiresClient;

@Slf4j
@Mod(Constants.MOD_ID)
public class EternalEmpiresNeoForge {
    public EternalEmpiresNeoForge() {
        EternalEmpires.init();
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(final FMLClientSetupEvent event) {
            EternalEmpiresClient.init();
        }

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                    .versioned("1")
                    .optional();
            registrar.playToClient(
                    UpdateDiscordRpcPayload.TYPE,
                    UpdateDiscordRpcPayload.BYTEBUF_CODEC,
                    (updateDiscordRpcPayload, context) -> context.enqueueWork( () -> {
                        log.info("[EternalEmpires] Received JSON: {}", updateDiscordRpcPayload.json());

                        updateDiscordRpcPayload.handlePayload();
                    })
            );
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
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