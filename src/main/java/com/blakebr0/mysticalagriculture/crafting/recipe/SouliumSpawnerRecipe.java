package com.blakebr0.mysticalagriculture.crafting.recipe;

import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IngredientWithCount;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SouliumSpawnerRecipe implements ISouliumSpawnerRecipe {
    private static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> ENTITY_TYPE_STREAM_CODEC = ByteBufCodecs.registry(Registries.ENTITY_TYPE);
    public static final MapCodec<SouliumSpawnerRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    IngredientWithCount.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                    WeightedList.codec(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity")).fieldOf("entities").forGetter(recipe -> recipe.entityTypes)
            ).apply(builder, SouliumSpawnerRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SouliumSpawnerRecipe> STREAM_CODEC = StreamCodec.of(
            SouliumSpawnerRecipe::toNetwork, SouliumSpawnerRecipe::fromNetwork
    );
    public static final RecipeSerializer<SouliumSpawnerRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final IngredientWithCount input;
    private final WeightedList<EntityType<?>> entityTypes;

    public SouliumSpawnerRecipe(IngredientWithCount input, WeightedList<EntityType<?>> entityTypes) {
        this.input = input;
        this.entityTypes = entityTypes;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        var stack = inventory.getItem(0);
        return this.input.test(stack);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public IngredientWithCount getIngredient() {
        return this.input;
    }

    @Override
    public RecipeSerializer<SouliumSpawnerRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<? extends ISouliumSpawnerRecipe> getType() {
        return ModRecipeTypes.SOULIUM_SPAWNER;
    }

    @Override
    public WeightedList<EntityType<?>> getEntityTypes() {
        return this.entityTypes;
    }

    @Override
    public EntityType<?> getFirstEntityType() {
        return this.entityTypes.unwrap().getFirst().value();
    }

    @Override
    public Optional<EntityType<?>> getRandomEntityType(RandomSource random) {
        return this.entityTypes.getRandom(random);
    }

    private static SouliumSpawnerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        var input = IngredientWithCount.STREAM_CODEC.decode(buffer);
        var entities = WeightedList.streamCodec(ENTITY_TYPE_STREAM_CODEC).decode(buffer);

        return new SouliumSpawnerRecipe(input, entities);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, SouliumSpawnerRecipe recipe) {
        IngredientWithCount.STREAM_CODEC.encode(buffer, recipe.input);
        WeightedList.streamCodec(ENTITY_TYPE_STREAM_CODEC).encode(buffer, recipe.entityTypes);
    }
}
