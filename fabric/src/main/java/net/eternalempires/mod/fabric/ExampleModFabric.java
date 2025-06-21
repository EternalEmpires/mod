package net.eternalempires.mod.fabric;

import net.fabricmc.api.ModInitializer;
import net.eternalempires.mod.common.ExampleMod;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.init();
    }
}
