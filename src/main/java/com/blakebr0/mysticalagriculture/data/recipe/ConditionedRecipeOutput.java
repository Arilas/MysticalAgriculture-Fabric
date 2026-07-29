package com.blakebr0.mysticalagriculture.data.recipe;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public interface ConditionedRecipeOutput extends RecipeOutput {
    void accept(
            ResourceKey<Recipe<?>> id,
            Recipe<?> recipe,
            List<ResourceCondition> conditions
    );

    @Override
    default void accept(
            ResourceKey<Recipe<?>> id,
            Recipe<?> recipe,
            AdvancementHolder advancement
    ) {
        this.accept(id, recipe, List.of());
    }
}
