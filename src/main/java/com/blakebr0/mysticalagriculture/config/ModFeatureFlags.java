package com.blakebr0.mysticalagriculture.config;

import com.blakebr0.cucumber.util.FeatureFlag;
import com.blakebr0.cucumber.util.FeatureFlags;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;

@FeatureFlags
public final class ModFeatureFlags {
    // ConfigValue implements Supplier<Boolean>, so feature flags stay bound to
    // the current validated value without exposing the JSON loader.
    public static final FeatureFlag DRAGON_DROPS_COGNIZANT = FeatureFlag.create(MysticalAgriculture.resource("dragon_crops_cognizant"), ModConfigs.DRAGON_DROPS_COGNIZANT);
    public static final FeatureFlag DRAGON_DROPS_ESSENCE = FeatureFlag.create(MysticalAgriculture.resource("dragon_drops_essence"), ModConfigs.DRAGON_DROPS_ESSENCE);
    public static final FeatureFlag ESSENCE_FARMLAND_CONVERSION = FeatureFlag.create(MysticalAgriculture.resource("essence_farmland_conversion"), ModConfigs.ESSENCE_FARMLAND_CONVERSION);
    public static final FeatureFlag FAKE_PLAYER_WATERING = FeatureFlag.create(MysticalAgriculture.resource("fake_player_watering"), ModConfigs.FAKE_PLAYER_WATERING);
    public static final FeatureFlag GENERATE_INFERIUM = FeatureFlag.create(MysticalAgriculture.resource("generate_inferium"), ModConfigs.GENERATE_INFERIUM);
    public static final FeatureFlag GENERATE_PROSPERITY = FeatureFlag.create(MysticalAgriculture.resource("generate_prosperity"), ModConfigs.GENERATE_PROSPERITY);
    public static final FeatureFlag GENERATE_SOULSTONE = FeatureFlag.create(MysticalAgriculture.resource("generate_soulstone"), ModConfigs.GENERATE_SOULSTONE);
    public static final FeatureFlag REQUIRES_EFFECTIVE_FARMLAND = FeatureFlag.create(MysticalAgriculture.resource("requires_effective_farmland"), ModConfigs.REQUIRES_EFFECTIVE_FARMLAND);
    public static final FeatureFlag SECONDARY_SEED_DROPS = FeatureFlag.create(MysticalAgriculture.resource("secondary_seed_drops"), ModConfigs.SECONDARY_SEED_DROPS);
    public static final FeatureFlag SEED_CRAFTING_RECIPES = FeatureFlag.create(MysticalAgriculture.resource("seed_crafting_recipes"), ModConfigs.SEED_CRAFTING_RECIPES);
    public static final FeatureFlag UNBREAKABLE_SUPREMIUM_ARMOR = FeatureFlag.create(MysticalAgriculture.resource("unbreakable_supremium_armor"), ModConfigs.UNBREAKABLE_SUPREMIUM_ARMOR);
    public static final FeatureFlag WITHER_DROPS_COGNIZANT = FeatureFlag.create(MysticalAgriculture.resource("wither_drops_cognizant"), ModConfigs.WITHER_DROPS_COGNIZANT);
    public static final FeatureFlag WITHER_DROPS_ESSENCE = FeatureFlag.create(MysticalAgriculture.resource("wither_drops_essence"), ModConfigs.WITHER_DROPS_ESSENCE);
}
