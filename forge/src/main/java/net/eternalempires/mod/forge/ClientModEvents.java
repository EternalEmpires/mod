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

package net.eternalempires.mod.forge;

import com.google.inject.Injector;
import lombok.Getter;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.common.util.CommonService;
import net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler;
import net.eternalempires.mod.forge.client.ForgeModListProvider;
import net.eternalempires.mod.forge.network.PacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class checks if the client is set up.
 *
 * @author EternalEmpires
 * @version 07/22/2025
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @Getter
    @Nullable
    private static CommonService commonService;

    /**
     * This method gets triggered by the forge event executor.<br/>
     * It will handle the client initialization and register the packet handler.
     *
     * @param event container of event-related information
     */
    @SubscribeEvent
    public static void clientSetup(final @NotNull FMLClientSetupEvent event) {
        final Injector injector = EternalEmpiresClient.init();

        commonService = injector.getInstance(CommonService.class);
        
        final PacketHandler packetHandler = injector.getInstance(PacketHandler.class);

        event.enqueueWork(packetHandler::register);

        ModCheckHandler.setModListProvider(new ForgeModListProvider());
    }
}
