package net.eternalempires.mod.common.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import net.eternalempires.mod.common.Constants;
import net.eternalempires.mod.common.network.AbstractEternalEmpiresPayload;
import net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ModCheckPayload extends AbstractEternalEmpiresPayload {

    public static final CustomPacketPayload.Type<ModCheckPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "modcheck"));

    public static final StreamCodec<ByteBuf, ModCheckPayload> BYTEBUF_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    });

    public static final StreamCodec<RegistryFriendlyByteBuf, ModCheckPayload> FORGE_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBytes(packet.data),
                    buf -> {
                        byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    }
            );

    public static final StreamCodec<FriendlyByteBuf, ModCheckPayload> FABRIC_CODEC =
            StreamCodec.of((buf, value) -> buf.writeBytes(value.data),
                    buf -> {
                        final byte[] data = new byte[buf.readableBytes()];
                        buf.readBytes(data);
                        return new ModCheckPayload(data);
                    });

    public ModCheckPayload(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public ModCheckPayload(byte[] data) {
        super(data);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private List<String> extractModIds() {
        try {
            List<String> modIds = new ArrayList<>();

            // Extract the "data" object first, then get "modIds" from within it
            String dataJson = extractJsonField("data");
            if (dataJson != null) {
                // Parse the nested modIds array from the data object
                // The dataJson should contain something like: {"modIds":["mod1","mod2","mod3"]}
                int modIdsStart = dataJson.indexOf("\"modIds\":");
                if (modIdsStart != -1) {
                    int arrayStart = dataJson.indexOf("[", modIdsStart);
                    int arrayEnd = dataJson.indexOf("]", arrayStart);

                    if (arrayStart != -1 && arrayEnd != -1) {
                        String modIdsArray = dataJson.substring(arrayStart + 1, arrayEnd);
                        String[] mods = modIdsArray.replace("\"", "").split(",");
                        for (String mod : mods) {
                            String trimmed = mod.trim();
                            if (!trimmed.isEmpty()) {
                                modIds.add(trimmed);
                            }
                        }
                    }
                }
            }

            return modIds;
        } catch (Exception e) {
            log.error("[EternalEmpires] Failed to extract mod IDs from JSON", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void handlePayload() {
        log.info("[EternalEmpires] Received JSON: {}", json);

        final String type = getTypeField();

        if (!"prohibited_mod_ids".equalsIgnoreCase(type)) {
            log.info("[EternalEmpires] Ignoring non-disallowed_mod_ids payload: type={}", type);
            return;
        }

        List<String> serverDisallowedMods = extractModIds();
        if (serverDisallowedMods.isEmpty()) {
            log.warn("[EternalEmpires] No mod IDs found in server packet");
            return;
        }

        log.info("[EternalEmpires] Server disallowed mods: {}", serverDisallowedMods);

        // Delegate to the loader-specific handler
        ModCheckHandler.performModCheck(serverDisallowedMods);
    }
}