package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalagriculture.crafting.condition.CropHasMaterialCondition;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceConditionTest {
    private static RegistryOps.RegistryInfoLookup registryInfo;

    @BeforeAll
    static void bootstrapRegistries() {
        Task3TestRegistries.ensureInitialized();
        registryInfo = registryInfo(Task3TestRegistries.lookup());
    }

    @Test
    void cropAugmentAndMaterialConditionsUseStableIdsAndLiteralFields() {
        assertCondition("""
                {"condition":"mysticalagriculture:crop_enabled","crop":"testing:component_crop"}
                """, true);
        assertCondition("""
                {"condition":"mysticalagriculture:crop_has_material","crop":"testing:component_crop"}
                """, true);
        assertCondition("""
                {"condition":"mysticalagriculture:augment_enabled","augment":"testing:enabled_augment"}
                """, true);
        assertCondition("""
                {"condition":"mysticalagriculture:crop_enabled","crop":"testing:missing_crop"}
                """, false);
    }

    @Test
    void seedCraftingConditionReadsTheCurrentTypedConfiguration(@TempDir Path directory) throws IOException {
        var enabled = directory.resolve("enabled.json");
        Files.writeString(enabled, "{\"seedCraftingRecipes\":true}");
        loadConfig(enabled);
        assertCondition("""
                {"condition":"mysticalagriculture:seed_crafting_recipes_enabled"}
                """, true);

        var disabled = directory.resolve("disabled.json");
        Files.writeString(disabled, "{\"seedCraftingRecipes\":false}");
        loadConfig(disabled);
        assertCondition("""
                {"condition":"mysticalagriculture:seed_crafting_recipes_enabled"}
                """, false);
    }

    @Test
    void fabricTagPopulatedAndNegatedConditionsRoundTripAndEvaluate() {
        var tag = TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.parse("c:gems/diamond")
        );
        var lookup = taggedItemLookup(tag);
        var populated = ResourceConditions.tagsPopulated(tag);
        var negated = ResourceConditions.not(populated);
        var expected = JsonParser.parseString("""
                {
                  "condition": "fabric:not",
                  "value": {
                    "condition": "fabric:tags_populated",
                    "registry": "minecraft:item",
                    "values": ["c:gems/diamond"]
                  }
                }
                """);

        var encoded = ResourceCondition.CODEC.encodeStart(JsonOps.INSTANCE, negated).getOrThrow();
        var decoded = ResourceCondition.CODEC.parse(JsonOps.INSTANCE, expected).getOrThrow();

        assertEquals(expected, encoded);
        assertFalse(decoded.test(lookup));
        assertTrue(populated.test(lookup));
    }

    @Test
    void migratedTagRecipeLoadsWhenItsTagIsPopulatedAndNotWhenMissing() throws IOException {
        var recipe = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/mysticalagriculture/recipe/essence/gems/ruby.json"
        ))).getAsJsonObject();
        var serializedCondition = recipe.getAsJsonArray("fabric:load_conditions").get(1);
        var condition = ResourceCondition.CODEC.parse(JsonOps.INSTANCE, serializedCondition).getOrThrow();
        var ruby = TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.parse("c:gems/ruby")
        );

        assertTrue(condition.test(taggedItemLookup(ruby)));
        assertFalse(condition.test(emptyItemLookup()));
    }

    @Test
    void cropMaterialAndPopulatedTagConditionsAgreeForTagBackedCrops() {
        var tag = TagKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.parse("c:gems/diamond")
        );
        var condition = ResourceConditions.and(
                new CropHasMaterialCondition(Task3TestRegistries.TAG_CROP_ID),
                ResourceConditions.tagsPopulated(tag)
        );

        assertTrue(condition.test(taggedItemLookup(tag)));
        assertFalse(condition.test(emptyItemLookup()));
    }

    private static void assertCondition(String json, boolean expected) {
        var expectedJson = JsonParser.parseString(json);
        var condition = ResourceCondition.CODEC.parse(JsonOps.INSTANCE, expectedJson).getOrThrow();

        assertEquals(expectedJson,
                ResourceCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow());
        assertEquals(expected, condition.test(registryInfo));
    }

    private static void loadConfig(Path path) {
        try {
            var load = ModConfigs.class.getDeclaredMethod("load", Path.class);
            load.setAccessible(true);
            load.invoke(null, path);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new AssertionError("Could not load the isolated test configuration", exception);
        }
    }

    private static RegistryOps.RegistryInfoLookup registryInfo(
            net.minecraft.core.HolderLookup.Provider provider
    ) {
        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(
                    ResourceKey<? extends Registry<? extends T>> registryKey
            ) {
                return provider.lookup(registryKey).map(RegistryOps.RegistryInfo::fromRegistryLookup);
            }
        };
    }

    private static RegistryOps.RegistryInfoLookup taggedItemLookup(TagKey<Item> tag) {
        var itemRegistry = new MappedRegistry<Item>(
                net.minecraft.core.registries.Registries.ITEM,
                Lifecycle.stable()
        );
        var diamondKey = ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.parse("testing:diamond")
        );
        Holder.Reference<Item> diamond = itemRegistry.register(
                diamondKey,
                Items.DIAMOND,
                RegistrationInfo.BUILT_IN
        );
        itemRegistry.bindTags(Map.of(tag, List.of(diamond)));
        itemRegistry.freeze();
        return registryInfo(new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(
                List.of(itemRegistry)
        ).freeze());
    }

    private static RegistryOps.RegistryInfoLookup emptyItemLookup() {
        var itemRegistry = new MappedRegistry<Item>(
                net.minecraft.core.registries.Registries.ITEM,
                Lifecycle.stable()
        );
        itemRegistry.freeze();
        return registryInfo(new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(
                List.of(itemRegistry)
        ).freeze());
    }
}
