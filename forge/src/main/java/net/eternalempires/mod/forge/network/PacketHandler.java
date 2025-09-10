/*
 * MIT License
 *
 * Copyright (c) 2025 EternalEmpires.net
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.eternalempires.mod.forge.network;

import com.google.inject.Inject;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.packet.ModCheckPayload;
import net.eternalempires.mod.common.network.packet.UpdateDiscordRpcPayload;
import net.eternalempires.mod.common.util.discord.RichPresenceService;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import org.jetbrains.annotations.NotNull;

/**
 * This is the PacketHandler of the forge client.
 * It will process custom-payload-packets from the plugin-message-channel
 *
 * @author EternalEmpires
 * @since 07/23/2025
 */
public final class PacketHandler {

    @Inject
    public PacketHandler(@NotNull RichPresenceService richPresenceService) {
        @NotNull SimpleChannel updateRichPresence = ChannelBuilder.named(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "mod"))
                .serverAcceptedVersions((status, i) -> true)
                .clientAcceptedVersions((status, i) -> true)
                .networkProtocolVersion(1)
                .simpleChannel()
                .play()
                .clientbound()
                .add(UpdateDiscordRpcPayload.class, UpdateDiscordRpcPayload.FORGE_CODEC,
                        (packet, context) -> {
                            packet.handlePayload(richPresenceService);
                        })
                .build();

        @NotNull SimpleChannel checkProhibitedMods = ChannelBuilder.named(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "modcheck"))
                .serverAcceptedVersions((status, i) -> true)
                .clientAcceptedVersions((status, i) -> true)
                .networkProtocolVersion(1)
                .simpleChannel()
                .play()
                .clientbound()
                .add(ModCheckPayload.class, ModCheckPayload.FORGE_CODEC,
                        (packet, context) -> {
                            packet.handlePayload();
                        })
                .build();
    }

    /**
     * This method is called in the Forge main-class to register UPDATE_RPC above
     */
    public void register() {
    }
}