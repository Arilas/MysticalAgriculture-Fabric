package com.blakebr0.mysticalagriculture.api.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientWithCount(Ingredient ingredient, int count) {
    public static final MapCodec<IngredientWithCount> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithCount::ingredient),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count").forGetter(IngredientWithCount::count)
            ).apply(builder, IngredientWithCount::new)
    );
    public static final Codec<IngredientWithCount> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC,
            IngredientWithCount::ingredient,
            ByteBufCodecs.VAR_INT,
            IngredientWithCount::count,
            IngredientWithCount::new
    );

    public IngredientWithCount {
        if (count < 1) {
            throw new IllegalArgumentException("Ingredient count must be at least one");
        }
    }

    public boolean test(ItemStack stack) {
        return stack.getCount() >= this.count && this.ingredient.test(stack);
    }
}
