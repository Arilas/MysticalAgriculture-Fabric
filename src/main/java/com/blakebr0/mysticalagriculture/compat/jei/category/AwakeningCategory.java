package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public class AwakeningCategory implements IRecipeCategory<RecipeHolder<IAwakeningRecipe>> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/infusion.png");
    public static final IRecipeHolderType<IAwakeningRecipe> RECIPE_TYPE = IRecipeHolderType.create(MysticalAgriculture.resource("awakening"));

    private final IDrawable background;
    private final IDrawable icon;

    public AwakeningCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 144, 81);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.AWAKENING_ALTAR));
    }

    @Override
    public IRecipeType<RecipeHolder<IAwakeningRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.awakening");
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return this.background.getWidth();
    }

    @Override
    public int getHeight() {
        return this.background.getHeight();
    }

    @Override
    public void draw(RecipeHolder<IAwakeningRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<IAwakeningRecipe> recipeHolder, IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        var pedestals = recipe.getPedestalIngredients();
        var essences = recipe.getEssenceIngredients();
        var result = recipe.assemble(CraftingInput.EMPTY);

        builder.addSlot(RecipeIngredientRole.INPUT, 33, 33).add(recipe.getAltarIngredient());
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 7).addItemStacks(createEssenceIngredient(essences.get(0)));
        builder.addSlot(RecipeIngredientRole.INPUT, 33, 1).add(pedestals.get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 59, 7).addItemStacks(createEssenceIngredient(essences.get(1)));
        builder.addSlot(RecipeIngredientRole.INPUT, 65, 33).add(pedestals.get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 59, 59).addItemStacks(createEssenceIngredient(essences.get(2)));
        builder.addSlot(RecipeIngredientRole.INPUT, 33, 64).add(pedestals.get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 7, 59).addItemStacks(createEssenceIngredient(essences.get(3)));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 33).add(pedestals.get(3));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 33).add(result);
    }

    private static List<ItemStack> createEssenceIngredient(SizedIngredient ingredient) {
        return ingredient.ingredient().items().map(item -> new ItemStack(item, ingredient.count())).toList();
    }
}
