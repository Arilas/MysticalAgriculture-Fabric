package com.blakebr0.mysticalagriculture.crafting.recipe;

import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.RecipeIngredientMatcher;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class InfusionRecipe implements IInfusionRecipe {
    public static final MapCodec<InfusionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                    Ingredient.CODEC
                            .listOf()
                            .fieldOf("ingredients")
                            .flatXmap(
                                    field -> {
                                        var max = 8;
                                        var ingredients = field.toArray(Ingredient[]::new);
                                        if (ingredients.length == 0) {
                                            return DataResult.error(() -> "No ingredients for infusion recipe");
                                        } else {
                                            return ingredients.length > max
                                                    ? DataResult.error(() -> "Too many ingredients for infusion recipe. The maximum is: %s".formatted(max))
                                                    : DataResult.success(Arrays.stream(ingredients).toList());
                                        }
                                    },
                                    DataResult::success
                            )
                            .forGetter(recipe -> recipe.inputs),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                    Codec.BOOL.optionalFieldOf("transfer_components", false).forGetter(recipe -> recipe.transferComponents)
            ).apply(builder, InfusionRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.of(
            InfusionRecipe::toNetwork, InfusionRecipe::fromNetwork
    );
    public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient input;
    private final List<Ingredient> inputs;
    private final ItemStackTemplate result;
    private final boolean transferComponents;
    // for CraftTweaker recipes
    private BiFunction<Integer, ItemStack, ItemStack> transformer;

    // the input is specified separately in JSON but is part of the ingredient list in practice
    public InfusionRecipe(Ingredient input, List<Ingredient> inputs, ItemStackTemplate result, boolean transferComponents) {
        this.input = input;
        this.inputs = inputs;
        this.result = result;
        this.transferComponents = transferComponents;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        // -1 ingredient for the input item
        if (this.inputs.size() != inventory.ingredientCount() - 1)
            return false;

        var input = inventory.getItem(0);
        if (!this.input.test(input))
            return false;

        var inputs = NonNullList.<ItemStack>create();

        for (var i = 1; i < inventory.size(); i++) {
            var item = inventory.getItem(i);
            if (!item.isEmpty()) {
                inputs.add(item);
            }
        }

        return RecipeIngredientMatcher.matchesIngredients(inputs, this.inputs);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory) {
        var result = this.result.create();

        if (inventory.ingredientCount() == 0)
            return result;

        var stack = inventory.getItem(0);

        if (this.transferComponents) {
            result.applyComponents(stack.getComponentsPatch());
        }

        return result;
    }

    @Override
    public Ingredient getAltarIngredient() {
        return this.input;
    }

    @Override
    public List<Ingredient> getPedestalIngredients() {
        return this.inputs;
    }

    @Override
    public RecipeSerializer<InfusionRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<IInfusionRecipe> getType() {
        return ModRecipeTypes.INFUSION;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        var remaining = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            var item = inventory.getItem(i);
            var remainder = item.getCraftingRemainder();
            if (remainder != null) {
                remaining.set(i, remainder.create());
            }
        }

        if (this.transformer != null) {
            var used = new boolean[remaining.size()];
            var inputs = NonNullList.<Ingredient>create();

            inputs.add(this.input);
            inputs.addAll(this.inputs);

            for (int i = 0; i < remaining.size(); i++) {
                var stack = inventory.getItem(i);
                for (int j = 0; j < inputs.size(); j++) {
                    var input = inputs.get(j);

                    if (!used[j] && input.test(stack)) {
                        var ingredient = this.transformer.apply(j, stack);

                        used[j] = true;
                        remaining.set(i, ingredient);

                        break;
                    }
                }
            }
        }

        return remaining;
    }

    public void setTransformer(BiFunction<Integer, ItemStack, ItemStack> transformer) {
        this.transformer = transformer;
    }

    private static InfusionRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        var input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        var inputs = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buffer);
        var result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
        var transferComponents = buffer.readBoolean();

        return new InfusionRecipe(input, inputs, result, transferComponents);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, InfusionRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buffer, recipe.inputs);
        ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
        buffer.writeBoolean(recipe.transferComponents);
    }
}
