package net.eternalempires.mod.common.client;

import net.eternalempires.mod.common.Constants;

public class EternalEmpiresClient {
    public static void init() {
        Constants.isTesting(true);  //pass `false` when building to disable logs

        Constants.LOGGER.fine("Client Init.");
    }
}