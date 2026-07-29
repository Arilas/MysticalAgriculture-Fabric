package com.blakebr0.mysticalagriculture.crafting.recipe;

import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.crafting.ingredient.FilledSoulJarIngredient;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.item.SoulJarItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

public class SoulJarEmptyRecipe implements CraftingRecipe {
    public static final MapCodec<SoulJarEmptyRecipe> MAP_CODEC = MapCodec.unit(() -> new SoulJarEmptyRecipe(
            new ItemStackTemplate(ModItems.SOUL_JAR),
            NonNullList.withSize(1, FilledSoulJarIngredient.of())
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, SoulJarEmptyRecipe> STREAM_CODEC = StreamCodec.of(
            SoulJarEmptyRecipe::toNetwork, SoulJarEmptyRecipe::fromNetwork
    );
    public static final RecipeSerializer<SoulJarEmptyRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    private PlacementInfo placementInfo;

    public SoulJarEmptyRecipe(ItemStackTemplate result, List<Ingredient> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        var hasJar = false;

        for (int i = 0; i < inventory.size(); i++) {
            var stack = inventory.getItem(i);

            if (hasJar && !stack.isEmpty())
                return false;

            var item = stack.getItem();

            if (item instanceof SoulJarItem) {
                double souls = MobSoulUtils.getSouls(stack);
                if (souls > 0) {
                    hasJar = true;
                }
            } else if (!stack.isEmpty()) {
                return false;
            }
        }

        return hasJar;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }

        return this.placementInfo;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new ShapelessCraftingRecipeDisplay(
                this.ingredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
        ));
    }

    @Override
    public RecipeSerializer<SoulJarEmptyRecipe> getSerializer() {
        return SERIALIZER;
    }

    private static SoulJarEmptyRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        return new SoulJarEmptyRecipe(new ItemStackTemplate(ModItems.SOUL_JAR), NonNullList.withSize(1, FilledSoulJarIngredient.of()));
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, SoulJarEmptyRecipe recipe) { }
}
