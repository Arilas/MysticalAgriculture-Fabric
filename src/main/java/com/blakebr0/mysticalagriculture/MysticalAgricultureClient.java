package com.blakebr0.mysticalagriculture;

import com.blakebr0.mysticalagriculture.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public final class MysticalAgricultureClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNetworkHandler.register();

        // Client registration moves here as each NeoForge client event subscriber is
        // replaced with its Fabric registration method in the dedicated client task.
    }
}
