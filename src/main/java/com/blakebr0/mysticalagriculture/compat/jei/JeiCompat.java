package com.blakebr0.mysticalagriculture.compat.jei;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.client.handler.ClientRecipeHandler;
import com.blakebr0.mysticalagriculture.client.screen.EnchanterScreen;
import com.blakebr0.mysticalagriculture.client.screen.EssenceFurnaceScreen;
import com.blakebr0.mysticalagriculture.client.screen.OreInfuserScreen;
import com.blakebr0.mysticalagriculture.client.screen.ReprocessorScreen;
import com.blakebr0.mysticalagriculture.client.screen.SoulExtractorScreen;
import com.blakebr0.mysticalagriculture.client.screen.SouliumSpawnerScreen;
import com.blakebr0.mysticalagriculture.compat.jei.category.AwakeningCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.CruxCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.EnchanterCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.InfusionCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.OreInfuserCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.ReprocessorCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.SoulExtractorCategory;
import com.blakebr0.mysticalagriculture.compat.jei.category.SouliumSpawnerCategory;
import com.blakebr0.mysticalagriculture.compat.jei.recipe.CruxRecipe;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class JeiCompat implements IModPlugin {
    public static final Identifier UID = MysticalAgriculture.resource("jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(
                new InfusionCategory(guiHelper),
                new AwakeningCategory(guiHelper),
                new EnchanterCategory(guiHelper),
                new ReprocessorCategory(guiHelper),
                new SoulExtractorCategory(guiHelper),
                new SouliumSpawnerCategory(guiHelper),
                new OreInfuserCategory(guiHelper),
                new CruxCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(InfusionCategory.RECIPE_TYPE, new ItemStack(ModBlocks.INFUSION_ALTAR));
        registration.addCraftingStation(InfusionCategory.RECIPE_TYPE, new ItemStack(ModBlocks.INFUSION_PEDESTAL));
        registration.addCraftingStation(AwakeningCategory.RECIPE_TYPE, new ItemStack(ModBlocks.AWAKENING_ALTAR));
        registration.addCraftingStation(AwakeningCategory.RECIPE_TYPE, new ItemStack(ModBlocks.AWAKENING_PEDESTAL));
        registration.addCraftingStation(AwakeningCategory.RECIPE_TYPE, new ItemStack(ModBlocks.ESSENCE_VESSEL));
        registration.addCraftingStation(EnchanterCategory.RECIPE_TYPE, new ItemStack(ModBlocks.ENCHANTER));
        registration.addCraftingStation(mezz.jei.api.constants.RecipeTypes.SMELTING, new ItemStack(ModBlocks.FURNACE));
        registration.addCraftingStation(ReprocessorCategory.RECIPE_TYPE, new ItemStack(ModBlocks.REPROCESSOR));
        registration.addCraftingStation(SoulExtractorCategory.RECIPE_TYPE, new ItemStack(ModBlocks.SOUL_EXTRACTOR));
        registration.addCraftingStation(SouliumSpawnerCategory.RECIPE_TYPE, new ItemStack(ModBlocks.SOULIUM_SPAWNER));
        registration.addCraftingStation(OreInfuserCategory.RECIPE_TYPE, new ItemStack(ModBlocks.ORE_INFUSER));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(InfusionCategory.RECIPE_TYPE, ClientRecipeHandler.INFUSION_RECIPES);
        registration.addRecipes(AwakeningCategory.RECIPE_TYPE, ClientRecipeHandler.AWAKENING_RECIPES);
        registration.addRecipes(EnchanterCategory.RECIPE_TYPE, ClientRecipeHandler.ENCHANTER_RECIPES);
        registration.addRecipes(ReprocessorCategory.RECIPE_TYPE, ClientRecipeHandler.REPROCESSOR_RECIPES);
        registration.addRecipes(SoulExtractorCategory.RECIPE_TYPE, ClientRecipeHandler.SOUL_EXTRACTION_RECIPES);
        registration.addRecipes(SouliumSpawnerCategory.RECIPE_TYPE, ClientRecipeHandler.SOULIUM_SPAWNER_RECIPES);
        registration.addRecipes(OreInfuserCategory.RECIPE_TYPE, ClientRecipeHandler.ORE_INFUSION_RECIPES);

        registration.addRecipes(CruxCategory.RECIPE_TYPE, CruxRecipe.createAll());

        registration.addIngredientInfo(
                new ItemStack(ModItems.COGNIZANT_DUST),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.desc.mysticalagriculture.cognizant_dust")
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(EnchanterScreen.class, 104, 41, 22, 15, EnchanterCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(EssenceFurnaceScreen.class, 99, 52, 22, 15, mezz.jei.api.constants.RecipeTypes.SMELTING);
        registration.addRecipeClickArea(ReprocessorScreen.class, 99, 52, 22, 15, ReprocessorCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(SoulExtractorScreen.class, 99, 52, 22, 15, SoulExtractorCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(SouliumSpawnerScreen.class, 99, 52, 22, 15, SouliumSpawnerCategory.RECIPE_TYPE);
        registration.addRecipeClickArea(OreInfuserScreen.class, 105, 52, 22, 15, OreInfuserCategory.RECIPE_TYPE);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.SOUL_JAR, (stack, _) -> {
            var type = MobSoulUtils.getType(stack);
            return type != null ? type.getEntityIds().toString() : "";
        });
    }
}
