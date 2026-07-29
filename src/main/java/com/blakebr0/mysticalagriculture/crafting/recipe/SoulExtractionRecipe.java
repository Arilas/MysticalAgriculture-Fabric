package com.blakebr0.mysticalagriculture.crafting.recipe;

import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class SoulExtractionRecipe implements ISoulExtractionRecipe {
    public static final MapCodec<SoulExtractionRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Ingredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
                    Result.CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
            ).apply(builder, SoulExtractionRecipe::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SoulExtractionRecipe> STREAM_CODEC = StreamCodec.of(
            SoulExtractionRecipe::toNetwork, SoulExtractionRecipe::fromNetwork
    );
    public static final RecipeSerializer<SoulExtractionRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient input;
    private final MobSoulType type;
    private final double souls;
    private final Result result;

    public SoulExtractionRecipe(Ingredient input, Result result) {
        this.input = input;
        this.type = MobSoulTypeRegistry.getInstance().getMobSoulTypeById(result.type);
        this.souls = result.souls;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level level) {
        if (inventory.size() < 3)
            return false;

        var input = inventory.getItem(0);
        if (!this.input.test(input))
            return false;

        var output = inventory.getItem(2);
        if (!output.is(this.result.stack().getItem()))
            return false;

        return MobSoulUtils.canAddTypeToJar(output, this.type) && !MobSoulUtils.isJarFull(output);
    }

    @Override
    public ItemStack assemble(CraftingInput inventory) {
        if (inventory.ingredientCount() == 0)
            return this.result.stack();

        var stack = inventory.getItem(2);
        var jar = stack.copyWithCount(1);

        MobSoulUtils.addSoulsToJar(jar, this.type, this.souls);

        return jar;
    }

    @Override
    public Ingredient getIngredient() {
        return this.input;
    }

    @Override
    public RecipeSerializer<SoulExtractionRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<ISoulExtractionRecipe> getType() {
        return ModRecipeTypes.SOUL_EXTRACTION.get();
    }

    @Override
    public MobSoulType getMobSoulType() {
        return this.type;
    }

    @Override
    public double getSouls() {
        return this.souls;
    }

    private static SoulExtractionRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        var input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        var result = Result.STREAM_CODEC.decode(buffer);

        return new SoulExtractionRecipe(input, result);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, SoulExtractionRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
        Result.STREAM_CODEC.encode(buffer, recipe.result);
    }

    public static class Result {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(builder ->
               builder.group(
                       Identifier.CODEC.fieldOf("type").forGetter(result -> result.type),
                       Codec.DOUBLE.fieldOf("souls").forGetter(result -> result.souls)
               ).apply(builder, Result::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, Result> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                result -> result.type,
                ByteBufCodecs.DOUBLE,
                result -> result.souls,
                Result::new
        );

        public final Identifier type;
        public final double souls;

        private ItemStack stack;

        public Result(Identifier type, double souls) {
            this.type = type;
            this.souls = souls;
        }

        public ItemStack stack() {
            if (stack == null) {
                this.stack = MobSoulUtils.getSoulJar(MobSoulTypeRegistry.getInstance().getMobSoulTypeById(type), souls, ModItems.SOUL_JAR);
            }

            return this.stack;
        }
    }
}
