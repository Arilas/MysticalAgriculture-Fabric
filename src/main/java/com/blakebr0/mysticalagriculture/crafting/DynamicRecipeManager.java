package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.cucumber.event.RecipeManagerLoadingEvent;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.List;
import java.util.Map;

public class DynamicRecipeManager {
    public static final DynamicRecipeManager INSTANCE = new DynamicRecipeManager();

    public static void register() {
        RecipeManagerLoadingEvent.EVENT.register(INSTANCE::onRecipeManagerLoading);
    }

    public void onRecipeManagerLoading(RecipeManagerLoadingEvent event) {
        var registries = event.getRegistries();

        for (var crop : CropRegistry.getInstance().getCrops()) {
            var seed = makeSeedRecipe(crop, registries);
            var seedRegular = makeRegularSeedRecipe(crop, registries);
            var reprocessor = makeReprocessorRecipe(crop);

            if (seed != null)
                event.addRecipe(seed);

            if (seedRegular != null)
                event.addRecipe(seedRegular);

            if (reprocessor != null)
                event.addRecipe(reprocessor);
        }
    }

    private static RecipeHolder<Recipe<?>> makeSeedRecipe(Crop crop, HolderLookup.Provider registries) {
        if (!crop.isEnabled() || !crop.getRecipeConfig().isSeedInfusionRecipeEnabled())
            return null;

        var essenceItem = crop.getTier().getEssenceItem();
        if (essenceItem == null)
            return null;

        var craftingSeedItem = crop.getType().getCraftingSeedItem();
        if (craftingSeedItem == null)
            return null;

        var material = crop.getCraftingMaterial(registries);
        if (material == null)
            return null;

        var essence = Ingredient.of(essenceItem);
        var craftingSeed = Ingredient.of(craftingSeedItem);
        var inputs = List.of(
                material, essence, material, essence, material, essence, material, essence
        );

        var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("seeds_infusion"));
        var result = new ItemStackTemplate(crop.getSeedsItem());

        return new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, id),
                new InfusionRecipe(craftingSeed, inputs, result, false)
        );
    }

    private static RecipeHolder<Recipe<?>> makeRegularSeedRecipe(Crop crop, HolderLookup.Provider registries) {
        if (!crop.isEnabled() || !crop.getRecipeConfig().isSeedCraftingRecipeEnabled())
            return null;

        if (!ModConfigs.SEED_CRAFTING_RECIPES.get())
            return null;

        var essenceItem = crop.getTier().getEssenceItem();
        if (essenceItem == null)
            return null;

        var craftingSeedItem = crop.getType().getCraftingSeedItem();
        if (craftingSeedItem == null)
            return null;

        var material = crop.getCraftingMaterial(registries);
        if (material == null)
            return null;

        var essence = Ingredient.of(essenceItem);
        var craftingSeed = Ingredient.of(craftingSeedItem);
        var keys = Map.of(
                'M', material,
                'E', essence,
                'C', craftingSeed
        );
        var shape = List.of(
                "MEM",
                "ECE",
                "MEM"
        );

        var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("seeds_vanilla"));
        var pattern = ShapedRecipePattern.of(keys, shape);
        var result = new ItemStackTemplate(crop.getSeedsItem());

        return new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, id),
                new ShapedRecipe(new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "mysticalagriculture:seeds"), pattern, result)
        );
    }

    private static RecipeHolder<Recipe<?>> makeReprocessorRecipe(Crop crop) {
        if (!crop.isEnabled() || !crop.getRecipeConfig().isSeedReprocessorRecipeEnabled())
            return null;

        var input = Ingredient.of(crop.getSeedsItem());
        var id = MysticalAgricultureAPI.resource(crop.getNameWithSuffix("seeds_reprocessor"));
        var result = new ItemStackTemplate(crop.getEssenceItem(), 2);

        return new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, id),
                new ReprocessorRecipe(input, result)
        );
    }
}
