package net.eternalempires.mod.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.eternalempires.mod.common.Constants;

public class PacketHandler {
    private static final SimpleChannel INSTANCE = ChannelBuilder.named(
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void register() {
        INSTANCE.messageBuilder(EternalEmpiresPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(EternalEmpiresPacket::encode)
                .decoder(EternalEmpiresPacket::new)
                .consumerMainThread(EternalEmpiresPacket::handle)
                .add();
    }
}