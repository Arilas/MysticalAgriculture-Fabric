package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class InfusionCategory implements IRecipeCategory<RecipeHolder<IInfusionRecipe>> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/infusion.png");
    public static final IRecipeHolderType<IInfusionRecipe> RECIPE_TYPE = IRecipeHolderType.create(MysticalAgriculture.resource("infusion"));

    private final IDrawable background;
    private final IDrawable icon;

    public InfusionCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 144, 81);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.INFUSION_ALTAR));
    }

    @Override
    public IRecipeType<RecipeHolder<IInfusionRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.infusion");
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
    public void draw(RecipeHolder<IInfusionRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<IInfusionRecipe> recipeHolder, IFocusGroup focuses) {
        var recipe = recipeHolder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 33, 33).add(recipe.getAltarIngredient());

        var inputs = recipe.getPedestalIngredients();
        var pedestals = inputs.size();

        switch (pedestals) {
            case 1 -> addSlot(builder, SlotPosition.NORTH, inputs.get(0));
            case 2 -> {
                addSlot(builder, SlotPosition.NORTH, inputs.get(0));
                addSlot(builder, SlotPosition.SOUTH, inputs.get(1));
            }
            case 3 -> {
                addSlot(builder, SlotPosition.WEST, inputs.get(0));
                addSlot(builder, SlotPosition.NORTH, inputs.get(1));
                addSlot(builder, SlotPosition.EAST, inputs.get(2));
            }
            case 4 -> {
                addSlot(builder, SlotPosition.NORTH, inputs.get(0));
                addSlot(builder, SlotPosition.EAST, inputs.get(1));
                addSlot(builder, SlotPosition.SOUTH, inputs.get(2));
                addSlot(builder, SlotPosition.WEST, inputs.get(3));
            }
            case 5 -> {
                addSlot(builder, SlotPosition.NORTH_WEST, inputs.get(0));
                addSlot(builder, SlotPosition.NORTH, inputs.get(1));
                addSlot(builder, SlotPosition.NORTH_EAST, inputs.get(2));
                addSlot(builder, SlotPosition.SOUTH_EAST, inputs.get(3));
                addSlot(builder, SlotPosition.SOUTH_WEST, inputs.get(4));
            }
            case 6 -> {
                addSlot(builder, SlotPosition.NORTH_WEST, inputs.get(0));
                addSlot(builder, SlotPosition.NORTH, inputs.get(1));
                addSlot(builder, SlotPosition.NORTH_EAST, inputs.get(2));
                addSlot(builder, SlotPosition.SOUTH_EAST, inputs.get(3));
                addSlot(builder, SlotPosition.SOUTH, inputs.get(4));
                addSlot(builder, SlotPosition.SOUTH_WEST, inputs.get(5));
            }
            case 7 -> {
                addSlot(builder, SlotPosition.WEST, inputs.get(0));
                addSlot(builder, SlotPosition.NORTH_WEST, inputs.get(1));
                addSlot(builder, SlotPosition.NORTH, inputs.get(2));
                addSlot(builder, SlotPosition.NORTH_EAST, inputs.get(3));
                addSlot(builder, SlotPosition.EAST, inputs.get(4));
                addSlot(builder, SlotPosition.SOUTH_EAST, inputs.get(5));
                addSlot(builder, SlotPosition.SOUTH_WEST, inputs.get(6));
            }
            case 8 -> {
                addSlot(builder, SlotPosition.NORTH_WEST, inputs.get(0));
                addSlot(builder, SlotPosition.NORTH, inputs.get(1));
                addSlot(builder, SlotPosition.NORTH_EAST, inputs.get(2));
                addSlot(builder, SlotPosition.EAST, inputs.get(3));
                addSlot(builder, SlotPosition.SOUTH_EAST, inputs.get(4));
                addSlot(builder, SlotPosition.SOUTH, inputs.get(5));
                addSlot(builder, SlotPosition.SOUTH_WEST, inputs.get(6));
                addSlot(builder, SlotPosition.WEST, inputs.get(7));
            }
        }

        var result = recipe.assemble(CraftingInput.EMPTY);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 33).add(result);
    }

    private static void addSlot(IRecipeLayoutBuilder builder, SlotPosition position, Ingredient ingredient) {
        builder.addSlot(RecipeIngredientRole.INPUT, position.x, position.y).add(ingredient);
    }

    private enum SlotPosition {
        NORTH_WEST(7, 7),
        NORTH(33, 1),
        NORTH_EAST(59, 7),
        EAST(65, 33),
        SOUTH_EAST(59, 59),
        SOUTH(33, 64),
        SOUTH_WEST(7, 59),
        WEST(1, 33);

        final int x;
        final int y;

        SlotPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
