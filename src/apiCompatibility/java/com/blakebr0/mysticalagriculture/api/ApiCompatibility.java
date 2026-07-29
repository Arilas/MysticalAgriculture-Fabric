package com.blakebr0.mysticalagriculture.api;

import com.blakebr0.mysticalagriculture.api.components.AugmentComponent;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.tinkering.Augment;
import net.minecraft.core.component.DataComponentType;

import java.util.List;

/**
 * Compile-only declarations representing a Fabric add-on's view of the public API.
 */
final class ApiCompatibility {
    private ICropRegistry crops;
    private IAugmentRegistry augments;
    private IMobSoulTypeRegistry souls;
    private Crop crop;
    private Augment augment;
    private MobSoulType soul;
    private MysticalAgricultureConfigValues config;
    private IInfusionRecipe recipe;

    private DataComponentType<List<AugmentComponent>> equippedAugments =
            MysticalAgricultureDataComponentTypes.EQUIPPED_AUGMENTS;
    private boolean secondarySeedDrops =
            MysticalAgricultureAPI.getConfigValues().isSecondarySeedDropsEnabled();
}
