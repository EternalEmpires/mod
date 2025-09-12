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
 *s
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

package net.eternalempires.mod.common.util.modlistcheck;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central handler responsible for verifying client-installed mods against
 * a server-provided disallowed list.
 * This class delegates to a loader-specific {@link IModListProvider} (set
 * during each loader’s initialization) to gather the installed mods. It then
 * compares the results to the server’s prohibited list and disconnects the
 * player if any matches are found.
 * On violation, the handler constructs a styled, localized disconnect screen
 * detailing which mods caused the rejection and providing a support URL for
 * further guidance.
 *
 * @see IModListProvider
 * @see net.eternalempires.mod.common.network.packet.ModCheckPayload
 *
 * @since 09/07/2025
 * @author EternalEmpires
 */
@Slf4j
public class ModCheckHandler {

    /**
     * -- SETTER --
     *  This should be called by each mod loader's initialization to set their specific provider
     */
    @Setter
    private static IModListProvider modListProvider;

    /**
     * Performs a client-side check against a list of server-provided
     * disallowed mod IDs.
     * <p>
     * This method queries the loader-specific {@link IModListProvider}
     * for installed mods, compares them against the prohibited list
     * sent by the server, and takes appropriate action:
     * <ul>
     *     <li>If no disallowed mods are found, the connection proceeds normally.</li>
     *     <li>If one or more disallowed mods are detected, the client is
     *     disconnected with a styled error message.</li>
     * </ul>
     *
     * @param serverDisallowedMods the list of mod IDs the server has prohibited
     */
    public static void performModCheck(@NotNull final List<String> serverDisallowedMods) {
        if (modListProvider == null) {
            log.warn("[EternalEmpires] No mod list provider set! Cannot perform mod check.");
            return;
        }

        final Set<String> installedMods = modListProvider.getInstalledModIds();
        log.info("[EternalEmpires] Client installed mods: {}", installedMods);

        final List<String> foundDisallowedMods = serverDisallowedMods.stream()
                .filter(installedMods::contains)
                .collect(Collectors.toList());

        if (!foundDisallowedMods.isEmpty()) {
            log.warn("[EternalEmpires] Found disallowed mods on client: {}", foundDisallowedMods);
            disconnectWithModError(foundDisallowedMods);
        } else {
            log.info("[EternalEmpires] No disallowed mods found, connection allowed");
        }
    }

    /**
     * Disconnects the client with a custom error screen when one or more
     * prohibited mods are detected.
     * <p>
     * The disconnect message includes:
     * <ul>
     *     <li>Server branding/title</li>
     *     <li>A localized failure explanation</li>
     *     <li>The list of offending mods by display name</li>
     *     <li>A clickable support URL for further guidance</li>
     * </ul>
     *
     * @param foundModIds the IDs of prohibited mods detected on the client
     */
    private static void disconnectWithModError(@NotNull final List<String> foundModIds) {
        log.info("[EternalEmpires] Disconnecting player due to disallowed mods: {}", foundModIds);

        final Map<String, String> modIdToNameMap = modListProvider.getModIdToNameMap();

        final Component title = Component.literal("Eternal Empires MMORPG")
                .setStyle(Style.EMPTY.withColor(0xFF5555).withBold(true));

        final Component failed = Component.translatable("mod.join.prohibited.failed")
                .setStyle(Style.EMPTY.withColor(0xFF5555));

        final Component explanation = Component.translatable(
                "mod.join.prohibited.explanation"
        ).setStyle(Style.EMPTY.withColor(0xAAAAAA));

        final String modsJoined = foundModIds.stream()
                .map(id -> modIdToNameMap.getOrDefault(id, id))
                .collect(Collectors.joining(", "));

        final Component modsHeader = Component.translatable("mod.join.prohibited.modsHeader")
                .setStyle(Style.EMPTY.withColor(0xFF5555));

        final Component modsList = Component.literal(modsJoined)
                .setStyle(Style.EMPTY.withColor(0xAAAAAA));

        final Component supportLine1 = Component.translatable(
                "mod.join.prohibited.supportLine1"
        ).setStyle(Style.EMPTY.withColor(0xAAAAAA));

        final Component supportUrl = Component.literal("https://eternalempires.link/0c942d")
                .setStyle(Style.EMPTY
                        .withColor(0xFF5555) // red
                        .withUnderlined(true) // underline so it looks clickable
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://eternalempires.link/0c942d")))
                );

        final Component disconnectReason = Component.empty()
                .append(title).append("\n")
                .append(failed).append("\n\n")
                .append(explanation).append("\n\n")
                .append(modsHeader).append("\n")
                .append(modsList).append("\n\n")
                .append(supportLine1).append(supportUrl);

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().getConnection().disconnect(disconnectReason);
        }
    }
}