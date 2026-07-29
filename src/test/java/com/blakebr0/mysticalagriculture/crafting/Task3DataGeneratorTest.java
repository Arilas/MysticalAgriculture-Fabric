package com.blakebr0.mysticalagriculture.crafting;

import com.blakebr0.mysticalagriculture.data.ModDataGenerators;
import com.blakebr0.mysticalagriculture.registry.Task3TestRegistries;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Task3DataGeneratorTest {
    @Test
    public void actualEntrypointRunsEveryProviderAgainstRealRegistries(@TempDir Path output) throws Exception {
        Task3TestRegistries.ensureInitialized();
        var generator = new FabricDataGenerator(
                output,
                modContainer(output),
                true,
                CompletableFuture.completedFuture(Task3TestRegistries.lookup())
        );

        new ModDataGenerators().onInitializeDataGenerator(generator);
        generator.run();

        var componentRecipe = output.resolve(
                "data/mysticalagriculture/recipe/seed/reprocessor/component_crop.json"
        );
        var tagRecipe = output.resolve(
                "data/mysticalagriculture/recipe/seed/reprocessor/tag_crop.json"
        );
        var blockTag = output.resolve(
                "data/mysticalagriculture/tags/block/crops.json"
        );
        var itemTag = output.resolve(
                "data/mysticalagriculture/tags/item/essences.json"
        );
        var blockState = output.resolve("assets/minecraft/blockstates/wheat.json");
        var itemModel = output.resolve("assets/minecraft/models/item/redstone.json");

        for (var path : List.of(componentRecipe, tagRecipe, blockTag, itemTag, blockState, itemModel)) {
            assertTrue(Files.isRegularFile(path), () -> "missing actual provider output " + path);
        }

        var recipe = JsonParser.parseString(Files.readString(componentRecipe)).getAsJsonObject();
        assertEquals("mysticalagriculture:reprocessor", recipe.get("type").getAsString());
        var conditions = recipe.getAsJsonArray("fabric:load_conditions");
        assertEquals("mysticalagriculture:crop_enabled",
                conditions.get(0).getAsJsonObject().get("condition").getAsString());

        var populatedTagRecipe = JsonParser.parseString(Files.readString(tagRecipe)).getAsJsonObject();
        assertEquals("testing:tag_crop",
                populatedTagRecipe.getAsJsonArray("fabric:load_conditions")
                        .get(0).getAsJsonObject().get("crop").getAsString());
    }

    private static ModContainer modContainer(Path root) {
        var metadata = (ModMetadata) Proxy.newProxyInstance(
                Task3DataGeneratorTest.class.getClassLoader(),
                new Class<?>[]{ModMetadata.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getId" -> "mysticalagriculture";
                    case "getName" -> "Mystical Agriculture";
                    case "getType" -> "fabric";
                    case "getVersion" -> Version.parse("1.0.0-test");
                    default -> emptyValue(method.getReturnType());
                }
        );
        return (ModContainer) Proxy.newProxyInstance(
                Task3DataGeneratorTest.class.getClassLoader(),
                new Class<?>[]{ModContainer.class},
                (proxy, method, arguments) -> switch (method.getName()) {
                    case "getMetadata" -> metadata;
                    case "getRoot", "getRootPath" -> root;
                    case "getRootPaths" -> List.of(root);
                    case "getPath" -> root.resolve((String) arguments[0]);
                    case "findPath" -> Optional.of(root.resolve((String) arguments[0]));
                    default -> emptyValue(method.getReturnType());
                }
        );
    }

    private static Object emptyValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (Optional.class.isAssignableFrom(type)) {
            return Optional.empty();
        }
        if (Map.class.isAssignableFrom(type)) {
            return Map.of();
        }
        if (Collection.class.isAssignableFrom(type)) {
            return List.of();
        }
        return null;
    }
}
