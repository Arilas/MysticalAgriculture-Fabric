package com.blakebr0.mysticalagriculture.lib;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.IMysticalAgriculturePlugin;
import com.blakebr0.mysticalagriculture.api.crop.CropTier;
import com.blakebr0.mysticalagriculture.api.crop.CropType;
import com.blakebr0.mysticalagriculture.api.lib.PluginConfig;
import com.blakebr0.mysticalagriculture.api.registry.IAugmentRegistry;
import com.blakebr0.mysticalagriculture.api.registry.ICropRegistry;
import com.blakebr0.mysticalagriculture.api.registry.IMobSoulTypeRegistry;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import net.minecraft.ChatFormatting;

public final class ModCorePlugin implements IMysticalAgriculturePlugin {
    public static final CropTier AWAKENED_SUPREMIUM_TIER = new CropTier(MysticalAgriculture.resource("5"), 6, 0xC47200, ChatFormatting.RED);

    @Override
    public void configure(PluginConfig config) {
        config.setModId(MysticalAgriculture.MOD_ID);
        config.disableDynamicSeedReprocessingRecipes();
    }

    @Override
    public void onRegisterCrops(ICropRegistry registry) {
        registry.registerTier(CropTier.ELEMENTAL);
        registry.registerTier(CropTier.ONE);
        registry.registerTier(CropTier.TWO);
        registry.registerTier(CropTier.THREE);
        registry.registerTier(CropTier.FOUR);
        registry.registerTier(CropTier.FIVE);

        registry.registerType(CropType.RESOURCE);
        registry.registerType(CropType.MOB);

        ModCrops.onRegisterCrops(registry);
    }

    @Override
    public void onPostRegisterCrops(ICropRegistry registry) {
        CropTier.ELEMENTAL.setFarmlandBlock(() -> ModBlocks.INFERIUM_FARMLAND).setEssenceItem(() -> ModItems.INFERIUM_ESSENCE);
        CropTier.ONE.setFarmlandBlock(() -> ModBlocks.INFERIUM_FARMLAND).setEssenceItem(() -> ModItems.INFERIUM_ESSENCE);
        CropTier.TWO.setFarmlandBlock(() -> ModBlocks.PRUDENTIUM_FARMLAND).setEssenceItem(() -> ModItems.PRUDENTIUM_ESSENCE);
        CropTier.THREE.setFarmlandBlock(() -> ModBlocks.TERTIUM_FARMLAND).setEssenceItem(() -> ModItems.TERTIUM_ESSENCE);
        CropTier.FOUR.setFarmlandBlock(() -> ModBlocks.IMPERIUM_FARMLAND).setEssenceItem(() -> ModItems.IMPERIUM_ESSENCE);
        CropTier.FIVE.setFarmlandBlock(() -> ModBlocks.SUPREMIUM_FARMLAND).setEssenceItem(() -> ModItems.SUPREMIUM_ESSENCE);

        CropType.RESOURCE.setCraftingSeedItem(() -> ModItems.PROSPERITY_SEED_BASE);
        CropType.MOB.setCraftingSeedItem(() -> ModItems.SOULIUM_SEED_BASE);

        AWAKENED_SUPREMIUM_TIER.setFarmlandBlock(() -> ModBlocks.AWAKENED_SUPREMIUM_FARMLAND).setEssenceItem(() -> ModItems.AWAKENED_SUPREMIUM_ESSENCE);
    }

    @Override
    public void onRegisterMobSoulTypes(IMobSoulTypeRegistry registry) {
        ModMobSoulTypes.onRegisterMobSoulTypes(registry);
    }

    @Override
    public void onRegisterAugments(IAugmentRegistry registry) {
        ModAugments.onRegisterAugments(registry);
    }
}
