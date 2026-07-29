package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.crafting.ingredient.CustomIngredientMatcher;
import com.blakebr0.mysticalagriculture.crafting.recipe.AwakeningRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.EnchanterRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.FarmlandTillRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.OreInfusionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.ReprocessorRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SoulJarEmptyRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.SouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.init.ModRecipeSerializers;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeCodecTest {
    private static List<RecipeCase<?>> recipes;

    private static List<RecipeCase<?>> recipes() {
        return List.of(
            recipeCase("awakening", AwakeningRecipe.MAP_CODEC.codec(), AwakeningRecipe.STREAM_CODEC, """
                    {
                      "input": "minecraft:diamond",
                      "ingredients": ["minecraft:stick", "minecraft:stick", "minecraft:stick", "minecraft:stick"],
                      "essences": [
                        {"ingredient": "minecraft:redstone", "count": 1},
                        {"ingredient": "minecraft:glowstone_dust", "count": 2},
                        {"ingredient": "minecraft:lapis_lazuli", "count": 3},
                        {"ingredient": "minecraft:quartz", "count": 4}
                      ],
                      "result": {"id": "minecraft:emerald"},
                      "transfer_components": true
                    }
                    """),
            recipeCase("enchanter", EnchanterRecipe.MAP_CODEC.codec(), EnchanterRecipe.STREAM_CODEC, """
                    {
                      "ingredients": [
                        {"ingredient": "minecraft:redstone", "count": 8},
                        {"ingredient": "minecraft:lapis_lazuli", "count": 16}
                      ],
                      "enchantment": "minecraft:protection"
                    }
                    """),
            recipeCase("farmland_till", FarmlandTillRecipe.MAP_CODEC.codec(), FarmlandTillRecipe.STREAM_CODEC, """
                    {
                      "result": {"id": "minecraft:farmland"},
                      "ingredients": ["minecraft:dirt", "minecraft:wooden_hoe"]
                    }
                    """),
            recipeCase("infusion", InfusionRecipe.MAP_CODEC.codec(), InfusionRecipe.STREAM_CODEC, """
                    {
                      "input": "minecraft:diamond",
                      "ingredients": ["minecraft:redstone", "minecraft:glowstone_dust"],
                      "result": {"id": "minecraft:emerald"},
                      "transfer_components": true
                    }
                    """),
            recipeCase("ore_infusion", OreInfusionRecipe.MAP_CODEC.codec(), OreInfusionRecipe.STREAM_CODEC, """
                    {
                      "ingredients": [
                        {"ingredient": "minecraft:stone", "count": 1},
                        {"ingredient": "minecraft:redstone", "count": 6}
                      ],
                      "result": {"id": "minecraft:deepslate"}
                    }
                    """),
            recipeCase("reprocessor", ReprocessorRecipe.MAP_CODEC.codec(), ReprocessorRecipe.STREAM_CODEC, """
                    {
                      "input": "minecraft:wheat_seeds",
                      "result": {"id": "minecraft:wheat", "count": 2}
                    }
                    """),
            recipeCase("soul_extraction", SoulExtractionRecipe.MAP_CODEC.codec(), SoulExtractionRecipe.STREAM_CODEC, """
                    {
                      "input": "minecraft:porkchop",
                      "result": {"type": "testing:test_soul", "souls": 0.5}
                    }
                    """),
            recipeCase("soul_jar_empty", SoulJarEmptyRecipe.MAP_CODEC.codec(), SoulJarEmptyRecipe.STREAM_CODEC, "{}"),
            recipeCase("soulium_spawner", SouliumSpawnerRecipe.MAP_CODEC.codec(), SouliumSpawnerRecipe.STREAM_CODEC, """
                    {
                      "input": {"ingredient": "minecraft:rotten_flesh", "count": 16},
                      "entities": [
                        {"entity": "minecraft:zombie", "weight": 18},
                        {"entity": "minecraft:husk", "weight": 1}
                      ]
                    }
                    """)
        );
    }

    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
        recipes = recipes();
    }

    @Test
    void allNineRecipeJsonAndNetworkCodecsRoundTripLiteralSchemas() {
        for (var recipe : recipes) {
            assertRoundTrip(recipe);
        }
        assertEquals(9, recipes.size());
    }

    @Test
    void serializerIdsRemainRegisteredInTheVanillaRegistry() {
        assertSerializerId("farmland_till", ModRecipeSerializers.CRAFTING_FARMLAND_TILL);
        assertSerializerId("infusion", ModRecipeSerializers.INFUSION);
        assertSerializerId("awakening", ModRecipeSerializers.AWAKENING);
        assertSerializerId("enchanter", ModRecipeSerializers.ENCHANTER);
        assertSerializerId("reprocessor", ModRecipeSerializers.REPROCESSOR);
        assertSerializerId("soul_extraction", ModRecipeSerializers.SOUL_EXTRACTION);
        assertSerializerId("soulium_spawner", ModRecipeSerializers.SOULIUM_SPAWNER);
        assertSerializerId("ore_infusion", ModRecipeSerializers.ORE_INFUSION);
        assertSerializerId("soul_jar_empty", ModRecipeSerializers.CRAFTING_SOUL_JAR_EMPTY);
    }

    @Test
    void cropIngredientMatchesOnlyTheRequiredMaterialComponents() {
        var matching = new ItemStack(Items.DIAMOND);
        matching.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("required material"));
        var wrongComponents = new ItemStack(Items.DIAMOND);
        wrongComponents.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("wrong material"));
        var required = net.minecraft.core.component.DataComponentPatch.builder()
                .set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                        net.minecraft.network.chat.Component.literal("required material"))
                .build();

        assertTrue(CustomIngredientMatcher.matchesComponents(
                matching,
                java.util.stream.Stream.of(Items.DIAMOND.builtInRegistryHolder()),
                required
        ));
        assertFalse(CustomIngredientMatcher.matchesComponents(
                wrongComponents,
                java.util.stream.Stream.of(Items.DIAMOND.builtInRegistryHolder()),
                required
        ));
    }

    @Test
    void filledSoulJarIngredientRequiresARealPositiveSoulComponent() {
        var soulJar = net.minecraft.resources.ResourceKey.create(
                Registries.ITEM,
                Identifier.parse("minecraft:glass_bottle")
        );
        var empty = new ItemStack(Items.GLASS_BOTTLE);
        var filled = new ItemStack(Items.GLASS_BOTTLE);
        filled.set(
                com.blakebr0.mysticalagriculture.api.MysticalAgricultureDataComponentTypes.SOUL_JAR,
                new com.blakebr0.mysticalagriculture.api.components.SoulJarComponent(
                        MobSoulTypeRegistryAccess.testSoul().getId(),
                        0.5
                )
        );
        assertFalse(CustomIngredientMatcher.isFilledSoulJar(empty, soulJar));
        assertTrue(CustomIngredientMatcher.isFilledSoulJar(filled, soulJar));
        assertEquals(Identifier.parse("mysticalagriculture:crop_component"),
                CustomIngredientMatcher.CROP_COMPONENT_ID);
        assertEquals(Identifier.parse("mysticalagriculture:filled_soul_jar"),
                CustomIngredientMatcher.FILLED_SOUL_JAR_ID);
    }

    private static void assertSerializerId(String path, RecipeSerializer<?> serializer) {
        assertEquals(
                Identifier.fromNamespaceAndPath("mysticalagriculture", path),
                Task3TestRegistries.serializerId(serializer)
        );
    }

    private static <T> void assertRoundTrip(RecipeCase<T> recipe) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, Task3TestRegistries.registryAccess());
        var expectedJson = JsonParser.parseString(recipe.json());
        var decodedJson = recipe.codec().parse(ops, expectedJson).getOrThrow();
        var encodedJson = recipe.codec().encodeStart(ops, decodedJson).getOrThrow();
        assertEquals(expectedJson, encodedJson, recipe.id() + " JSON round trip");

        var buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                Task3TestRegistries.registryAccess()
        );
        recipe.streamCodec().encode(buffer, decodedJson);
        var decodedNetwork = recipe.streamCodec().decode(buffer);
        JsonElement encodedNetwork = recipe.codec().encodeStart(ops, decodedNetwork).getOrThrow();

        assertEquals(expectedJson, encodedNetwork, recipe.id() + " network round trip");
        assertEquals(0, buffer.readableBytes(), recipe.id() + " packet must be fully consumed");
    }

    private static <T> RecipeCase<T> recipeCase(
            String id,
            Codec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
            String json
    ) {
        return new RecipeCase<>(id, codec, streamCodec, json);
    }

    private record RecipeCase<T>(
            String id,
            Codec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec,
            String json
    ) {
    }

    private static final class MobSoulTypeRegistryAccess {
        private static com.blakebr0.mysticalagriculture.api.soul.MobSoulType testSoul() {
            return com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry.getInstance()
                    .getMobSoulTypeById(Task3TestRegistries.SOUL_ID);
        }
    }
}
