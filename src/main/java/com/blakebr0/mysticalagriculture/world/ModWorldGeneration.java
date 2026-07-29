package com.blakebr0.mysticalagriculture.world;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class ModWorldGeneration {
    public static final ResourceKey<PlacedFeature> PROSPERITY_ORE = placedFeature("prosperity_ore");
    public static final ResourceKey<PlacedFeature> INFERIUM_ORE = placedFeature("inferium_ore");
    public static final ResourceKey<PlacedFeature> SOULSTONE = placedFeature("soulstone");

    public static void register() {
        if (ModConfigs.GENERATE_PROSPERITY.get()) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    PROSPERITY_ORE
            );
        }

        if (ModConfigs.GENERATE_INFERIUM.get()) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                    GenerationStep.Decoration.UNDERGROUND_ORES,
                    INFERIUM_ORE
            );
        }

        if (ModConfigs.GENERATE_SOULSTONE.get()) {
            BiomeModifications.addFeature(
                    BiomeSelectors.tag(BiomeTags.IS_NETHER),
                    GenerationStep.Decoration.RAW_GENERATION,
                    SOULSTONE
            );
        }
    }

    private static ResourceKey<PlacedFeature> placedFeature(String path) {
        return ResourceKey.create(
                Registries.PLACED_FEATURE,
                MysticalAgricultureAPI.resource(path)
        );
    }

    private ModWorldGeneration() {
    }
}
