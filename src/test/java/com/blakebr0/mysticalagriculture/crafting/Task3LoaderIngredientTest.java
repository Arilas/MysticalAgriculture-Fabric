package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureDataComponentTypes;
import com.blakebr0.mysticalagriculture.api.components.SoulJarComponent;
import com.blakebr0.mysticalagriculture.crafting.ingredient.CropComponentIngredient;
import com.blakebr0.mysticalagriculture.crafting.ingredient.FilledSoulJarIngredient;
import com.blakebr0.mysticalagriculture.data.recipe.ConditionedRecipeOutput;
import com.blakebr0.mysticalagriculture.data.recipe.CraftingRecipeBuilder;
import com.blakebr0.mysticalagriculture.data.recipe.InfusionRecipeBuilder;
import com.blakebr0.mysticalagriculture.init.ModIngredientTypes;
import com.blakebr0.mysticalagriculture.registry.CropRegistry;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Task3LoaderIngredientTest {
    private static Item soulJar;

    @BeforeAll
    public static void bootstrapRegistriesAndIngredientTypes() {
        Task3TestRegistries.ensureInitialized();
        soulJar = BuiltInRegistries.ITEM.getValueOrThrow(Task3TestRegistries.SOUL_JAR);
        ModIngredientTypes.register();
    }

    @Test
    public void cropComponentUsesProductionCropDerivationAndVanillaIngredientBridge() {
        var ingredient = new CropComponentIngredient(
                Task3TestRegistries.CROP_ID,
                CropComponentIngredient.ComponentType.MATERIAL
        );
        var matching = new ItemStack(net.minecraft.world.item.Items.DIAMOND);
        matching.set(DataComponents.CUSTOM_NAME, Component.literal("required material"));
        var wrong = new ItemStack(net.minecraft.world.item.Items.DIAMOND);
        wrong.set(DataComponents.CUSTOM_NAME, Component.literal("wrong material"));

        assertTrue(ingredient.test(matching));
        assertFalse(ingredient.test(wrong));
        assertEquals(1, ingredient.items().count());
        assertTrue(ingredient.toVanilla().test(matching));
        assertSame(CropComponentIngredient.SERIALIZER, ingredient.getSerializer());
    }

    @Test
    public void cropComponentSerializerMapAndPacketCodecsRoundTrip() {
        var original = new CropComponentIngredient(
                Task3TestRegistries.CROP_ID,
                CropComponentIngredient.ComponentType.MATERIAL
        );
        var expectedJson = JsonParser.parseString("""
                {"crop":"testing:component_crop","component":"material"}
                """);

        var encoded = CropComponentIngredient.SERIALIZER.getCodec()
                .codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        var decoded = CropComponentIngredient.SERIALIZER.getCodec()
                .codec().parse(JsonOps.INSTANCE, expectedJson).getOrThrow();
        assertEquals(expectedJson, encoded);
        assertEquals(original, decoded);

        var buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                Task3TestRegistries.registryAccess()
        );
        CropComponentIngredient.SERIALIZER.getStreamCodec().encode(buffer, original);
        assertEquals(original,
                CropComponentIngredient.SERIALIZER.getStreamCodec().decode(buffer));
        assertEquals(0, buffer.readableBytes());

        var ingredientJson = net.minecraft.world.item.crafting.Ingredient.CODEC
                .encodeStart(
                        RegistryOps.create(JsonOps.INSTANCE, Task3TestRegistries.registryAccess()),
                        original.toVanilla()
                ).getOrThrow();
        assertEquals("mysticalagriculture:crop_component",
                ingredientJson.getAsJsonObject().get("fabric:type").getAsString());
    }

    @Test
    public void filledSoulJarUsesFixedRegistryKeyAndPositiveSoulComponent() {
        var ingredient = new FilledSoulJarIngredient();
        var empty = new ItemStack(soulJar);
        var filled = new ItemStack(soulJar);
        filled.set(
                MysticalAgricultureDataComponentTypes.SOUL_JAR,
                new SoulJarComponent(Task3TestRegistries.SOUL_ID, 0.5)
        );

        assertFalse(ingredient.test(empty));
        assertTrue(ingredient.test(filled));
        assertTrue(ingredient.toVanilla().test(filled));
        assertSame(soulJar, ingredient.items().findFirst().orElseThrow().value());
        assertSame(FilledSoulJarIngredient.SERIALIZER, ingredient.getSerializer());
    }

    @Test
    public void filledSoulJarSerializerMapAndPacketCodecsRoundTrip() {
        var original = new FilledSoulJarIngredient();
        var expectedJson = JsonParser.parseString("{}");

        var encoded = FilledSoulJarIngredient.SERIALIZER.getCodec()
                .codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        var decoded = FilledSoulJarIngredient.SERIALIZER.getCodec()
                .codec().parse(JsonOps.INSTANCE, expectedJson).getOrThrow();
        assertEquals(expectedJson, encoded);
        assertEquals(original, decoded);

        var buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(),
                Task3TestRegistries.registryAccess()
        );
        FilledSoulJarIngredient.SERIALIZER.getStreamCodec().encode(buffer, original);
        assertEquals(original,
                FilledSoulJarIngredient.SERIALIZER.getStreamCodec().decode(buffer));
        assertEquals(0, buffer.readableBytes());

        var ingredientJson = net.minecraft.world.item.crafting.Ingredient.CODEC
                .encodeStart(
                        RegistryOps.create(JsonOps.INSTANCE, Task3TestRegistries.registryAccess()),
                        original.toVanilla()
                ).getOrThrow();
        assertEquals("mysticalagriculture:filled_soul_jar",
                ingredientJson.getAsJsonObject().get("fabric:type").getAsString());
    }

    @Test
    public void tagAwareRecipeBuildersEmitDirectPopulatedTagConditions() {
        var crop = CropRegistry.getInstance().getCropById(Task3TestRegistries.TAG_CROP_ID);
        assertNotNull(crop);

        var crafting = saveConditions(CraftingRecipeBuilder.seed(
                Identifier.parse("testing:crafting"),
                crop
        ));
        var infusion = saveConditions(InfusionRecipeBuilder.seed(
                Identifier.parse("testing:infusion"),
                crop
        ));

        assertDirectPopulatedTag(crafting.getLast());
        assertDirectPopulatedTag(infusion.getLast());
    }

    private static List<ResourceCondition> saveConditions(RecipeBuilder builder) {
        var captured = new AtomicReference<List<ResourceCondition>>();
        builder.save(new ConditionedRecipeOutput() {
            @Override
            public void accept(
                    ResourceKey<Recipe<?>> id,
                    Recipe<?> recipe,
                    List<ResourceCondition> conditions
            ) {
                captured.set(conditions);
            }

            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.advancement();
            }

            @Override
            public void includeRootAdvancement() {
            }
        });
        return captured.get();
    }

    private static void assertDirectPopulatedTag(ResourceCondition condition) {
        var json = ResourceCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition)
                .getOrThrow()
                .getAsJsonObject();
        assertEquals("fabric:tags_populated", json.get("condition").getAsString());
        assertEquals("c:gems/diamond", json.getAsJsonArray("values").get(0).getAsString());
        assertFalse(json.has("value"));
    }
}
