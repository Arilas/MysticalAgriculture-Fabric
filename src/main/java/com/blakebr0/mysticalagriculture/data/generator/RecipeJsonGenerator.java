package com.blakebr0.mysticalagriculture.data.generator;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.data.recipe.ConditionedRecipeOutput;
import com.blakebr0.mysticalagriculture.data.recipe.ReprocessorRecipeBuilder;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class RecipeJsonGenerator implements DataProvider {
    private final PackOutput.PathProvider recipes;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public RecipeJsonGenerator(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(lookup -> {
            var recipes = new LinkedHashMap<ResourceKey<Recipe<?>>, GeneratedRecipe>();
            var recipeOutput = new CollectingRecipeOutput(recipes);

            for (var crop : CropRegistry.getInstance().getCrops()) {
                var id = MysticalAgricultureAPI.resource("seed/reprocessor/" + crop.getName());
                ReprocessorRecipeBuilder.seed(id, crop).save(recipeOutput);
            }

            var ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
            var writes = new ArrayList<CompletableFuture<?>>(recipes.size());
            for (var entry : recipes.entrySet()) {
                var json = Recipe.CODEC.encodeStart(ops, entry.getValue().recipe())
                        .getOrThrow()
                        .getAsJsonObject();
                if (!entry.getValue().conditions().isEmpty()) {
                    json.add(
                            ResourceConditions.CONDITIONS_KEY,
                            ResourceCondition.LIST_CODEC.encodeStart(
                                    ops,
                                    entry.getValue().conditions()
                            ).getOrThrow()
                    );
                }
                writes.add(DataProvider.saveStable(
                        output,
                        json,
                        this.recipes.json(entry.getKey())
                ));
            }

            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Mystical Agriculture recipe generator";
    }

    private record GeneratedRecipe(
            Recipe<?> recipe,
            List<ResourceCondition> conditions
    ) {
    }

    private static final class CollectingRecipeOutput implements ConditionedRecipeOutput {
        private final Map<ResourceKey<Recipe<?>>, GeneratedRecipe> recipes;

        private CollectingRecipeOutput(
                Map<ResourceKey<Recipe<?>>, GeneratedRecipe> recipes
        ) {
            this.recipes = recipes;
        }

        @Override
        public void accept(
                ResourceKey<Recipe<?>> id,
                Recipe<?> recipe,
                List<ResourceCondition> conditions
        ) {
            var previous = this.recipes.put(
                    id,
                    new GeneratedRecipe(recipe, List.copyOf(conditions))
            );
            if (previous != null) {
                throw new IllegalStateException("Duplicate generated recipe " + id.identifier());
            }
        }

        @Override
        public Advancement.Builder advancement() {
            return Advancement.Builder.advancement();
        }

        @Override
        public void includeRootAdvancement() {
        }
    }
}
