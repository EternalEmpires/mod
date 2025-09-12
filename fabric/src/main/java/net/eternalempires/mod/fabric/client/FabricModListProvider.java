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

package net.eternalempires.mod.fabric.client;

import net.eternalempires.mod.common.util.modlistcheck.IModListProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Fabric-specific implementation of {@link IModListProvider}.
 * Uses the {@link FabricLoader} API to query all mods visible to the
 * Fabric client runtime. Provides both the set of installed mod IDs
 * and a mapping of mod IDs to their display names.
 * This implementation is registered by the Fabric loader during
 * client initialization so that {@link net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler}
 * can perform server-side mod validation.
 *
 * @see IModListProvider
 * @see net.eternalempires.mod.common.util.modlistcheck.ModCheckHandler
 *
 * @since 09/07/2025
 * @author EternalEmpires
 */
public class FabricModListProvider implements IModListProvider {

    /**
     * Retrieves the set of all installed mod IDs visible to the Fabric client.
     * <p>
     * This implementation queries the {@link FabricLoader} runtime and collects
     * the {@code id} from each discovered {@link ModContainer}.
     *
     * @return a set containing the IDs of all installed mods
     */
    @Override
    public @NotNull Set<String> getInstalledModIds() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(modContainer -> modContainer.getMetadata().getId())
                .collect(Collectors.toSet());
    }

    /**
     * Builds a mapping of installed mod IDs to their corresponding display names.
     * <p>
     * This method iterates over all {@link ModContainer} instances reported by
     * {@link FabricLoader}, extracting both the {@code id} and {@code name}
     * from each mod’s metadata.
     *
     * @return a map where keys are mod IDs and values are their human-readable names
     */
    @Override
    public @NotNull Map<String, String> getModIdToNameMap() {
        final Map<String, String> modIdToNameMap = new HashMap<>();

        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            final String modId = modContainer.getMetadata().getId();
            final String modName = modContainer.getMetadata().getName();
            modIdToNameMap.put(modId, modName);
        }

        return modIdToNameMap;
    }
}