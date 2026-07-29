//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
//import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.ingredient.IIngredient;
//import com.blamejared.crafttweaker.api.item.IItemStack;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//@ZenCodeType.Name("mods.mysticalagriculture.ReprocessorCrafting")
//@ZenRegister
//public final class ReprocessorCrafting implements IRecipeManager<IReprocessorRecipe> {
//    @Override
//    public RecipeType<IReprocessorRecipe> getRecipeType() {
//        return ModRecipeTypes.REPROCESSOR;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, IItemStack output, IIngredient input) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var recipe = new ReprocessorRecipe(input.asVanillaIngredient(), output.getInternal());
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//}
