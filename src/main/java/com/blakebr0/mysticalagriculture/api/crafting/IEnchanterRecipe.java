package com.blakebr0.mysticalagriculture.api.crafting;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Used to represent an Enchanter recipe for the recipe type
 */
public interface IEnchanterRecipe extends Recipe<CraftingInput> {
    List<IngredientWithCount> getIngredients();
    Holder<Enchantment> getEnchantment();

    /**
     * Get the maximum enchantment level for the provided recipe input
     *
     * @param input the recipe input
     * @return the resulting enchantment level
     */
    int getMaxResultEnchantmentLevel(RecipeInput input);

    /**
     * Special case version of {@link Recipe#assemble(RecipeInput)} that will only return the
     * newly enchanted item with the provided level or empty
     *
     * @param input the recipe input
     * @param level the required enchantment level
     * @return the enchanted item or empty
     */
    ItemStack assemble(CraftingInput input, int level);

    default NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return this.getRemainingItems(input, this.getMaxResultEnchantmentLevel(input));
    }

    /**
     * Returns the remaining items after a successful crafting operation
     *
     * @param input the crafting input
     * @return the remaining items
     */
    NonNullList<ItemStack> getRemainingItems(CraftingInput input, int level);

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default String group() {
        return "mysticalagriculture:enchanter";
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
