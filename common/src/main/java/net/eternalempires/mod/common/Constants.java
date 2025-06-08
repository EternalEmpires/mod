package net.eternalempires.mod.common;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Constants {
    public static final String MOD_ID = "eternalempires";
    public static final List<@NotNull String> SERVER_IPS = List.of("beta.eternalempires.dev");
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final org.slf4j.Logger newLogger = org.slf4j.LoggerFactory.getLogger(MOD_ID);

    public static void isTesting(boolean value) {
        if (value) {
            LOGGER.setLevel(Level.FINE);
        } else {
            LOGGER.setLevel(Level.OFF);
        }
    }
}