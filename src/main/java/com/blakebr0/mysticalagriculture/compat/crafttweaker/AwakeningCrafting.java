//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.cucumber.helper.ParsingHelper;
//import com.blakebr0.mysticalagriculture.MysticalAgriculture;
//import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
//import com.blakebr0.mysticalagriculture.crafting.EssenceVesselColorManager;
//import com.blakebr0.mysticalagriculture.crafting.recipe.AwakeningRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.base.IRuntimeAction;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.ingredient.IIngredient;
//import com.blamejared.crafttweaker.api.item.IItemStack;
//import com.blamejared.crafttweaker.api.item.MCItemStack;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//@ZenCodeType.Name("mods.mysticalagriculture.AwakeningCrafting")
//@ZenRegister
//public final class AwakeningCrafting implements IRecipeManager<IAwakeningRecipe> {
//    @Override
//    public RecipeType<IAwakeningRecipe> getRecipeType() {
//        return ModRecipeTypes.AWAKENING;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, IItemStack output, IIngredient input, IIngredient[] inputs, IItemStack[] essences, @ZenCodeType.OptionalBoolean boolean transferComponents) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var recipe = new AwakeningRecipe(input.asVanillaIngredient(), toIngredientsList(inputs), toItemStackList(essences), output.getInternal(), transferComponents);
//
//        recipe.setTransformer((slot, stack) -> slot == 0
//                ? input.getRemainingItem(new MCItemStack(stack)).getInternal()
//                : inputs[slot - 1].getRemainingItem(new MCItemStack(stack)).getInternal());
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//
//    @ZenCodeType.Method
//    public void setEssenceVesselColor(IItemStack stack, String color) {
//        CraftTweakerAPI.apply(new SetEssenceVesselColorAction(stack, color));
//    }
//
//    private static NonNullList<Ingredient> toIngredientsList(IIngredient... iingredients) {
//        var ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
//
//        for (int i = 0; i < iingredients.length; i++) {
//            ingredients.set(i, iingredients[i].asVanillaIngredient());
//        }
//
//        return ingredients;
//    }
//
//    private static NonNullList<ItemStack> toItemStackList(IItemStack... iitemStacks) {
//        var itemStacks = NonNullList.withSize(4, ItemStack.EMPTY);
//
//        for (int i = 0; i < iitemStacks.length; i++) {
//            itemStacks.set(i, iitemStacks[i].getInternal());
//        }
//
//        return itemStacks;
//    }
//
//    record SetEssenceVesselColorAction(IItemStack stack, String color) implements IRuntimeAction {
//        @Override
//        public void apply() {
//            EssenceVesselColorManager.INSTANCE.addColor(stack.getInternal(), ParsingHelper.parseHex(color, color));
//        }
//
//        @Override
//        public String describe() {
//            return "Setting the Essence Vessel color for the item %s to %s.".formatted(stack.getDisplayName().getString(), color);
//        }
//
//        @Override
//        public String systemName() {
//            return MysticalAgriculture.NAME;
//        }
//    }
//}
