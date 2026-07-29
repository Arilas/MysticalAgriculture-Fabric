package com.blakebr0.mysticalagriculture.init;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.crafting.recipe.AwakeningRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.EnchanterRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.FarmlandTillRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.OreInfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SoulJarEmptyRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SouliumSpawnerRecipe;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializers {
    public static final RecipeSerializer<FarmlandTillRecipe> CRAFTING_FARMLAND_TILL = FarmlandTillRecipe.SERIALIZER;
    public static final RecipeSerializer<InfusionRecipe> INFUSION = InfusionRecipe.SERIALIZER;
    public static final RecipeSerializer<AwakeningRecipe> AWAKENING = AwakeningRecipe.SERIALIZER;
    public static final RecipeSerializer<EnchanterRecipe> ENCHANTER = EnchanterRecipe.SERIALIZER;
    public static final RecipeSerializer<ReprocessorRecipe> REPROCESSOR = ReprocessorRecipe.SERIALIZER;
    public static final RecipeSerializer<SoulExtractionRecipe> SOUL_EXTRACTION = SoulExtractionRecipe.SERIALIZER;
    public static final RecipeSerializer<SouliumSpawnerRecipe> SOULIUM_SPAWNER = SouliumSpawnerRecipe.SERIALIZER;
    public static final RecipeSerializer<OreInfusionRecipe> ORE_INFUSION = OreInfusionRecipe.SERIALIZER;
    public static final RecipeSerializer<SoulJarEmptyRecipe> CRAFTING_SOUL_JAR_EMPTY = SoulJarEmptyRecipe.SERIALIZER;

    public static void register() {
        register(BuiltInRegistries.RECIPE_SERIALIZER);
        synchronize(CRAFTING_FARMLAND_TILL);
        synchronize(INFUSION);
        synchronize(AWAKENING);
        synchronize(ENCHANTER);
        synchronize(REPROCESSOR);
        synchronize(SOUL_EXTRACTION);
        synchronize(SOULIUM_SPAWNER);
        synchronize(ORE_INFUSION);
        synchronize(CRAFTING_SOUL_JAR_EMPTY);
    }

    public static void register(Registry<RecipeSerializer<?>> registry) {
        register(registry, "farmland_till", CRAFTING_FARMLAND_TILL);
        register(registry, "infusion", INFUSION);
        register(registry, "awakening", AWAKENING);
        register(registry, "enchanter", ENCHANTER);
        register(registry, "reprocessor", REPROCESSOR);
        register(registry, "soul_extraction", SOUL_EXTRACTION);
        register(registry, "soulium_spawner", SOULIUM_SPAWNER);
        register(registry, "ore_infusion", ORE_INFUSION);
        register(registry, "soul_jar_empty", CRAFTING_SOUL_JAR_EMPTY);
    }

    private static void register(
            Registry<RecipeSerializer<?>> registry,
            String name,
            RecipeSerializer<?> serializer
    ) {
        Registry.register(
                registry,
                MysticalAgricultureAPI.resource(name),
                serializer
        );
    }

    private static void synchronize(RecipeSerializer<?> serializer) {
        RecipeSynchronization.synchronizeRecipeSerializer(serializer);
    }

    private ModRecipeSerializers() {
    }
}
