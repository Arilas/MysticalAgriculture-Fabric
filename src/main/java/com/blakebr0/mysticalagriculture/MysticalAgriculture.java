package com.blakebr0.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureConfigValues;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModCreativeModeTabs;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.registry.AugmentRegistry;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.mysticalagriculture.registry.FabricPluginRegistry;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MysticalAgriculture implements ModInitializer {
    public static final String MOD_ID = "mysticalagriculture";
    public static final String NAME = "Mystical Agriculture";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    @Override
    public void onInitialize() {
        // 1. Configuration
        ModConfigs.load();

        // 2. Public API references
        initAPI();

        // 3. Built-in and external plug-in discovery
        var plugins = FabricPluginRegistry.create();
        plugins.loadPlugins();

        // 4. Content collection and vanilla registration
        plugins.collectContent();
        ModBlocks.register();
        ModBlocks.registerBlockItems();
        ModItems.register();
        ModDataComponentTypes.register();
        ModTileEntities.register();
        ModMenuTypes.register();
        ModCreativeModeTabs.register();

        // 5. Recipe, world, network, and storage registration are installed by Tasks 3 and 4.

        // 6. Dynamic registry finalization
        plugins.finalizeContent();

        // 7. Callbacks and reload listeners are installed by the gameplay and lifecycle tasks.
    }

    public static Identifier resource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void initAPI() {
        MysticalAgricultureAPI.bootstrap(
                CropRegistry.getInstance(),
                AugmentRegistry.getInstance(),
                MobSoulTypeRegistry.getInstance(),
                new MysticalAgricultureConfigValues(
                        ModConfigs.INFERIUM_DROP_CHANCE::get,
                        ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE::get,
                        ModConfigs.SECONDARY_SEED_DROPS::get,
                        ModConfigs.REQUIRES_EFFECTIVE_FARMLAND::get,
                        ModConfigs.UNBREAKABLE_SUPREMIUM_ARMOR::get,
                        ModConfigs.FAKE_PLAYER_WATERING::get
                )
        );
    }
}
