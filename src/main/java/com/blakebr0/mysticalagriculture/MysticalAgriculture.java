package com.blakebr0.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureConfigValues;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.crafting.DynamicRecipeManager;
import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.handler.ModStorageProviders;
import com.blakebr0.mysticalagriculture.handler.ReloadSyncHandler;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModCreativeModeTabs;
import com.blakebr0.mysticalagriculture.init.ModDataComponentTypes;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModConditionSerializers;
import com.blakebr0.mysticalagriculture.init.ModIngredientTypes;
import com.blakebr0.mysticalagriculture.init.ModMenuTypes;
import com.blakebr0.mysticalagriculture.init.ModTileEntities;
import com.blakebr0.mysticalagriculture.init.ModRecipeSerializers;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.init.ModWorldFeatures;
import com.blakebr0.mysticalagriculture.registry.AugmentRegistry;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.mysticalagriculture.registry.FabricPluginRegistry;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.network.NetworkHandler;
import com.blakebr0.mysticalagriculture.util.RecipeIngredientCache;
import com.blakebr0.mysticalagriculture.world.ModWorldGeneration;
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
        ModWorldFeatures.register();

        // 5. Data-driven content and world generation
        ModIngredientTypes.register();
        ModConditionSerializers.register();
        ModRecipeTypes.register();
        ModRecipeSerializers.register();
        ModWorldGeneration.register();

        // 6. Dynamic registry finalization
        plugins.finalizeContent();

        // 7. Recipe lifecycle and server-data reload state
        DynamicRecipeManager.register();
        RecipeIngredientCache.register();
        EssenceVesselColorManager.register();

        // 8. Stateful machine boundaries and reload synchronization
        NetworkHandler.register();
        ModStorageProviders.register();
        ReloadSyncHandler.register();

        // 9. Remaining gameplay and client lifecycle hooks are installed by Tasks 5–7.
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
