package com.blakebr0.mysticalagriculture.crafting.ingredient;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

public final class FilledSoulJarIngredient implements CustomIngredient {
    public static final MapCodec<FilledSoulJarIngredient> CODEC =
            MapCodec.unit(FilledSoulJarIngredient::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, FilledSoulJarIngredient> STREAM_CODEC =
            StreamCodec.unit(new FilledSoulJarIngredient());
    public static final CustomIngredientSerializer<FilledSoulJarIngredient> SERIALIZER =
            new CustomIngredientSerializer<>() {
                @Override
                public net.minecraft.resources.Identifier getIdentifier() {
                    return CustomIngredientMatcher.FILLED_SOUL_JAR_ID;
                }

                @Override
                public MapCodec<FilledSoulJarIngredient> getCodec() {
                    return CODEC;
                }

                @Override
                public StreamCodec<RegistryFriendlyByteBuf, FilledSoulJarIngredient> getStreamCodec() {
                    return STREAM_CODEC;
                }
            };
    private static final ResourceKey<Item> SOUL_JAR = ResourceKey.create(
            net.minecraft.core.registries.Registries.ITEM,
            MysticalAgricultureAPI.resource("soul_jar")
    );

    private HolderSet<Item> values;

    @Override
    public boolean test(ItemStack stack) {
        return CustomIngredientMatcher.isFilledSoulJar(stack, SOUL_JAR);
    }

    @Override
    public Stream<Holder<Item>> items() {
        if (this.values == null) {
            var values = new ArrayList<Holder<Item>>();
            BuiltInRegistries.ITEM.get(SOUL_JAR).ifPresent(soulJar -> {
                for (var type : MobSoulTypeRegistry.getInstance().getMobSoulTypes()) {
                    values.add(MobSoulUtils.getFilledSoulJar(type, soulJar.value()).typeHolder());
                }
            });
            this.values = HolderSet.direct(values);
        }
        return this.values.stream();
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public static Ingredient of() {
        return new FilledSoulJarIngredient().toVanilla();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FilledSoulJarIngredient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(FilledSoulJarIngredient.class);
    }
}
