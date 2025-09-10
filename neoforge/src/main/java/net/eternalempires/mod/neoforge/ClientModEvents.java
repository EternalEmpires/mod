package net.eternalempires.mod.neoforge;

import com.google.inject.Injector;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.client.EternalEmpiresClient;
import net.eternalempires.mod.common.network.packet.ModCheckPayload;
import net.eternalempires.mod.common.network.packet.UpdateDiscordRpcPayload;
import net.eternalempires.mod.common.util.CommonService;
import net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler;
import net.eternalempires.mod.neoforge.client.NeoForgeModListProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class checks if the client is set up and will listen to the plugin message channel for custom-payload-packets.
 *
 * @author EternalEmpires
 * @version 07/22/2025
 */
@Slf4j
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    @Getter
    @Nullable
    private static CommonService commonService;

    /**
     * This is called when the client was set up.
     * This will create an injector instance via the {@link EternalEmpiresClient}.
     *
     * @param event container of event-related information
     */
    @SubscribeEvent
    public static void clientSetup(final @NotNull FMLClientSetupEvent event) {
        final Injector injector = EternalEmpiresClient.init();

        commonService = injector.getInstance(CommonService.class);
        ModCheckHandler.setModListProvider(new NeoForgeModListProvider());
    }

    /**
     * This method gets triggered by the neo-forge event executor.<br/>
     * This will register the plugin-channel and register a scheduler to the context for processing custom-payload-packets.
     *
     * @param event container of event-related information
     */
    @SubscribeEvent
    public static void register(final @NotNull RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constants.MOD_ID)
                .versioned("1")
                .optional();

        registrar.playToClient(
                UpdateDiscordRpcPayload.TYPE,
                UpdateDiscordRpcPayload.BYTEBUF_CODEC,
                (updateDiscordRpcPayload, context) -> {
                    context.enqueueWork(() -> {
                        log.debug("Received JSON: {}", updateDiscordRpcPayload.json());

                        assert commonService != null;
                        updateDiscordRpcPayload.handlePayload(Objects.requireNonNull(
                                commonService.getRichPresenceService(),
                                "ClientModEvents is not initialized!"
                        ));
                    });
                }
        );

        registrar.playToClient(
                ModCheckPayload.TYPE,
                ModCheckPayload.BYTEBUF_CODEC,
                (modCheckPayload, context) -> context.enqueueWork(() -> {
                    log.info("[EternalEmpires] Received ModCheck JSON: {}", modCheckPayload.json());
                    modCheckPayload.handlePayload();
                })
        );
    }
}
