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

package net.eternalempires.mod.fabric.network;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.network.packet.ModCheckPayload;
import net.eternalempires.mod.common.network.packet.UpdateDiscordRpcPayload;
import net.eternalempires.mod.common.util.discord.RichPresenceService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * This class will handle packet-related stuff.
 *
 * @author EternalEmpires
 * @since 07/04/2025
 */
@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public final class PacketHandlersFabric {

    @NotNull
    private final RichPresenceService richPresenceService;

    public void register() {
        PayloadTypeRegistry.playS2C().register(UpdateDiscordRpcPayload.TYPE, UpdateDiscordRpcPayload.FABRIC_CODEC);
        PayloadTypeRegistry.playS2C().register(ModCheckPayload.TYPE, ModCheckPayload.FABRIC_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(UpdateDiscordRpcPayload.TYPE, this::handleRpcContext);
        ClientPlayNetworking.registerGlobalReceiver(ModCheckPayload.TYPE, this::handleModListContext);
    }

    /**
     * this context is registered in the ClientPlayNetworking. It registers a context-related executor that fetches
     * the custom-payload-packets from the plugin-channel.
     *
     * @param payload the payload
     * @param context the context
     */
    private void handleRpcContext(
            final @NotNull UpdateDiscordRpcPayload payload,
            final @NotNull ClientPlayNetworking.Context context
    ) {
        // TODO - Tjorven: should you ignore the closeable?
        context.client().execute(() -> {
            try {
                log.debug("Received payload: {}", payload);

                // TODO - Tjorven: Consider validating the payload structure and content before processing.
                payload.handlePayload(richPresenceService);
            } catch (Exception e) {
                log.error("Failed to handle payload [{}]", payload.getClass().getName(), e);
            }
        });
    }

    private void handleModListContext(
            final @NotNull ModCheckPayload payload,
            final @NotNull ClientPlayNetworking.Context context
    ) {
        context.client().execute(() -> {
            try {
                log.info("Received ModCheck payload: {}", payload);
                payload.handlePayload();
            } catch (Exception e) {
                log.warn("Failed to handle ModCheck payload ", e);
            }
        });
    }
}