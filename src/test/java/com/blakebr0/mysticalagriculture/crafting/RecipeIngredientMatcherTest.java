package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import com.blakebr0.mysticalagriculture.crafting.recipe.EnchanterRecipe;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeIngredientMatcherTest {
    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
    }

    @Test
    void matcherBacktracksAcrossAmbiguousIngredientAssignments() {
        var stacks = List.of(new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD));
        var ingredients = List.of(
                Ingredient.of(Items.DIAMOND, Items.EMERALD),
                Ingredient.of(Items.DIAMOND)
        );

        assertTrue(RecipeIngredientMatcher.matchesIngredients(stacks, ingredients));
    }

    @Test
    void matchingAndMultiplierQueriesDoNotMutateInputStacksOrComponents() {
        var namedDiamond = new ItemStack(Items.DIAMOND, 9);
        namedDiamond.set(DataComponents.CUSTOM_NAME, Component.literal("untouched"));
        var emerald = new ItemStack(Items.EMERALD, 8);
        var stacks = List.of(namedDiamond, emerald);
        var snapshots = stacks.stream().map(ItemStack::copy).toList();
        var ingredients = List.of(
                new IngredientWithCount(Ingredient.of(Items.DIAMOND, Items.EMERALD), 2),
                new IngredientWithCount(Ingredient.of(Items.DIAMOND), 3)
        );

        assertTrue(RecipeIngredientMatcher.matches(stacks, ingredients));
        assertEquals(3, RecipeIngredientMatcher.maxMultiplier(stacks, ingredients, 4));
        for (var index = 0; index < stacks.size(); index++) {
            assertTrue(ItemStack.matches(snapshots.get(index), stacks.get(index)));
        }
    }

    @Test
    void enchanterUsesOptimalAssignmentForItsMaximumMultiplier() {
        var protection = Task3TestRegistries.lookup()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PROTECTION);
        var recipe = new EnchanterRecipe(List.of(
                new IngredientWithCount(Ingredient.of(Items.DIAMOND, Items.EMERALD), 2),
                new IngredientWithCount(Ingredient.of(Items.DIAMOND), 3)
        ), protection);
        var diamond = new ItemStack(Items.DIAMOND, 9);
        var emerald = new ItemStack(Items.EMERALD, 8);
        var book = new ItemStack(Items.BOOK);
        var input = CraftingInput.of(3, 1, List.of(diamond, emerald, book));

        assertEquals(3, recipe.getMaxResultEnchantmentLevel(input));
        assertEquals(9, diamond.getCount());
        assertEquals(8, emerald.getCount());
    }

    @Test
    void enchanterConsumesTheSameUnorderedAssignmentUsedForItsMultiplier() {
        var protection = Task3TestRegistries.lookup()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.PROTECTION);
        var recipe = new EnchanterRecipe(List.of(
                new IngredientWithCount(Ingredient.of(Items.DIAMOND, Items.EMERALD), 2),
                new IngredientWithCount(Ingredient.of(Items.DIAMOND), 3)
        ), protection);
        var input = CraftingInput.of(3, 1, List.of(
                new ItemStack(Items.DIAMOND, 9),
                new ItemStack(Items.EMERALD, 8),
                new ItemStack(Items.BOOK)
        ));

        var remaining = recipe.getRemainingItems(input, 3);

        assertTrue(remaining.get(0).isEmpty(), "the diamond-specific input must consume all nine diamonds");
        assertEquals(2, remaining.get(1).getCount(), "the broad input must consume six emeralds");
    }
}
