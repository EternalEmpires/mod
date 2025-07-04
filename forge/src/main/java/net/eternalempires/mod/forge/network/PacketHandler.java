package net.eternalempires.mod.forge.network;

import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.UpdateDiscordRpcPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {

    private static final SimpleChannel INSTANCE = ChannelBuilder.named(
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"))
            .serverAcceptedVersions((_, _) -> true)
            .clientAcceptedVersions((_, _) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    //todo: SimpleChannel#messageBuilder is deprecated
    public static void register() {
        INSTANCE.messageBuilder(UpdateDiscordRpcPayload.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(UpdateDiscordRpcPayload::encode)
                .decoder(UpdateDiscordRpcPayload::new)
                .consumerMainThread((packet, _) -> {
                    packet.handlePayload(); // No need for Forge context!
                })
                .add();
    }
}