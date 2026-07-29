package com.blakebr0.mysticalagriculture.registry;

import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.lib.ModCorePlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.List;

public final class FabricPluginRegistry {
    public static PluginRegistry create() {
        return new PluginRegistry(
                FabricPluginRegistry::getFabricCandidates,
                new ModCorePlugin(),
                CropRegistry.getInstance(),
                AugmentRegistry.getInstance(),
                MobSoulTypeRegistry.getInstance()
        );
    }

    static PluginRegistry.PluginCandidate toCandidate(
            EntrypointContainer<IMysticalAgriculturePlugin> container
    ) {
        return new PluginRegistry.PluginCandidate(
                container.getProvider().getMetadata().getId(),
                container.getDefinition(),
                container::getEntrypoint
        );
    }

    private static List<PluginRegistry.PluginCandidate> getFabricCandidates() {
        return FabricLoader.getInstance()
                .getEntrypointContainers(PluginRegistry.ENTRYPOINT_KEY, IMysticalAgriculturePlugin.class)
                .stream()
                .map(FabricPluginRegistry::toCandidate)
                .toList();
    }

    private FabricPluginRegistry() {
    }
}
