//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.cucumber.crafting.ingredient.IngredientWithCount;
//import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
//import com.blakebr0.mysticalagriculture.crafting.recipe.EnchanterRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.item.IItemStack;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import com.blamejared.crafttweaker.platform.services.IRegistryHelper;
//import net.minecraft.core.NonNullList;
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//import java.util.Objects;
//
//@ZenCodeType.Name("mods.mysticalagriculture.EnchanterCrafting")
//@ZenRegister
//public final class EnchanterCrafting implements IRecipeManager<IEnchanterRecipe>, IRegistryHelper {
//    @Override
//    public RecipeType<IEnchanterRecipe> getRecipeType() {
//        return ModRecipeTypes.ENCHANTER;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, String enchantmentID, IItemStack[] inputs) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var enchantment = this.registryOrThrow(Registries.ENCHANTMENT)
//                .getHolder(Identifier.parse(enchantmentID))
//                .orElseThrow(() -> new RuntimeException("Could not find enchantment " + enchantmentID));
//
//        var recipe = new EnchanterRecipe(toIngredientsList(inputs), enchantment);
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//
//    @ZenCodeType.Method
//    public void removeByEnchantment(String enchantmentID) {
//        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(this, recipe -> {
//            var enchantment = recipe.value().getEnchantment().getKey();
//            return enchantment != null && Objects.equals(enchantment.location(), Identifier.parse(enchantmentID));
//        }));
//    }
//
//    private static NonNullList<IngredientWithCount> toIngredientsList(IItemStack... istacks) {
//        var ingredients = NonNullList.withSize(2, IngredientWithCount.EMPTY);
//
//        for (int i = 0; i < istacks.length; i++) {
//            var stack = istacks[i];
//
//            ingredients.set(i, new IngredientWithCount(new Ingredient.ItemValue(stack.getInternal()), stack.amount()));
//        }
//
//        return ingredients;
//    }
//}
