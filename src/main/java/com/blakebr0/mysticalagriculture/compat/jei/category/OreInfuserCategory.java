package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import com.blakebr0.mysticalagriculture.api.crafting.IOreInfusionRecipe;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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

import java.util.List;

public class OreInfuserCategory implements IRecipeCategory<RecipeHolder<IOreInfusionRecipe>> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/ore_infuser.png");
    public static final IRecipeHolderType<IOreInfusionRecipe> RECIPE_TYPE = IRecipeHolderType.create(MysticalAgriculture.resource("ore_infuser"));

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public OreInfuserCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 102, 26);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ORE_INFUSER));

        var arrow = helper.createDrawable(TEXTURE, 105, 0, 24, 17);

        this.arrow = helper.createAnimatedDrawable(arrow, 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public IRecipeType<RecipeHolder<IOreInfusionRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.ore_infuser");
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
    public void draw(RecipeHolder<IOreInfusionRecipe> recipe, IRecipeSlotsView slots, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
        this.arrow.draw(gfx, 45, 4);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<IOreInfusionRecipe> recipeHolder, IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        var inputs = recipe.getIngredients();
        var result = recipe.assemble(CraftingInput.EMPTY);

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addItemStacks(createInputsList(inputs.get(0)));
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 5).addItemStacks(createInputsList(inputs.get(1)));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 5).add(result);
    }

    private static List<ItemStack> createInputsList(IngredientWithCount input) {
        return input.ingredient()
                .items()
                .map(item -> new ItemStack(item.value(), input.count()))
                .toList();
    }
}
