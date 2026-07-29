package com.blakebr0.mysticalagriculture.compat.jei.category;

import com.blakebr0.cucumber.util.Formatting;
import com.blakebr0.mysticalagriculture.MysticalAgriculture;
import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.lib.ModTooltips;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class SouliumSpawnerCategory implements IRecipeCategory<RecipeHolder<ISouliumSpawnerRecipe>> {
    private static final Identifier TEXTURE = MysticalAgriculture.resource("textures/jei/soulium_spawner.png");
    public static final IRecipeHolderType<ISouliumSpawnerRecipe> RECIPE_TYPE = IRecipeHolderType.create(MysticalAgriculture.resource("soulium_spawner"));

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public SouliumSpawnerCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 82, 26);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SOULIUM_SPAWNER));

        var arrow = helper.createDrawable(TEXTURE, 85, 0, 24, 17);

        this.arrow = helper.createAnimatedDrawable(arrow, 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public IRecipeType<RecipeHolder<ISouliumSpawnerRecipe>> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.category.mysticalagriculture.soulium_spawner");
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
    public void draw(RecipeHolder<ISouliumSpawnerRecipe> recipe, IRecipeSlotsView slots, GuiGraphicsExtractor gfx, double mouseX, double mouseY) {
        this.background.draw(gfx);
        this.arrow.draw(gfx, 24, 4);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ISouliumSpawnerRecipe> recipeHolder, IFocusGroup focuses) {
        var recipe = recipeHolder.value();
        var inputs = createInputsList(recipe);
        var outputs = createOutputsList(recipe);

        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addItemStacks(inputs);

        var totalWeight = recipe.getEntityTypes().unwrap()
                .stream().mapToInt(Weighted::weight).sum();

        builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 5)
                .addItemStacks(outputs)
                .addRichTooltipCallback((slots, tooltip) -> slots.getDisplayedItemStack().ifPresent(stack -> {
                    var data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data == null || !data.copyTag().contains("Weight"))
                        return;

                    var weight = data.copyTag().getIntOr("Weight", 0);
                    var chance = ((double) weight / (double) totalWeight) * 100D;

                    tooltip.add(ModTooltips.CHANCE.args(Formatting.percent(chance)).toComponent());
                }));
    }

    private static List<ItemStack> createInputsList(ISouliumSpawnerRecipe recipe) {
        var input = recipe.getIngredient();
        return input.ingredient()
                .items()
                .map(item -> new ItemStack(item.value(), input.count()))
                .toList();
    }

    private static List<ItemStack> createOutputsList(ISouliumSpawnerRecipe recipe) {
        var entries = recipe.getEntityTypes().unwrap();
        var outputs = new ArrayList<ItemStack>();

        for (var entry : entries) {
            SpawnEggItem.byId(entry.value()).ifPresent(item -> {
                var tag = new CompoundTag();
                tag.putInt("Weight", entry.weight());

                var stack = new ItemStack(item);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                outputs.add(stack);
            });
        }

        return outputs;
    }
}
