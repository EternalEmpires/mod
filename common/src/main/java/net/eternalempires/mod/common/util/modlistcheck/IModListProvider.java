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

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;


/**
 * Abstraction for retrieving information about installed mods on the client.
 * Each supported mod loader (e.g., Fabric, Forge, NeoForge) must provide its
 * own implementation of this interface so that {@link ModCheckHandler} can
 * query the installed mods in a loader-agnostic way.
 * Implementations are expected to return both the set of installed mod IDs and
 * a mapping from mod ID to a user-friendly display name.
 *
 * @see ModCheckHandler
 *
 * @since 09/07/2025
 * @author EternalEmpires
 */
public interface IModListProvider {

    /**
     * Gets the set of installed mod IDs on the client.
     *
     * @return a non-null set of mod IDs (may be empty, but never null)
     */
    @NotNull Set<@NotNull String> getInstalledModIds();

    /**
     * Gets a map of mod ID to mod display name for all installed mods.
     *
     * @return a non-null map where keys are non-null mod IDs and values are
     * non-null display names (may be empty, but never null)
     */
    @NotNull Map<@NotNull String, @NotNull String> getModIdToNameMap();
}