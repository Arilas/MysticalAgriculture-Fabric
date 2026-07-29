package com.blakebr0.mysticalagriculture.data.recipe;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.crafting.condition.CropEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
import net.minecraft.advancements.triggers.Criterion;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ReprocessorRecipeBuilder implements RecipeBuilder {
    private final Identifier id;
    private final Ingredient input;
    private final ItemStackTemplate result;
    private final List<ResourceCondition> conditions;

    public ReprocessorRecipeBuilder(Identifier id, Ingredient input, ItemStackTemplate result) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.conditions = new ArrayList<>();
    }

    public void addCondition(ResourceCondition condition) {
        this.conditions.add(condition);
    }

    public static ReprocessorRecipeBuilder seed(Identifier id, Crop crop) {
        var input = Ingredient.of(crop.getSeedsItem());
        var result = crop.getEssenceItem();

        var builder = new ReprocessorRecipeBuilder(id, input, new ItemStackTemplate(result, 2));

        builder.addCondition(new CropEnabledCondition(crop.getId()));

        return builder;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return ResourceKey.create(Registries.RECIPE, this.id);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        ((ConditionedRecipeOutput) output).accept(
                id,
                new ReprocessorRecipe(this.input, this.result),
                List.copyOf(this.conditions)
        );
    }
}
