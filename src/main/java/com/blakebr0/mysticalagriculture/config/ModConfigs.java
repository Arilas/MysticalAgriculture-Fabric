package com.blakebr0.mysticalagriculture.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ModConfigs {
    private static final Logger LOGGER = LoggerFactory.getLogger("Mystical Agriculture");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final ConfigValue<Double> INFERIUM_DROP_CHANCE =
            doubleValue("inferiumDropChance", 0.2, 0.0, 1.0);
    public static final ConfigValue<Integer> INFUSION_CRYSTAL_USES =
            intValue("infusionCrystalUses", 1000, 10, Integer.MAX_VALUE);
    public static final ConfigValue<Integer> GROWTH_ACCELERATOR_COOLDOWN =
            intValue("growthAcceleratorCooldown", 10, 1, Integer.MAX_VALUE);
    public static final ConfigValue<Double> FERTILIZED_ESSENCE_DROP_CHANCE =
            doubleValue("fertilizedEssenceChance", 0.1, 0.0, 1.0);
    public static final ConfigValue<Boolean> SECONDARY_SEED_DROPS =
            booleanValue("secondarySeedDrops", true);
    public static final ConfigValue<Boolean> REQUIRES_EFFECTIVE_FARMLAND =
            booleanValue("requiresEffectiveFarmland", false);
    public static final ConfigValue<Boolean> WITHER_DROPS_ESSENCE =
            booleanValue("witherDropsEssence", true);
    public static final ConfigValue<Boolean> WITHER_DROPS_COGNIZANT =
            booleanValue("witherDropsCognizant", true);
    public static final ConfigValue<Boolean> DRAGON_DROPS_ESSENCE =
            booleanValue("dragonDropsEssence", true);
    public static final ConfigValue<Boolean> DRAGON_DROPS_COGNIZANT =
            booleanValue("dragonDropsCognizant", true);
    public static final ConfigValue<Boolean> ESSENCE_FARMLAND_CONVERSION =
            booleanValue("essenceFarmlandConversion", true);
    public static final ConfigValue<Boolean> SEED_CRAFTING_RECIPES =
            booleanValue("seedCraftingRecipes", false);
    public static final ConfigValue<Boolean> UNBREAKABLE_SUPREMIUM_ARMOR =
            booleanValue("unbreakableSupremiumArmor", false);
    public static final ConfigValue<Boolean> FAKE_PLAYER_WATERING =
            booleanValue("fakePlayerWatering", true);
    public static final ConfigValue<Boolean> AWAKENED_SUPREMIUM_SET_BONUS =
            booleanValue("awakenedSupremiumSetBonus", true);
    public static final ConfigValue<Boolean> GENERATE_PROSPERITY =
            booleanValue("generateProsperityOre", true);
    public static final ConfigValue<Boolean> GENERATE_INFERIUM =
            booleanValue("generateInferiumOre", true);
    public static final ConfigValue<Boolean> GENERATE_SOULSTONE =
            booleanValue("generateSoulstone", true);
    public static final ConfigValue<Double> SOULIUM_ORE_CHANCE =
            doubleValue("souliumOreChance", 0.05, 0.0, 1.0);

    private static final List<ConfigValue<?>> VALUES = List.of(
            INFERIUM_DROP_CHANCE,
            INFUSION_CRYSTAL_USES,
            GROWTH_ACCELERATOR_COOLDOWN,
            FERTILIZED_ESSENCE_DROP_CHANCE,
            SECONDARY_SEED_DROPS,
            REQUIRES_EFFECTIVE_FARMLAND,
            WITHER_DROPS_ESSENCE,
            WITHER_DROPS_COGNIZANT,
            DRAGON_DROPS_ESSENCE,
            DRAGON_DROPS_COGNIZANT,
            ESSENCE_FARMLAND_CONVERSION,
            SEED_CRAFTING_RECIPES,
            UNBREAKABLE_SUPREMIUM_ARMOR,
            FAKE_PLAYER_WATERING,
            AWAKENED_SUPREMIUM_SET_BONUS,
            GENERATE_PROSPERITY,
            GENERATE_INFERIUM,
            GENERATE_SOULSTONE,
            SOULIUM_ORE_CHANCE
    );

    public static void load() {
        load(FabricLoader.getInstance().getConfigDir()
                .resolve("mysticalagriculture.json"));
    }

    static synchronized void load(Path path) {
        resetDefaults();

        try {
            var parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.notExists(path)) {
                writeDefaults(path);
                return;
            }

            JsonObject json;
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            }

            VALUES.forEach(value -> value.read(json));
        } catch (Exception e) {
            resetDefaults();
            LOGGER.error("Could not read Mystical Agriculture config {}; using defaults without overwriting the file", path, e);
        }
    }

    private static void resetDefaults() {
        VALUES.forEach(ConfigValue::reset);
    }

    private static void writeDefaults(Path path) throws IOException {
        var json = new JsonObject();
        VALUES.forEach(value -> value.writeDefault(json));

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
        }
    }

    private static ConfigValue<Boolean> booleanValue(String key, boolean defaultValue) {
        return new ConfigValue<>(
                key,
                defaultValue,
                element -> {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
                        throw new IllegalArgumentException("expected a boolean");
                    }

                    return element.getAsBoolean();
                },
                value -> true
        );
    }

    private static ConfigValue<Integer> intValue(String key, int defaultValue, int minimum, int maximum) {
        return new ConfigValue<>(
                key,
                defaultValue,
                element -> {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                        throw new IllegalArgumentException("expected an integer");
                    }

                    return element.getAsBigDecimal().intValueExact();
                },
                value -> value >= minimum && value <= maximum
        );
    }

    private static ConfigValue<Double> doubleValue(String key, double defaultValue, double minimum, double maximum) {
        return new ConfigValue<>(
                key,
                defaultValue,
                element -> {
                    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                        throw new IllegalArgumentException("expected a number");
                    }

                    return element.getAsDouble();
                },
                value -> Double.isFinite(value) && value >= minimum && value <= maximum
        );
    }

    public static final class ConfigValue<T> implements Supplier<T> {
        private final String key;
        private final T defaultValue;
        private final Function<JsonElement, T> parser;
        private final Predicate<T> validator;
        private T value;

        private ConfigValue(
                String key,
                T defaultValue,
                Function<JsonElement, T> parser,
                Predicate<T> validator
        ) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.parser = parser;
            this.validator = validator;
            this.value = defaultValue;
        }

        @Override
        public T get() {
            return this.value;
        }

        private void read(JsonObject json) {
            if (!json.has(this.key)) {
                return;
            }

            var jsonValue = json.get(this.key);

            try {
                var parsed = this.parser.apply(jsonValue);
                if (!this.validator.test(parsed)) {
                    throw new IllegalArgumentException("value is outside the accepted range");
                }

                this.value = parsed;
            } catch (RuntimeException e) {
                this.reset();
                LOGGER.warn(
                        "Invalid Mystical Agriculture config key '{}' with value {}; using fallback {}",
                        this.key,
                        jsonValue,
                        this.defaultValue
                );
            }
        }

        private void reset() {
            this.value = this.defaultValue;
        }

        private void writeDefault(JsonObject json) {
            json.add(this.key, GSON.toJsonTree(this.defaultValue));
        }
    }
}
