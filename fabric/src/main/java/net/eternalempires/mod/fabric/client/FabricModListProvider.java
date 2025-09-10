package net.eternalempires.mod.fabric.client;

import net.eternalempires.mod.common.util.modlistcheck.IModListProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FabricModListProvider implements IModListProvider {

    @Override
    public Set<String> getInstalledModIds() {
        return FabricLoader.getInstance().getAllMods().stream()
                .map(modContainer -> modContainer.getMetadata().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getModIdToNameMap() {
        Map<String, String> modIdToNameMap = new HashMap<>();

        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            String modId = modContainer.getMetadata().getId();
            String modName = modContainer.getMetadata().getName();
            modIdToNameMap.put(modId, modName);
        }

        return modIdToNameMap;
    }
}