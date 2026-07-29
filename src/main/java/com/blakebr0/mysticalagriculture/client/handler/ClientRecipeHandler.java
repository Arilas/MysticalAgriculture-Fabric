package com.blakebr0.mysticalagriculture.client.handler;

import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IOreInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.tileentity.SouliumSpawnerTileEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ClientRecipeHandler {
    private static boolean registered;

    public static final List<RecipeHolder<IAwakeningRecipe>> AWAKENING_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<IEnchanterRecipe>> ENCHANTER_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<IInfusionRecipe>> INFUSION_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<IReprocessorRecipe>> REPROCESSOR_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<ISoulExtractionRecipe>> SOUL_EXTRACTION_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<ISouliumSpawnerRecipe>> SOULIUM_SPAWNER_RECIPES = new ArrayList<>();
    public static final List<RecipeHolder<IOreInfusionRecipe>> ORE_INFUSION_RECIPES = new ArrayList<>();

    public static final Map<Identifier, IAwakeningRecipe> AWAKENING_RECIPE_MAP = new LinkedHashMap<>();
    public static final Map<Identifier, IInfusionRecipe> INFUSION_RECIPE_MAP = new LinkedHashMap<>();
    public static final Map<Identifier, ISouliumSpawnerRecipe> SOULIUM_SPAWNER_RECIPE_MAP = SouliumSpawnerTileEntity.CLIENT_RECIPE_MAP;

    private ClientRecipeHandler() {
    }

    public static synchronized void register() {
        if (registered)
            return;

        ClientRecipeSynchronizedEvent.EVENT.register((_, recipes) -> replaceRecipes(recipes));
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> clear());
        registered = true;
    }

    static void replaceRecipes(SynchronizedRecipes recipes) {
        clear();

        AWAKENING_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.AWAKENING));
        ENCHANTER_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.ENCHANTER));
        INFUSION_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.INFUSION));
        REPROCESSOR_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.REPROCESSOR));
        SOUL_EXTRACTION_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.SOUL_EXTRACTION));
        SOULIUM_SPAWNER_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.SOULIUM_SPAWNER));
        ORE_INFUSION_RECIPES.addAll(recipes.getAllOfType(ModRecipeTypes.ORE_INFUSION));

        map(AWAKENING_RECIPES, AWAKENING_RECIPE_MAP);
        map(INFUSION_RECIPES, INFUSION_RECIPE_MAP);
        map(SOULIUM_SPAWNER_RECIPES, SOULIUM_SPAWNER_RECIPE_MAP);
    }

    public static void clear() {
        AWAKENING_RECIPES.clear();
        ENCHANTER_RECIPES.clear();
        INFUSION_RECIPES.clear();
        REPROCESSOR_RECIPES.clear();
        SOUL_EXTRACTION_RECIPES.clear();
        SOULIUM_SPAWNER_RECIPES.clear();
        ORE_INFUSION_RECIPES.clear();

        AWAKENING_RECIPE_MAP.clear();
        INFUSION_RECIPE_MAP.clear();
        SOULIUM_SPAWNER_RECIPE_MAP.clear();
    }

    private static <T extends Recipe<?>> void map(List<RecipeHolder<T>> list, Map<Identifier, T> map) {
        for (var recipe : list) {
            map.put(recipe.id().identifier(), recipe.value());
        }
    }
}
