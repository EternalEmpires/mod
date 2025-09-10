package net.eternalempires.mod.common.util.modlistcheck;

import java.util.Map;
import java.util.Set;

public interface IModListProvider {
    Set<String> getInstalledModIds();

    /**
     * Gets a map of mod ID to mod name for all installed mods
     * @return Map where key is mod ID and value is mod display name
     */
    Map<String, String> getModIdToNameMap();
}