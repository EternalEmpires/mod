package net.eternalempires.mod.fabric;

import net.fabricmc.api.ModInitializer;
import net.eternalempires.mod.common.EternalEmpires;

public class EternalEmpiresFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EternalEmpires.init();
    }
}
