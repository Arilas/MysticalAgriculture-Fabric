package com.blakebr0.mysticalagriculture.util;

import com.blakebr0.cucumber.event.RecipeManagerLoadedEvent;
import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.google.common.base.Stopwatch;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.LoggerFactory;

public class RecipeIngredientCache {
    public static final RecipeIngredientCache INSTANCE = new RecipeIngredientCache();
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger("Mystical Agriculture");

    private volatile State state;

    private RecipeIngredientCache() {
        this.state = new State(Map.of(), Set.of());
    }

    public static void register() {
        RecipeManagerLoadedEvent.EVENT.register(INSTANCE::onRecipeManagerLoaded);
    }

    public void onRecipeManagerLoaded(RecipeManagerLoadedEvent event) {
        var stopwatch = Stopwatch.createStarted();
        var manager = event.getRecipeManager();
        var recipes = RecipeMap.create(manager.getRecipes());

        if (!this.rebuild(recipes)) {
            return;
        }

        EssenceVesselColorManager.INSTANCE.finishReload();
        LOGGER.info("Recipe ingredient caching done in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    boolean rebuild(RecipeMap recipes) {
        try {
            this.state = buildState(recipes);
            return true;
        } catch (RecipeCacheBuildException exception) {
            LOGGER.error(
                    "Could not rebuild the recipe ingredient cache because recipe {} failed",
                    exception.recipeId().identifier(),
                    exception.getCause()
            );
            return false;
        } catch (RuntimeException exception) {
            LOGGER.error("Could not rebuild the recipe ingredient cache before the atomic state swap", exception);
            return false;
        }
    }

    static State buildState(RecipeMap recipes) {
        var nextCaches = new HashMap<RecipeType<?>, Map<Item, List<Ingredient>>>();
        var nextValidVesselItems = new HashSet<Item>();

        cache(nextCaches, recipes, RecipeType.SMELTING, recipe -> List.of(recipe.input()));
        cache(nextCaches, recipes, ModRecipeTypes.REPROCESSOR, recipe -> List.of(recipe.getIngredient()));
        cache(nextCaches, recipes, ModRecipeTypes.SOUL_EXTRACTION, recipe -> List.of(recipe.getIngredient()));
        cache(nextCaches, recipes, ModRecipeTypes.SOULIUM_SPAWNER, recipe -> List.of(recipe.getIngredient().ingredient()));
        cache(nextCaches, recipes, ModRecipeTypes.ORE_INFUSION, recipe -> recipe.getIngredients().stream()
                .map(com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount::ingredient)
                .toList());

        cacheVesselItems(nextValidVesselItems, recipes);
        return new State(immutableCaches(nextCaches), Set.copyOf(nextValidVesselItems));
    }

    public void setState(
            Map<RecipeType<?>, Map<Item, List<Ingredient>>> caches,
            Set<Item> validVesselItems
    ) {
        this.state = new State(immutableCaches(caches), Set.copyOf(validVesselItems));
    }

    public Map<RecipeType<?>, Map<Item, List<Ingredient>>> copyCaches() {
        return this.state.caches;
    }

    public Set<Item> copyValidVesselItems() {
        return this.state.validVesselItems;
    }

    public boolean isValidInput(ItemStack stack, RecipeType<?> type) {
        var cache = this.state.caches.getOrDefault(type, Collections.emptyMap()).get(stack.getItem());
        return cache != null && cache.stream().anyMatch(i -> i.test(stack));
    }

    // soulium spawner ingredients are count-dependent, and we don't care in this case
    public boolean isValidSouliumSpawnerInput(ItemStack stack) {
        return isValidInput(stack.copyWithCount(Integer.MAX_VALUE), ModRecipeTypes.SOULIUM_SPAWNER);
    }

    public boolean isValidVesselItem(ItemStack stack) {
        return this.state.validVesselItems.contains(stack.getItem());
    }

    private static <C extends RecipeInput, T extends @NonNull Recipe<C>> void cache(
            Map<RecipeType<?>, Map<Item, List<Ingredient>>> caches,
            RecipeMap recipes,
            RecipeType<T> type,
            Function<T, List<Ingredient>> ingredients
    ) {
        caches.put(type, new HashMap<>());

        for (var holder : recipes.byType(type)) {
            try {
                for (var ingredient : ingredients.apply(holder.value())) {
                    var items = new HashSet<>();
                    for (var stack : ingredient.items().toList()) {
                        var item = stack.value();
                        if (items.contains(item))
                            continue;

                        var cache = caches.get(type).computeIfAbsent(item, _ -> new ArrayList<>());

                        items.add(item);
                        cache.add(ingredient);
                    }
                }
            } catch (RuntimeException exception) {
                throw failure(holder, exception);
            }
        }
    }

    private static void cacheVesselItems(Set<Item> validVesselItems, RecipeMap recipes) {
        for (var holder : recipes.byType(ModRecipeTypes.AWAKENING)) {
            try {
                var recipe = holder.value();
                for (var essence : recipe.getEssenceIngredients()) {
                    validVesselItems.addAll(
                            essence.ingredient().items().map(Holder::value).toList()
                    );
                }
            } catch (RuntimeException exception) {
                throw failure(holder, exception);
            }
        }
    }

    private static RecipeCacheBuildException failure(
            RecipeHolder<?> holder,
            RuntimeException exception
    ) {
        if (exception instanceof RecipeCacheBuildException cacheBuildException) {
            return cacheBuildException;
        }
        return new RecipeCacheBuildException(holder.id(), exception);
    }

    private static Map<RecipeType<?>, Map<Item, List<Ingredient>>> immutableCaches(
            Map<RecipeType<?>, Map<Item, List<Ingredient>>> caches
    ) {
        var copy = new HashMap<RecipeType<?>, Map<Item, List<Ingredient>>>();
        caches.forEach((type, cache) -> {
            var itemCopy = new HashMap<Item, List<Ingredient>>();
            cache.forEach((item, ingredients) -> itemCopy.put(item, List.copyOf(ingredients)));
            copy.put(type, Map.copyOf(itemCopy));
        });
        return Map.copyOf(copy);
    }

    record State(
            Map<RecipeType<?>, Map<Item, List<Ingredient>>> caches,
            Set<Item> validVesselItems
    ) {
    }

    static final class RecipeCacheBuildException extends RuntimeException {
        private final ResourceKey<Recipe<?>> recipeId;

        private RecipeCacheBuildException(
                ResourceKey<Recipe<?>> recipeId,
                RuntimeException cause
        ) {
            super("Could not cache recipe " + recipeId.identifier(), cause);
            this.recipeId = recipeId;
        }

        ResourceKey<Recipe<?>> recipeId() {
            return this.recipeId;
        }
    }
}
