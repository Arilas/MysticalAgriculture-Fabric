//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.cucumber.crafting.ingredient.IngredientWithCount;
//import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
//import com.blakebr0.mysticalagriculture.crafting.recipe.SouliumSpawnerRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.ingredient.IIngredient;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.resources.Identifier;
//import net.minecraft.util.random.WeightedEntry;
//import net.minecraft.util.random.WeightedRandomList;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@ZenCodeType.Name("mods.mysticalagriculture.SouliumSpawnerCrafting")
//@ZenRegister
//public final class SouliumSpawnerCrafting implements IRecipeManager<ISouliumSpawnerRecipe> {
//    @Override
//    public RecipeType<ISouliumSpawnerRecipe> getRecipeType() {
//        return ModRecipeTypes.SOULIUM_SPAWNER;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, IIngredient input, int inputCount, String[] entities) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var ingredient = new IngredientWithCount(new Ingredient.ItemValue(input.getItems()[0].getInternal()), inputCount);
//        var recipe = new SouliumSpawnerRecipe(ingredient, toEntityTypeList(entities));
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//
//    @ZenCodeType.Method
//    public void removeByEntity(String entity) {
//        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(this, recipe -> recipe.value().getEntityTypes().unwrap()
//                .stream()
//                .anyMatch(e -> e.data().toString().equals(entity)))
//        );
//    }
//
//    private static WeightedRandomList<WeightedEntry.Wrapper<EntityType<?>>> toEntityTypeList(String[] entities) {
//        List<WeightedEntry.Wrapper<EntityType<?>>> entityTypes = new ArrayList<>();
//
//        for (var entity : entities) {
//            var entityIDParts = entity.split("@");
//            var entityTypeID = Identifier.parse(entityIDParts[0]);
//            var entityType = BuiltInRegistries.ENTITY_TYPE.get(entityTypeID);
//            var weight = 1;
//
//            if (entityIDParts.length > 1) {
//                weight = Integer.parseInt(entityIDParts[1]);
//            }
//
//            entityTypes.add(WeightedEntry.wrap(entityType, weight));
//        }
//
//        return WeightedRandomList.create(entityTypes);
//    }
//}
