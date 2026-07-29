package com.blakebr0.mysticalagriculture.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntegrationArtifactTest {
    private static final List<String> JEI_CLASSES = List.of(
            "com/blakebr0/mysticalagriculture/compat/jei/JeiCompat.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/AwakeningCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/CruxCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/EnchanterCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/InfusionCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/OreInfuserCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/ReprocessorCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/SoulExtractorCategory.class",
            "com/blakebr0/mysticalagriculture/compat/jei/category/SouliumSpawnerCategory.class"
    );
    private static final List<String> JEI_TEXTURES = List.of(
            "assets/mysticalagriculture/textures/jei/infusion.png",
            "assets/mysticalagriculture/textures/jei/crux.png",
            "assets/mysticalagriculture/textures/jei/enchanter.png",
            "assets/mysticalagriculture/textures/jei/ore_infuser.png",
            "assets/mysticalagriculture/textures/jei/reprocessor.png",
            "assets/mysticalagriculture/textures/jei/soulium_spawner.png"
    );
    private static final Set<String> REMOVED_CLASSES = Set.of(
            "com/blakebr0/mysticalagriculture/compat/TOPCompat.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/AwakeningCrafting.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/EnchanterCrafting.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/InfusionCrafting.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/ReprocessorCrafting.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/SoulExtractorCrafting.class",
            "com/blakebr0/mysticalagriculture/compat/crafttweaker/SouliumSpawnerCrafting.class"
    );

    @Test
    void releaseMetadataDiscoversOptionalPluginsWithoutRequiringThem() throws IOException {
        try (var jar = artifact()) {
            var metadata = readJson(jar, "fabric.mod.json");
            var entrypoints = metadata.getAsJsonObject("entrypoints");
            var depends = metadata.getAsJsonObject("depends");
            var suggests = metadata.getAsJsonObject("suggests");

            assertEquals(
                    "com.blakebr0.mysticalagriculture.compat.jei.JeiCompat",
                    entrypoints.getAsJsonArray("jei_mod_plugin").get(0).getAsString()
            );
            assertEquals(
                    "com.blakebr0.mysticalagriculture.compat.JadeCompat",
                    entrypoints.getAsJsonArray("jade").get(0).getAsString()
            );
            assertTrue(suggests.has("jei"));
            assertTrue(suggests.has("jade"));
            assertFalse(depends.has("jei"));
            assertFalse(depends.has("jade"));
        }
    }

    @Test
    void releaseArtifactShipsIntegrationsAndResourcesButDoesNotVendorOptionalApis() throws IOException {
        try (var jar = artifact()) {
            JEI_CLASSES.forEach(name -> assertNotNull(jar.getEntry(name), name));
            JEI_TEXTURES.forEach(name -> assertNotNull(jar.getEntry(name), name));
            assertNotNull(jar.getEntry("com/blakebr0/mysticalagriculture/compat/JadeCompat.class"));

            var entries = jar.stream().map(entry -> entry.getName()).toList();
            assertTrue(entries.stream().noneMatch(name -> name.startsWith("mezz/jei/")));
            assertTrue(entries.stream().noneMatch(name -> name.startsWith("snownee/jade/")));
            REMOVED_CLASSES.forEach(name -> assertFalse(entries.contains(name), name));
        }
    }

    @Test
    void commonEntrypointsDoNotLinkOptionalApisWhenOptionalModsAreAbsent() throws IOException {
        try (var jar = artifact()) {
            assertDoesNotContain(jar, "com/blakebr0/mysticalagriculture/MysticalAgriculture.class", "mezz/jei");
            assertDoesNotContain(jar, "com/blakebr0/mysticalagriculture/MysticalAgriculture.class", "snownee/jade");
            assertDoesNotContain(jar, "com/blakebr0/mysticalagriculture/MysticalAgricultureClient.class", "mezz/jei");
            assertDoesNotContain(jar, "com/blakebr0/mysticalagriculture/MysticalAgricultureClient.class", "snownee/jade");
        }
    }

    private static void assertDoesNotContain(JarFile jar, String entryName, String forbidden) throws IOException {
        var entry = jar.getJarEntry(entryName);
        assertNotNull(entry, entryName);
        try (var input = jar.getInputStream(entry)) {
            var contents = new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
            assertFalse(contents.contains(forbidden), entryName + " links " + forbidden);
        }
    }

    private static JsonObject readJson(JarFile jar, String name) throws IOException {
        var entry = jar.getJarEntry(name);
        assertNotNull(entry, name);
        try (var reader = new java.io.InputStreamReader(jar.getInputStream(entry), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    private static JarFile artifact() throws IOException {
        return new JarFile(Path.of(System.getProperty("task7Artifact")).toFile());
    }
}
