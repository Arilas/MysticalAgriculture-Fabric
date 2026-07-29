package com.blakebr0.mysticalagriculture.crafting.condition;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;

public final class SeedCraftingRecipesEnabledCondition implements ResourceCondition {
    public static final MapCodec<SeedCraftingRecipesEnabledCondition> CODEC =
            MapCodec.unit(SeedCraftingRecipesEnabledCondition::new);
    public static final ResourceConditionType<SeedCraftingRecipesEnabledCondition> TYPE =
            ResourceConditionType.create(
                    MysticalAgricultureAPI.resource("seed_crafting_recipes_enabled"),
                    CODEC
            );

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup registryInfo) {
        return ModConfigs.SEED_CRAFTING_RECIPES.get();
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }
}
