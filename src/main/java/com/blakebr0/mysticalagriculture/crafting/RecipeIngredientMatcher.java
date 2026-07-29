package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

public final class RecipeIngredientMatcher {
    public static boolean matchesIngredients(List<ItemStack> stacks, List<Ingredient> ingredients) {
        return matches(
                stacks,
                ingredients.stream()
                        .map(ingredient -> new IngredientWithCount(ingredient, 1))
                        .toList()
        );
    }

    public static boolean matches(List<ItemStack> stacks, List<IngredientWithCount> ingredients) {
        if (stacks.size() != ingredients.size()) {
            return false;
        }

        return matchNext(stacks, ingredients, new boolean[stacks.size()], 0);
    }

    public static int maxMultiplier(List<ItemStack> stacks, List<IngredientWithCount> ingredients, int limit) {
        if (stacks.size() != ingredients.size()) {
            return 0;
        }

        return maxMultiplier(stacks, ingredients, new boolean[stacks.size()], 0, limit);
    }

    public static int[] findAssignment(
            List<ItemStack> stacks,
            List<IngredientWithCount> ingredients,
            int multiplier
    ) {
        if (stacks.size() != ingredients.size() || multiplier < 1) {
            return null;
        }

        var assignment = new int[ingredients.size()];
        Arrays.fill(assignment, -1);
        return assignNext(stacks, ingredients, new boolean[stacks.size()], assignment, 0, multiplier)
                ? assignment
                : null;
    }

    private static boolean matchNext(
            List<ItemStack> stacks,
            List<IngredientWithCount> ingredients,
            boolean[] used,
            int ingredientIndex
    ) {
        if (ingredientIndex == ingredients.size()) {
            return true;
        }

        var ingredient = ingredients.get(ingredientIndex);
        for (var stackIndex = 0; stackIndex < stacks.size(); stackIndex++) {
            if (!used[stackIndex] && ingredient.test(stacks.get(stackIndex))) {
                used[stackIndex] = true;
                if (matchNext(stacks, ingredients, used, ingredientIndex + 1)) {
                    return true;
                }
                used[stackIndex] = false;
            }
        }
        return false;
    }

    private static int maxMultiplier(
            List<ItemStack> stacks,
            List<IngredientWithCount> ingredients,
            boolean[] used,
            int ingredientIndex,
            int currentMaximum
    ) {
        if (ingredientIndex == ingredients.size()) {
            return currentMaximum;
        }

        var ingredient = ingredients.get(ingredientIndex);
        var best = 0;
        for (var stackIndex = 0; stackIndex < stacks.size(); stackIndex++) {
            var stack = stacks.get(stackIndex);
            if (!used[stackIndex] && ingredient.ingredient().test(stack)) {
                var multiplier = Math.min(currentMaximum, stack.getCount() / ingredient.count());
                if (multiplier > 0) {
                    used[stackIndex] = true;
                    best = Math.max(
                            best,
                            maxMultiplier(stacks, ingredients, used, ingredientIndex + 1, multiplier)
                    );
                    used[stackIndex] = false;
                }
            }
        }
        return best;
    }

    private static boolean assignNext(
            List<ItemStack> stacks,
            List<IngredientWithCount> ingredients,
            boolean[] used,
            int[] assignment,
            int ingredientIndex,
            int multiplier
    ) {
        if (ingredientIndex == ingredients.size()) {
            return true;
        }

        var ingredient = ingredients.get(ingredientIndex);
        for (var stackIndex = 0; stackIndex < stacks.size(); stackIndex++) {
            var stack = stacks.get(stackIndex);
            if (!used[stackIndex]
                    && ingredient.ingredient().test(stack)
                    && stack.getCount() >= ingredient.count() * multiplier) {
                used[stackIndex] = true;
                assignment[ingredientIndex] = stackIndex;
                if (assignNext(stacks, ingredients, used, assignment, ingredientIndex + 1, multiplier)) {
                    return true;
                }
                assignment[ingredientIndex] = -1;
                used[stackIndex] = false;
            }
        }
        return false;
    }

    private RecipeIngredientMatcher() {
    }
}
