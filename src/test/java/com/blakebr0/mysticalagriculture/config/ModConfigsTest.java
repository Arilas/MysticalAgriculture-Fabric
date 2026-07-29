package com.blakebr0.mysticalagriculture.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigsTest {
    @TempDir
    Path tempDirectory;

    @Test
    void missingFileLoadsEveryDefaultAndCreatesConfiguration() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");

        ModConfigs.load(path);

        assertAllDefaults();
        assertTrue(Files.isRegularFile(path), "mysticalagriculture.json should be created");

        JsonObject json;
        try (var reader = Files.newBufferedReader(path)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        }

        assertAll(
                () -> assertEquals(0.2, json.get("inferiumDropChance").getAsDouble(), "inferiumDropChance"),
                () -> assertEquals(1000, json.get("infusionCrystalUses").getAsInt(), "infusionCrystalUses"),
                () -> assertEquals(10, json.get("growthAcceleratorCooldown").getAsInt(), "growthAcceleratorCooldown"),
                () -> assertEquals(0.1, json.get("fertilizedEssenceChance").getAsDouble(), "fertilizedEssenceChance"),
                () -> assertTrue(json.get("secondarySeedDrops").getAsBoolean(), "secondarySeedDrops"),
                () -> assertFalse(json.get("requiresEffectiveFarmland").getAsBoolean(), "requiresEffectiveFarmland"),
                () -> assertTrue(json.get("witherDropsEssence").getAsBoolean(), "witherDropsEssence"),
                () -> assertTrue(json.get("witherDropsCognizant").getAsBoolean(), "witherDropsCognizant"),
                () -> assertTrue(json.get("dragonDropsEssence").getAsBoolean(), "dragonDropsEssence"),
                () -> assertTrue(json.get("dragonDropsCognizant").getAsBoolean(), "dragonDropsCognizant"),
                () -> assertTrue(json.get("essenceFarmlandConversion").getAsBoolean(), "essenceFarmlandConversion"),
                () -> assertFalse(json.get("seedCraftingRecipes").getAsBoolean(), "seedCraftingRecipes"),
                () -> assertFalse(json.get("unbreakableSupremiumArmor").getAsBoolean(), "unbreakableSupremiumArmor"),
                () -> assertTrue(json.get("fakePlayerWatering").getAsBoolean(), "fakePlayerWatering"),
                () -> assertTrue(json.get("awakenedSupremiumSetBonus").getAsBoolean(), "awakenedSupremiumSetBonus"),
                () -> assertTrue(json.get("generateProsperityOre").getAsBoolean(), "generateProsperityOre"),
                () -> assertTrue(json.get("generateInferiumOre").getAsBoolean(), "generateInferiumOre"),
                () -> assertTrue(json.get("generateSoulstone").getAsBoolean(), "generateSoulstone"),
                () -> assertEquals(0.05, json.get("souliumOreChance").getAsDouble(), "souliumOreChance")
        );
    }

    @Test
    void numericMinimumAndMaximumValuesAreAccepted() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        Files.writeString(path, """
                {
                  "inferiumDropChance": 0.0,
                  "infusionCrystalUses": 10,
                  "growthAcceleratorCooldown": 1,
                  "fertilizedEssenceChance": 0.0,
                  "souliumOreChance": 0.0
                }
                """);

        ModConfigs.load(path);

        assertAll(
                () -> assertEquals(0.0, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance minimum"),
                () -> assertEquals(10, ModConfigs.INFUSION_CRYSTAL_USES.get(), "infusionCrystalUses minimum"),
                () -> assertEquals(1, ModConfigs.GROWTH_ACCELERATOR_COOLDOWN.get(), "growthAcceleratorCooldown minimum"),
                () -> assertEquals(0.0, ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE.get(), "fertilizedEssenceChance minimum"),
                () -> assertEquals(0.0, ModConfigs.SOULIUM_ORE_CHANCE.get(), "souliumOreChance minimum")
        );

        Files.writeString(path, """
                {
                  "inferiumDropChance": 1.0,
                  "infusionCrystalUses": 2147483647,
                  "growthAcceleratorCooldown": 2147483647,
                  "fertilizedEssenceChance": 1.0,
                  "souliumOreChance": 1.0
                }
                """);

        ModConfigs.load(path);

        assertAll(
                () -> assertEquals(1.0, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance maximum"),
                () -> assertEquals(Integer.MAX_VALUE, ModConfigs.INFUSION_CRYSTAL_USES.get(), "infusionCrystalUses maximum"),
                () -> assertEquals(Integer.MAX_VALUE, ModConfigs.GROWTH_ACCELERATOR_COOLDOWN.get(), "growthAcceleratorCooldown maximum"),
                () -> assertEquals(1.0, ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE.get(), "fertilizedEssenceChance maximum"),
                () -> assertEquals(1.0, ModConfigs.SOULIUM_ORE_CHANCE.get(), "souliumOreChance maximum")
        );
    }

    @Test
    void outOfRangeNumberFallsBackOnlyThatKey() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        Files.writeString(path, """
                {
                  "inferiumDropChance": 0.75,
                  "infusionCrystalUses": 9
                }
                """);

        ModConfigs.load(path);

        assertAll(
                () -> assertEquals(0.75, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance"),
                () -> assertEquals(1000, ModConfigs.INFUSION_CRYSTAL_USES.get(), "infusionCrystalUses")
        );
    }

    @Test
    void wrongJsonTypeFallsBackOnlyThatKey() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        Files.writeString(path, """
                {
                  "secondarySeedDrops": "yes",
                  "requiresEffectiveFarmland": true
                }
                """);

        ModConfigs.load(path);

        assertAll(
                () -> assertTrue(ModConfigs.SECONDARY_SEED_DROPS.get(), "secondarySeedDrops"),
                () -> assertTrue(ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get(), "requiresEffectiveFarmland")
        );
    }

    @Test
    void malformedJsonLoadsDefaultsWithoutThrowing() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        Files.writeString(path, "{ definitely not json");

        assertDoesNotThrow(() -> ModConfigs.load(path), "malformed JSON");
        assertAllDefaults();
    }

    @Test
    void unknownKeysAreIgnoredAndValidExistingFileIsNotRewritten() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        var contents = """
                {
                  "inferiumDropChance": 0.6,
                  "futureSetting": {
                    "preserve": true
                  }
                }
                """;
        Files.writeString(path, contents);

        ModConfigs.load(path);

        assertEquals(0.6, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance");
        assertEquals(contents, Files.readString(path), "valid existing mysticalagriculture.json");
    }

    @Test
    void loadingTwiceResetsOmittedValuesToDefaults() throws IOException {
        var path = this.tempDirectory.resolve("mysticalagriculture.json");
        Files.writeString(path, """
                {
                  "inferiumDropChance": 0.9,
                  "secondarySeedDrops": false
                }
                """);
        ModConfigs.load(path);

        Files.writeString(path, """
                {
                  "requiresEffectiveFarmland": true
                }
                """);
        ModConfigs.load(path);

        assertAll(
                () -> assertEquals(0.2, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance"),
                () -> assertTrue(ModConfigs.SECONDARY_SEED_DROPS.get(), "secondarySeedDrops"),
                () -> assertTrue(ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get(), "requiresEffectiveFarmland")
        );
    }

    private static void assertAllDefaults() {
        assertAll(
                () -> assertEquals(0.2, ModConfigs.INFERIUM_DROP_CHANCE.get(), "inferiumDropChance"),
                () -> assertEquals(1000, ModConfigs.INFUSION_CRYSTAL_USES.get(), "infusionCrystalUses"),
                () -> assertEquals(10, ModConfigs.GROWTH_ACCELERATOR_COOLDOWN.get(), "growthAcceleratorCooldown"),
                () -> assertEquals(0.1, ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE.get(), "fertilizedEssenceChance"),
                () -> assertTrue(ModConfigs.SECONDARY_SEED_DROPS.get(), "secondarySeedDrops"),
                () -> assertFalse(ModConfigs.REQUIRES_EFFECTIVE_FARMLAND.get(), "requiresEffectiveFarmland"),
                () -> assertTrue(ModConfigs.WITHER_DROPS_ESSENCE.get(), "witherDropsEssence"),
                () -> assertTrue(ModConfigs.WITHER_DROPS_COGNIZANT.get(), "witherDropsCognizant"),
                () -> assertTrue(ModConfigs.DRAGON_DROPS_ESSENCE.get(), "dragonDropsEssence"),
                () -> assertTrue(ModConfigs.DRAGON_DROPS_COGNIZANT.get(), "dragonDropsCognizant"),
                () -> assertTrue(ModConfigs.ESSENCE_FARMLAND_CONVERSION.get(), "essenceFarmlandConversion"),
                () -> assertFalse(ModConfigs.SEED_CRAFTING_RECIPES.get(), "seedCraftingRecipes"),
                () -> assertFalse(ModConfigs.UNBREAKABLE_SUPREMIUM_ARMOR.get(), "unbreakableSupremiumArmor"),
                () -> assertTrue(ModConfigs.FAKE_PLAYER_WATERING.get(), "fakePlayerWatering"),
                () -> assertTrue(ModConfigs.AWAKENED_SUPREMIUM_SET_BONUS.get(), "awakenedSupremiumSetBonus"),
                () -> assertTrue(ModConfigs.GENERATE_PROSPERITY.get(), "generateProsperityOre"),
                () -> assertTrue(ModConfigs.GENERATE_INFERIUM.get(), "generateInferiumOre"),
                () -> assertTrue(ModConfigs.GENERATE_SOULSTONE.get(), "generateSoulstone"),
                () -> assertEquals(0.05, ModConfigs.SOULIUM_ORE_CHANCE.get(), "souliumOreChance")
        );
    }
}
