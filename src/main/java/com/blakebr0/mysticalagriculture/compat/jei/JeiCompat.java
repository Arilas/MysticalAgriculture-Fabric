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
import com.blakebr0.mysticalagriculture.container.EnchanterContainer;
import com.blakebr0.mysticalagriculture.container.EssenceFurnaceContainer;
import com.blakebr0.mysticalagriculture.container.OreInfuserContainer;
import com.blakebr0.mysticalagriculture.container.ReprocessorContainer;
import com.blakebr0.mysticalagriculture.container.SoulExtractorContainer;
import com.blakebr0.mysticalagriculture.container.SouliumSpawnerContainer;
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
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
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
        MysticalAgriculture.LOGGER.info("Registered 8 Mystical Agriculture JEI recipe categories");
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
        MysticalAgriculture.LOGGER.info("Registered 11 Mystical Agriculture JEI crafting stations");
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

        var cruxRecipes = CruxRecipe.createAll();
        registration.addRecipes(CruxCategory.RECIPE_TYPE, cruxRecipes);

        registration.addIngredientInfo(
                new ItemStack(ModItems.COGNIZANT_DUST),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.desc.mysticalagriculture.cognizant_dust")
        );
        MysticalAgriculture.LOGGER.info(
                "Registered Mystical Agriculture JEI recipes after client recipe synchronization: infusion={}, awakening={}, enchanter={}, reprocessor={}, soul_extractor={}, soulium_spawner={}, ore_infuser={}, crux={}",
                ClientRecipeHandler.INFUSION_RECIPES.size(),
                ClientRecipeHandler.AWAKENING_RECIPES.size(),
                ClientRecipeHandler.ENCHANTER_RECIPES.size(),
                ClientRecipeHandler.REPROCESSOR_RECIPES.size(),
                ClientRecipeHandler.SOUL_EXTRACTION_RECIPES.size(),
                ClientRecipeHandler.SOULIUM_SPAWNER_RECIPES.size(),
                ClientRecipeHandler.ORE_INFUSION_RECIPES.size(),
                cruxRecipes.size()
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
        MysticalAgriculture.LOGGER.info("Registered 6 Mystical Agriculture JEI GUI click areas");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                EnchanterContainer.class, null, EnchanterCategory.RECIPE_TYPE,
                0, 3, 4, 36
        );
        registration.addRecipeTransferHandler(
                EssenceFurnaceContainer.class, null, RecipeTypes.SMELTING,
                1, 1, 4, 36
        );
        registration.addRecipeTransferHandler(
                ReprocessorContainer.class, null, ReprocessorCategory.RECIPE_TYPE,
                1, 1, 4, 36
        );
        registration.addRecipeTransferHandler(
                SoulExtractorContainer.class, null, SoulExtractorCategory.RECIPE_TYPE,
                1, 1, 4, 36
        );
        registration.addRecipeTransferHandler(
                SouliumSpawnerContainer.class, null, SouliumSpawnerCategory.RECIPE_TYPE,
                1, 1, 3, 36
        );
        registration.addRecipeTransferHandler(
                OreInfuserContainer.class, null, OreInfuserCategory.RECIPE_TYPE,
                1, 2, 5, 36
        );
        MysticalAgriculture.LOGGER.info("Registered 6 Mystical Agriculture JEI recipe transfer handlers");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, ModItems.SOUL_JAR, (stack, _) -> {
            var type = MobSoulUtils.getType(stack);
            return type != null ? type.getEntityIds().toString() : "";
        });
    }
}
