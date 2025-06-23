package net.eternalempires.mod.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.EternalEmpires;
import net.eternalempires.mod.common.client.DiscordRPCManager;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.forge.network.PacketHandler;

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

    @Mod.EventBusSubscriber(modid = "eternalempires", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerConnectionHandler {

        private static boolean hasCheckedServer = false;

        // Use TickEvent to check when connected to server (most reliable method)
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && !hasCheckedServer) {
                Minecraft mc = Minecraft.getInstance();

                // Check if we're in a multiplayer world and have server data
                if (mc.level != null && mc.getCurrentServer() != null) {
                    checkServerAndStartRPC();
                    hasCheckedServer = true;
                }
            }
        }

        // Detect when player disconnects to stop RPC and reset check
        @SubscribeEvent
        public static void onClientTick_Disconnect(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Minecraft mc = Minecraft.getInstance();

                // If we were checking a server but now we're not connected anymore
                if (hasCheckedServer && (mc.level == null || mc.getCurrentServer() == null)) {
                    hasCheckedServer = false;

                    // Stop Discord RPC if it was started
                    if (DiscordRPCManager.isStarted()) {
                        DiscordRPCManager.stop();
                    }
                }
            }
        }

        private static void checkServerAndStartRPC() {
            Minecraft mc = Minecraft.getInstance();
            ServerData serverData = mc.getCurrentServer();

            if (serverData != null) {
                String serverIP = serverData.ip;

                // Check if the server IP is in our target list
                if (Constants.SERVER_IPS.contains(serverIP)) {
                    Constants.LOGGER.fine("Connected to target server: " + serverIP);
                    DiscordRPCManager.start();
                }
            }
        }
    }
}
