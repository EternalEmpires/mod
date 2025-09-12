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

package net.eternalempires.mod.neoforge.client;

import net.eternalempires.mod.common.util.modlistcheck.IModListProvider;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * NeoForge-specific implementation of {@link IModListProvider}.
 * Uses the {@link ModList} API provided by NeoForge to query all mods
 * visible to the client runtime. Supplies both the set of installed
 * mod IDs and a mapping of mod IDs to their display names.
 * This implementation is registered during NeoForge client initialization
 * so that {@link net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler}
 * can enforce server-defined mod restrictions.
 *
 * @see IModListProvider
 * @see net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler
 *
 * @since 09/07/2025
 * @author EternalEmpires
 */
public class NeoForgeModListProvider implements IModListProvider {

    /**
     * Retrieves the set of all installed mod IDs visible to the NeoForge client.
     * <p>
     * This implementation queries the {@link ModList} provided by NeoForge and
     * collects the {@code modId} from each {@link IModInfo}.
     *
     * @return a set containing the IDs of all installed mods
     */
    @Override
    public @NotNull Set<String> getInstalledModIds() {
        return ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .collect(Collectors.toSet());
    }

    /**
     * Builds a mapping of installed mod IDs to their corresponding display names.
     * <p>
     * This method iterates over all {@link IModInfo} entries reported by
     * {@link ModList}, extracting both the {@code modId} and the human-readable
     * {@code displayName} for each mod.
     *
     * @return a map where keys are mod IDs and values are their display names
     */
    @Override
    public @NotNull Map<String, String> getModIdToNameMap() {
        final Map<String, String> modIdToNameMap = new HashMap<>();

        for (IModInfo modInfo : ModList.get().getMods()) {
            final String modId = modInfo.getModId();
            final String modName = modInfo.getDisplayName();
            modIdToNameMap.put(modId, modName);
        }

        return modIdToNameMap;
    }
}