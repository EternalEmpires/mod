package net.eternalempires.mod.common;

import net.minecraft.SharedConstants;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Constants {

    public static final @NotNull String MOD_ID = "eternalempires";
    public static final @NotNull String DISCORD_APPLICATION_ID = "1191464487191056404";

    public static final @NotNull List<@NotNull String> SERVER_IPS = List.of(
            "beta.eternalempires.dev"
    );

    public static final @NotNull String VERSION = SharedConstants.getCurrentVersion().getName();
}