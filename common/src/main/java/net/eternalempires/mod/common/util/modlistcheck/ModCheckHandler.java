package net.eternalempires.mod.common.util.modlistcheck;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class ModCheckHandler {

    /**
     * -- SETTER --
     *  This should be called by each mod loader's initialization to set their specific provider
     */
    @Setter
    private static IModListProvider modListProvider;

    public static void performModCheck(List<String> serverDisallowedMods) {
        if (modListProvider == null) {
            log.warn("[EternalEmpires] No mod list provider set! Cannot perform mod check.");
            return;
        }

        Set<String> installedMods = modListProvider.getInstalledModIds();
        log.info("[EternalEmpires] Client installed mods: {}", installedMods);

        List<String> foundDisallowedMods = serverDisallowedMods.stream()
                .filter(installedMods::contains)
                .collect(Collectors.toList());

        if (!foundDisallowedMods.isEmpty()) {
            log.warn("[EternalEmpires] Found disallowed mods on client: {}", foundDisallowedMods);
            disconnectWithModError(foundDisallowedMods);
        } else {
            log.info("[EternalEmpires] No disallowed mods found, connection allowed");
        }
    }

    private static void disconnectWithModError(List<String> foundModIds) {
        log.info("[EternalEmpires] Disconnecting player due to disallowed mods: {}", foundModIds);

        Map<String, String> modIdToNameMap = modListProvider.getModIdToNameMap();

        Component title = Component.literal("Eternal Empires MMORPG")
                .setStyle(Style.EMPTY.withColor(0xFF5555).withBold(true));

        Component failed = Component.translatable("mod.join.prohibited.failed")
                .setStyle(Style.EMPTY.withColor(0xFF5555));

        Component explanation = Component.translatable(
                "mod.join.prohibited.explanation"
        ).setStyle(Style.EMPTY.withColor(0xAAAAAA));

        String modsJoined = foundModIds.stream()
                .map(id -> modIdToNameMap.getOrDefault(id, id))
                .collect(Collectors.joining(", "));

        Component modsHeader = Component.translatable("mod.join.prohibited.modsHeader")
                .setStyle(Style.EMPTY.withColor(0xFF5555));

        Component modsList = Component.literal(modsJoined)
                .setStyle(Style.EMPTY.withColor(0xAAAAAA));

        Component supportLine1 = Component.translatable(
                "mod.join.prohibited.supportLine1"
        ).setStyle(Style.EMPTY.withColor(0xAAAAAA));

        Component supportUrl = Component.literal("https://eternalempires.link/0c942d")
                .setStyle(Style.EMPTY
                        .withColor(0xFF5555) // red
                        .withUnderlined(true) // underline so it looks clickable
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://eternalempires.link/0c942d")))
                );

        Component disconnectReason = Component.empty()
                .append(title).append("\n")
                .append(failed).append("\n\n")
                .append(explanation).append("\n\n")
                .append(modsHeader).append("\n")
                .append(modsList).append("\n\n")
                .append(supportLine1).append(supportUrl);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().getConnection().disconnect(disconnectReason);
        }
/*
        minecraft.execute(() -> {
            minecraft.setScreen(new DisconnectedScreen(
                    new TitleScreen(),
                    Component.literal("Disconnected"),
                    disconnectReason
            ));
        });


 */
    }
}