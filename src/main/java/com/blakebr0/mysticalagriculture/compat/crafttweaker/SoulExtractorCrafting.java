//package com.blakebr0.mysticalagriculture.compat.crafttweaker;
//
//import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
//import com.blakebr0.mysticalagriculture.crafting.recipe.SoulExtractionRecipe;
//import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
//import com.blamejared.crafttweaker.api.CraftTweakerAPI;
//import com.blamejared.crafttweaker.api.CraftTweakerConstants;
//import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
//import com.blamejared.crafttweaker.api.action.recipe.ActionRemoveRecipe;
//import com.blamejared.crafttweaker.api.annotation.ZenRegister;
//import com.blamejared.crafttweaker.api.ingredient.IIngredient;
//import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
//import net.minecraft.resources.Identifier;
//import net.minecraft.world.item.crafting.RecipeHolder;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.openzen.zencode.java.ZenCodeType;
//
//import java.util.Objects;
//
//@ZenCodeType.Name("mods.mysticalagriculture.SoulExtractorCrafting")
//@ZenRegister
//public final class SoulExtractorCrafting implements IRecipeManager<ISoulExtractionRecipe> {
//    @Override
//    public RecipeType<ISoulExtractionRecipe> getRecipeType() {
//        return ModRecipeTypes.SOUL_EXTRACTION;
//    }
//
//    @ZenCodeType.Method
//    public void addRecipe(String name, IIngredient input, String type, double souls) {
//        var id = CraftTweakerConstants.rl(this.fixRecipeName(name));
//        var result = new SoulExtractionRecipe.Result(Identifier.parse(type), souls);
//        var recipe = new SoulExtractionRecipe(input.asVanillaIngredient(), result);
//
//        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
//    }
//
//    @ZenCodeType.Method
//    public void removeByMobSoulType(String type) {
//        CraftTweakerAPI.apply(new ActionRemoveRecipe<>(this, recipe -> {
//            var mobSoulType = recipe.value().getMobSoulType();
//            return mobSoulType != null && Objects.equals(mobSoulType.getId(), Identifier.parse(type));
//        }));
//    }
//}
