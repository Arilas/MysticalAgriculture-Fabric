package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IOreInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipeTypes {
    public static final RecipeType<IInfusionRecipe> INFUSION = type("infusion");
    public static final RecipeType<IAwakeningRecipe> AWAKENING = type("awakening");
    public static final RecipeType<IEnchanterRecipe> ENCHANTER = type("enchanter");
    public static final RecipeType<IReprocessorRecipe> REPROCESSOR = type("reprocessor");
    public static final RecipeType<ISoulExtractionRecipe> SOUL_EXTRACTION = type("soul_extraction");
    public static final RecipeType<ISouliumSpawnerRecipe> SOULIUM_SPAWNER = type("soulium_spawner");
    public static final RecipeType<IOreInfusionRecipe> ORE_INFUSION = type("ore_infusion");

    public static void register() {
        register("infusion", INFUSION);
        register("awakening", AWAKENING);
        register("enchanter", ENCHANTER);
        register("reprocessor", REPROCESSOR);
        register("soul_extraction", SOUL_EXTRACTION);
        register("soulium_spawner", SOULIUM_SPAWNER);
        register("ore_infusion", ORE_INFUSION);
    }

    private static <T extends net.minecraft.world.item.crafting.Recipe<?>> RecipeType<T> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return MysticalAgricultureAPI.resource(name).toString();
            }
        };
    }

    private static void register(String name, RecipeType<?> type) {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, MysticalAgricultureAPI.resource(name), type);
    }

    private ModRecipeTypes() {
    }
}
