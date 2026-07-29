package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.world.feature.SoulstoneFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public final class ModWorldFeatures {
    public static final Feature<OreConfiguration> SOULSTONE =
            new SoulstoneFeature(OreConfiguration.CODEC);

    public static void register() {
        Registry.register(
                BuiltInRegistries.FEATURE,
                MysticalAgricultureAPI.resource("soulstone"),
                SOULSTONE
        );
    }

    private ModWorldFeatures() {
    }
}
