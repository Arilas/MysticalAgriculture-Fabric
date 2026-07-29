package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.compat.jei.recipe.CruxRecipe;
import com.blakebr0.mysticalagriculture.init.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class CruxCategory implements IRecipeCategory<CruxRecipe> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/crux.png");
    public static final IRecipeType<CruxRecipe> RECIPE_TYPE = IRecipeType.create(MysticalAgriculture.resource("crux"), CruxRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CruxCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 80, 54);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.PROSPERITY_SEED_BASE));
    }

    @Override
    public IRecipeType<CruxRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.crux");
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
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(CruxRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CruxRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.getIngredients();
        var output = recipe.essence();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).add(inputs.get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 19).add(inputs.get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 37).add(inputs.get(2));

        builder.addSlot(RecipeIngredientRole.INPUT, 59, 20).add(output);
    }
}
