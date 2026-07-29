package com.blakebr0.mysticalagriculture.util;

import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeIngredientCacheFailureTest {
    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
    }

    @Test
    void failingRecipeIdentifiesItsIdAndLeavesThePreviousSnapshotUntouched() {
        var cache = RecipeIngredientCache.INSTANCE;
        var oldCaches = Map.<RecipeType<?>, Map<net.minecraft.world.item.Item, List<Ingredient>>>of(
                RecipeType.SMELTING,
                Map.of(Items.DIAMOND, List.of(Ingredient.of(Items.STONE)))
        );
        var oldVesselItems = Set.of(Items.GOLD_INGOT);
        cache.setState(oldCaches, oldVesselItems);

        var recipeId = ResourceKey.create(
                Registries.RECIPE,
                Identifier.parse("mysticalagriculture:broken_cache_recipe")
        );
        var recipes = RecipeMap.create(List.of(new RecipeHolder<>(recipeId, new FailingRecipe())));

        var failure = assertThrows(
                RecipeIngredientCache.RecipeCacheBuildException.class,
                () -> RecipeIngredientCache.buildState(recipes)
        );
        assertEquals(recipeId, failure.recipeId());

        assertFalse(cache.rebuild(recipes));
        assertEquals(oldCaches, cache.copyCaches());
        assertEquals(oldVesselItems, cache.copyValidVesselItems());
    }

    private static final class FailingRecipe implements IReprocessorRecipe {
        @Override
        public Ingredient getIngredient() {
            throw new IllegalStateException("deliberate broken ingredient");
        }

        @Override
        public boolean matches(CraftingInput input, Level level) {
            return false;
        }

        @Override
        public ItemStack assemble(CraftingInput input) {
            return ItemStack.EMPTY;
        }

        @Override
        public RecipeSerializer<? extends Recipe<CraftingInput>> getSerializer() {
            return ReprocessorRecipe.SERIALIZER;
        }

        @Override
        public RecipeType<IReprocessorRecipe> getType() {
            return ModRecipeTypes.REPROCESSOR;
        }
    }
}
