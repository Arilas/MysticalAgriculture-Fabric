package com.blakebr0.mysticalagriculture.data.recipe;

import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.crafting.condition.CropEnabledCondition;
import com.blakebr0.mysticalagriculture.crafting.condition.CropHasMaterialCondition;
import com.blakebr0.mysticalagriculture.crafting.ingredient.CropComponentIngredient;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import net.minecraft.advancements.triggers.Criterion;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InfusionRecipeBuilder implements RecipeBuilder {
    private final Identifier id;
    private final List<Ingredient> inputs;
    private final ItemStackTemplate result;
    private final List<ResourceCondition> conditions;
    private Ingredient input;

    public InfusionRecipeBuilder(Identifier id, ItemStackTemplate result) {
        this.id = id;
        this.inputs = new ArrayList<>(8);
        this.result = result;
        this.conditions = new ArrayList<>();
    }

    public void addIngredient(Ingredient ingredient) {
        this.inputs.add(ingredient);
    }

    public void addCondition(ResourceCondition condition) {
        this.conditions.add(condition);
    }

    public static InfusionRecipeBuilder seed(Identifier id, Crop crop) {
        var builder = new InfusionRecipeBuilder(id, new ItemStackTemplate(crop.getSeedsItem()));

        var essence = CropComponentIngredient.of(crop.getId(), CropComponentIngredient.ComponentType.ESSENCE);
        var seed = CropComponentIngredient.of(crop.getId(), CropComponentIngredient.ComponentType.SEED);
        var material = CropComponentIngredient.of(crop.getId(), CropComponentIngredient.ComponentType.MATERIAL);

        builder.input = seed;

        builder.addIngredient(material);
        builder.addIngredient(essence);
        builder.addIngredient(material);
        builder.addIngredient(essence);
        builder.addIngredient(material);
        builder.addIngredient(essence);
        builder.addIngredient(material);
        builder.addIngredient(essence);

        builder.addCondition(new CropEnabledCondition(crop.getId()));
        builder.addCondition(new CropHasMaterialCondition(crop.getId()));

        var ingredient = crop.getLazyIngredient();

        if (ingredient.isTag()) {
            var tag = TagKey.create(Registries.ITEM, Identifier.parse(ingredient.getId()));
            builder.addCondition(ResourceConditions.not(ResourceConditions.tagsPopulated(tag)));
        }

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
        ((ConditionedRecipeOutput) output).accept(id, new InfusionRecipe(
                this.input,
                this.inputs,
                this.result,
                false
        ), List.copyOf(this.conditions));
    }
}
