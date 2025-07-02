package net.eternalempires.mod.common.client;

import net.arikia.dev.drpc.*;
import net.eternalempires.mod.common.Constants;

public class DiscordRPCManager {

    private static boolean started = false;
    private static Thread callbackThread;
    private static long startTimeStamp;

    public static void start() {
        if (started) return;
        started = true;

        startTimeStamp = System.currentTimeMillis();

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler((user) -> {
                    Constants.LOGGER.fine("Discord RPC ready for user: " + user.username);
                })
                .build();

        //DiscordRPC.discordInitialize("1379773116787724329", handlers, true);
        DiscordRPC.discordInitialize(Constants.DISCORD_APPLICATION_ID, handlers, true);

        DiscordRichPresence presence = new DiscordRichPresence.Builder("Playing on Eternal Empires")
                .setDetails("")
                //.setBigImage("icon", "Eternal Adventure")
                .setBigImage("eternalempires_e_1400x1400", "EternalEmpires.net")
                .setSmallImage("grasblock", "Minecraft " + Constants.VERSION)  //new line, for small image
                .setStartTimestamps(startTimeStamp)
                .build();

        DiscordRPC.discordUpdatePresence(presence);

        callbackThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                DiscordRPC.discordRunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "Discord-RPC-Callback-Thread");

        callbackThread.start();
    }

    public static void stop() {
        if (!started) return;

        started = false;
        if (callbackThread != null && callbackThread.isAlive()) {
            callbackThread.interrupt();
        }

        DiscordRPC.discordShutdown();
        Constants.LOGGER.fine("Discord RPC stopped.");
    }

    public static boolean isStarted() {
        return started;
    }

    public static void updateLocation(String location) {
        if (!started) return;

        DiscordRichPresence presence = new DiscordRichPresence.Builder(location)
                .setDetails("Playing on Eternal Empires")
                //.setBigImage("icon", "Wanderer")
                .setBigImage("eternalempires_e_1400x1400", "EternalEmpires.net")
                .setSmallImage("grasblock", "Minecraft " + Constants.VERSION)  //new line, for small image
                .setStartTimestamps(startTimeStamp)
                .build();

        DiscordRPC.discordUpdatePresence(presence);
    }
}