package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.crafting.condition.AugmentEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.CropEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.CropHasMaterialCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.SeedCraftingRecipesEnabledCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

public final class ModConditionSerializers {
    public static void register() {
        ResourceConditions.register(CropEnabledCondition.TYPE);
        ResourceConditions.register(AugmentEnabledCondition.TYPE);
        ResourceConditions.register(CropHasMaterialCondition.TYPE);
        ResourceConditions.register(SeedCraftingRecipesEnabledCondition.TYPE);
    }

    private ModConditionSerializers() {
    }
}
