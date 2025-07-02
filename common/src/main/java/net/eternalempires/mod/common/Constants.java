package net.eternalempires.mod.common;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Constants {
    public static final String MOD_ID = "eternalempires";
    public static final List<@NotNull String> SERVER_IPS = List.of("beta.eternalempires.dev");
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final String DISCORD_APPLICATION_ID = "1191464487191056404";

    public static void setTesting(boolean value) {
        if (value) {
            LOGGER.setLevel(Level.FINE);
        } else {
            LOGGER.setLevel(Level.OFF);
        }
    }
}