package net.eternalempires.mod.neoforge;

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
import net.eternalempires.mod.neoforge.network.EternalEmpiresPacketPayload;

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
                    EternalEmpiresPacketPayload.TYPE,
                    EternalEmpiresPacketPayload.STREAM_CODEC,
                    (eternalEmpiresPacketPayload, iPayloadContext) -> iPayloadContext.enqueueWork( () -> {
                        Constants.LOGGER.fine("[EternalEmpires] Received JSON: " + eternalEmpiresPacketPayload.json());

                        String regionName = extractRegionName(eternalEmpiresPacketPayload.json());
                        if (regionName != null) {
                            Constants.LOGGER.fine("[EternalEmpires] Updating location: " + regionName);
                            DiscordRPCManager.updateLocation(regionName);
                        }
                    })
            );
        }
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

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class ServerConnectionHandler {
        @SubscribeEvent
        public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
            Minecraft mc = Minecraft.getInstance();
            ServerData serverData = mc.getCurrentServer();

            if (serverData != null) {
                String ip = serverData.ip;
                Constants.LOGGER.fine("Joined server: " + ip);

                if (Constants.SERVER_IPS.contains(ip)) {
                    Constants.LOGGER.fine("✅ Matched IP. Starting Discord RPC.");
                    DiscordRPCManager.start();
                }
            }
        }

        @SubscribeEvent
        public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
            if (DiscordRPCManager.isStarted()) {
                Constants.LOGGER.fine("🛑 Leaving server. Stopping Discord RPC.");
                DiscordRPCManager.stop();
            }
        }
    }
}
