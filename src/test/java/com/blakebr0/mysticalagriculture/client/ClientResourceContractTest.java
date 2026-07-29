package com.blakebr0.mysticalagriculture.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientResourceContractTest {
    private static final List<String> EQUIPMENT_ASSETS = List.of(
            "inferium",
            "prudentium",
            "tertium",
            "imperium",
            "supremium",
            "awakened_supremium"
    );

    @Test
    public void equipmentAssetsDefineBothRequiredHumanoidLayers() throws IOException {
        var equipment = assets().resolve("equipment");

        for (var name : EQUIPMENT_ASSETS) {
            var root = read(equipment.resolve(name + ".json"));
            var layers = root.getAsJsonObject("layers");

            assertTrue(layers.has("humanoid"), name);
            assertTrue(layers.has("humanoid_leggings"), name);
            assertEquals(
                    "mysticalagriculture:" + name,
                    layers.getAsJsonArray("humanoid").get(0).getAsJsonObject()
                            .get("texture").getAsString(),
                    name
            );
            assertEquals(
                    "mysticalagriculture:" + name,
                    layers.getAsJsonArray("humanoid_leggings").get(0).getAsJsonObject()
                            .get("texture").getAsString(),
                    name
            );
        }
    }

    @Test
    public void itemDefinitionsRetainCustomPropertyAndTintContracts() throws IOException {
        var items = assets().resolve("items");
        var experience = read(items.resolve("experience_capsule.json")).getAsJsonObject("model");
        var soulJar = read(items.resolve("soul_jar.json")).getAsJsonObject("model");
        var infusionCrystal = read(items.resolve("infusion_crystal.json")).getAsJsonObject("model");

        assertEquals("mysticalagriculture:experience_capsule",
                experience.get("property").getAsString());
        assertEquals("mysticalagriculture:soul_jar",
                soulJar.get("property").getAsString());
        assertEquals(9, soulJar.getAsJsonArray("entries").size());
        assertTrue(hasTintType(soulJar, "mysticalagriculture:soul_jar"));
        assertTrue(hasTintType(infusionCrystal, "mysticalagriculture:infusion_crystal"));
    }

    @Test
    public void standaloneStaffModelDefinesItsParticleTexture() throws IOException {
        var staff = read(assets().resolve("models/item/supremium_staff.json"));
        var textures = staff.getAsJsonObject("textures");

        assertEquals("mysticalagriculture:item/gear/supremium_staff",
                textures.get("particle").getAsString());
    }

    private static boolean hasTintType(JsonElement element, String type) {
        if (element.isJsonObject()) {
            var object = element.getAsJsonObject();
            if (object.has("type") && object.get("type").isJsonPrimitive()
                    && type.equals(object.get("type").getAsString())) {
                return true;
            }

            return object.entrySet().stream()
                    .anyMatch(entry -> hasTintType(entry.getValue(), type));
        }

        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) {
                if (hasTintType(child, type)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static JsonObject read(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static Path assets() {
        return Path.of(System.getProperty("task6AssetsDir"));
    }
}
