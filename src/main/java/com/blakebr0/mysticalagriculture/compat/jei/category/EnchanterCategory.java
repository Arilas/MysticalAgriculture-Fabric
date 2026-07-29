package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.List;
import java.util.stream.IntStream;

public class EnchanterCategory implements IRecipeCategory<RecipeHolder<IEnchanterRecipe>> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/enchanter.png");
    public static final IRecipeHolderType<IEnchanterRecipe> RECIPE_TYPE = IRecipeHolderType.create(MysticalAgriculture.resource("enchanter"));

    private final IDrawable background;
    private final IDrawable icon;

    public EnchanterCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 144, 26);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ENCHANTER));
    }

    @Override
    public IRecipeType<RecipeHolder<IEnchanterRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.enchanter");
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
    public void draw(RecipeHolder<IEnchanterRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<IEnchanterRecipe> recipeHolder, IFocusGroup focuses) {
        var recipe = recipeHolder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addItemStacks(createIngredientItemStack(recipe, 0));
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 5).addItemStacks(createIngredientItemStack(recipe, 1));
        builder.addSlot(RecipeIngredientRole.INPUT, 63, 5).addItemStacks(createIngredientItemStack(recipe, 2));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 123, 5).addItemStacks(createIngredientItemStack(recipe, 3));
    }

    private static List<ItemStack> createIngredientItemStack(IEnchanterRecipe recipe, int slot) {
        if (slot == 2) {
            return List.of(new ItemStack(Items.BOOK));
        }

        var enchantment = recipe.getEnchantment();
        var maxLevel = enchantment.value().getMaxLevel();

        if (slot == 3) {
            return IntStream.rangeClosed(1, maxLevel)
                    .mapToObj(i -> EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, i)))
                    .toList();
        }

        var input = recipe.getIngredients().get(slot);

        return input.ingredient().items().flatMap(item ->
            IntStream.rangeClosed(1, maxLevel).mapToObj(i -> new ItemStack(item, input.count() * i))
        ).toList();
    }
}
