package com.blakebr0.mysticalagriculture.api.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

import java.util.List;
import java.util.Map;

/**
 * Used to represent an Awakening recipe for the recipe type
 */
public interface IAwakeningRecipe extends Recipe<CraftingInput> {
    Ingredient getAltarIngredient();
    List<Ingredient> getPedestalIngredients();
    List<IngredientWithCount> getEssenceIngredients();

    /**
     * Gets a map of missing essences and the amount of each that is missing
     *
     * @param items the list essence vessel items
     * @return a map of missing essences -> amount missing
     */
    Map<IngredientWithCount, Integer> getMissingEssences(List<ItemStack> items);

    /**
     * Checks if the given list of items has the required essences and stack sizes for this recipe
     *
     * @param items the list essence vessel items
     * @return all required essences are provided
     */
    default boolean hasRequiredEssences(List<ItemStack> items) {
        return this.getMissingEssences(items).isEmpty();
    }

    /**
     * Returns the remaining items after a successful crafting operation
     *
     * @param input the crafting input
     * @return the remaining items
     */
    NonNullList<ItemStack> getRemainingItems(CraftingInput input);

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default String group() {
        return "mysticalagriculture:awakening";
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }
}
