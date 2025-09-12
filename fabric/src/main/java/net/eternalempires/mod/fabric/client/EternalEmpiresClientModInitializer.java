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

package net.eternalempires.mod.fabric.client;

import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler;
import net.eternalempires.mod.fabric.listeners.JoinListener;
import net.eternalempires.mod.fabric.listeners.LogoutListener;
import net.eternalempires.mod.fabric.network.PacketHandlersFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * The Fabric mod initializer.
 *
 * @author EternalEmpires
 * @since 07/01/2025
 */
@Slf4j
public final class EternalEmpiresClientModInitializer implements ClientModInitializer {

    /**
     * This is run when the mod gets initialized
     */
    @Override
    public void onInitializeClient() {
        final Injector injector = EternalEmpiresClient.init();

        injector.getInstance(PacketHandlersFabric.class).register();

        ClientPlayConnectionEvents.JOIN.register(injector.getInstance(JoinListener.class));
        ClientPlayConnectionEvents.DISCONNECT.register(injector.getInstance(LogoutListener.class));

        ModCheckHandler.setModListProvider(new FabricModListProvider());
    }
}