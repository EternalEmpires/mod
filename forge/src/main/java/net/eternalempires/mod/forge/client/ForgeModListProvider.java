package net.eternalempires.mod.forge.client;

import net.eternalempires.mod.common.util.modlistcheck.IModListProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ForgeModListProvider implements IModListProvider {

    @Override
    public Set<String> getInstalledModIds() {
        return ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, String> getModIdToNameMap() {
        Map<String, String> modIdToNameMap = new HashMap<>();

        for (IModInfo modInfo : ModList.get().getMods()) {
            String modId = modInfo.getModId();
            String modName = modInfo.getDisplayName();
            modIdToNameMap.put(modId, modName);
        }

        return modIdToNameMap;
    }
}