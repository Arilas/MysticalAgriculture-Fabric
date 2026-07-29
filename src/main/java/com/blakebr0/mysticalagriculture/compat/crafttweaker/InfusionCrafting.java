//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
//import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.ingredient.IIngredient;
//import com.blamejared.crafttweaker.api.item.IItemStack;
//import com.blamejared.crafttweaker.api.item.MCItemStack;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//@ZenCodeType.Name("mods.mysticalagriculture.InfusionCrafting")
//@ZenRegister
//public final class InfusionCrafting implements IRecipeManager<IInfusionRecipe> {
//    @Override
//    public RecipeType<IInfusionRecipe> getRecipeType() {
//        return ModRecipeTypes.INFUSION;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, IItemStack output, IIngredient input, IIngredient[] inputs, @ZenCodeType.OptionalBoolean boolean transferNBT) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var recipe = new InfusionRecipe(input.asVanillaIngredient(), toIngredientsList(inputs), output.getInternal(), transferNBT);
//
//        recipe.setTransformer((slot, stack) -> slot == 0
//                ? input.getRemainingItem(new MCItemStack(stack)).getInternal()
//                : inputs[slot - 1].getRemainingItem(new MCItemStack(stack)).getInternal());
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//
//    private static NonNullList<Ingredient> toIngredientsList(IIngredient... iingredients) {
//        var ingredients = NonNullList.<Ingredient>create();
//
//        for (var iingredient : iingredients) {
//            ingredients.add(iingredient.asVanillaIngredient());
//        }
//
//        return ingredients;
//    }
//}
