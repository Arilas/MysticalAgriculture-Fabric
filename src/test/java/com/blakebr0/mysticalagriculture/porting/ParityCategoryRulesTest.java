package com.blakebr0.mysticalagriculture.porting;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParityCategoryRulesTest {
    private static final String BASELINE = "cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462";
    private static final String AUDITED_PORT = "14e9e7a58f36ecccd48127fdd88d0d4b745d894f";
    private static final String JAVA_ROOT = "src/main/java/com/blakebr0/mysticalagriculture/";

    private static final Map<String, Integer> EXPECTED_COUNTS = Map.of(
            "build-docs-metadata", 21,
            "registry-api-bootstrap", 40,
            "data-recipes-worldgen", 635,
            "machines-storage-network", 50,
            "gameplay", 49,
            "client", 41,
            "optional-integrations", 17,
            "tests-fixtures", 40
    );

    private static final List<CategoryRule> RULES = List.of(
            rule("build-docs-metadata", ParityCategoryRulesTest::isBuildDocsMetadata),
            rule("registry-api-bootstrap", ParityCategoryRulesTest::isRegistryApiBootstrap),
            rule("data-recipes-worldgen", ParityCategoryRulesTest::isDataRecipesWorldgen),
            rule("machines-storage-network", ParityCategoryRulesTest::isMachinesStorageNetwork),
            rule("gameplay", ParityCategoryRulesTest::isGameplay),
            rule("client", ParityCategoryRulesTest::isClient),
            rule("optional-integrations", path -> path.startsWith(JAVA_ROOT + "compat/")),
            rule("tests-fixtures", ParityCategoryRulesTest::isTestFixture)
    );

    @Test
    void orderedCategoryRulesCoverTheAuditedPortExactlyOnce() throws Exception {
        var root = Path.of(System.getProperty("portingRepoRoot")).toAbsolutePath().normalize();
        var paths = changedPaths(root);
        var counts = new LinkedHashMap<String, Integer>();

        assertEquals(893, paths.size(), "audited port path count changed");

        for (var path : paths) {
            var matches = RULES.stream()
                    .filter(rule -> rule.matches.test(path))
                    .map(CategoryRule::name)
                    .toList();
            assertEquals(1, matches.size(), () -> path + " matched categories " + matches);
            counts.merge(matches.getFirst(), 1, Integer::sum);
        }

        assertEquals(EXPECTED_COUNTS, counts, "parity category totals changed");
    }

    private static boolean isBuildDocsMetadata(String path) {
        return !path.startsWith("src/")
                || path.equals("src/main/resources/fabric.mod.json")
                || path.equals("src/main/resources/mysticalagriculture.accesswidener")
                || path.equals("src/main/resources/mysticalagriculture.mixins.json")
                || path.startsWith("src/main/resources/META-INF/");
    }

    private static boolean isRegistryApiBootstrap(String path) {
        if (path.equals(JAVA_ROOT + "MysticalAgriculture.java")
                || path.startsWith(JAVA_ROOT + "registry/")
                || path.startsWith(JAVA_ROOT + "init/")
                || path.startsWith(JAVA_ROOT + "lib/")
                || path.startsWith(JAVA_ROOT + "config/")) {
            return true;
        }
        return path.startsWith(JAVA_ROOT + "api/")
                && !path.startsWith(JAVA_ROOT + "api/machine/")
                && !path.startsWith(JAVA_ROOT + "api/tinkering/")
                && !path.startsWith(JAVA_ROOT + "api/util/");
    }

    private static boolean isDataRecipesWorldgen(String path) {
        return path.startsWith("src/generated/resources/")
                || path.startsWith("src/main/resources/data/")
                || path.startsWith(JAVA_ROOT + "crafting/")
                || path.startsWith(JAVA_ROOT + "data/")
                || path.startsWith(JAVA_ROOT + "world/")
                || path.equals(JAVA_ROOT + "util/RecipeIngredientCache.java");
    }

    private static boolean isMachinesStorageNetwork(String path) {
        return path.startsWith(JAVA_ROOT + "tileentity/")
                || path.startsWith(JAVA_ROOT + "container/")
                || path.startsWith(JAVA_ROOT + "api/machine/")
                || machineBlock(path)
                || machineHandler(path)
                || machineNetwork(path);
    }

    private static boolean machineBlock(String path) {
        return path.startsWith(JAVA_ROOT + "block/")
                && !path.endsWith("/InfusedFarmlandBlock.java")
                && !path.endsWith("/MysticalCropBlock.java")
                && !path.endsWith("/WitherproofBlock.java")
                && !path.endsWith("/WitherproofGlassBlock.java");
    }

    private static boolean machineHandler(String path) {
        return path.equals(JAVA_ROOT + "handler/MachineItemStorage.java")
                || path.equals(JAVA_ROOT + "handler/ModStorageProviders.java")
                || path.equals(JAVA_ROOT + "handler/RegisterCapabilityHandler.java")
                || path.equals(JAVA_ROOT + "handler/ReloadSyncHandler.java")
                || path.equals(JAVA_ROOT + "handler/TinkerableHandler.java");
    }

    private static boolean machineNetwork(String path) {
        return path.equals(JAVA_ROOT + "network/NetworkHandler.java")
                || path.equals(JAVA_ROOT + "network/payloads/ExperienceCapsulePickupPayload.java")
                || path.equals(JAVA_ROOT + "network/payloads/ReloadIngredientCachePayload.java")
                || path.equals(JAVA_ROOT + "network/payloads/SyncEssenceVesselColorsPayload.java");
    }

    private static boolean isGameplay(String path) {
        return path.startsWith(JAVA_ROOT + "augment/")
                || path.startsWith(JAVA_ROOT + "item/")
                || path.startsWith(JAVA_ROOT + "mixin/")
                || path.startsWith(JAVA_ROOT + "api/tinkering/")
                || path.startsWith(JAVA_ROOT + "api/util/")
                || path.startsWith(JAVA_ROOT + "block/") && !machineBlock(path)
                || path.startsWith(JAVA_ROOT + "handler/") && !machineHandler(path)
                || path.startsWith(JAVA_ROOT + "network/") && !machineNetwork(path);
    }

    private static boolean isClient(String path) {
        return path.equals(JAVA_ROOT + "MysticalAgricultureClient.java")
                || path.startsWith(JAVA_ROOT + "client/")
                || path.equals("src/main/resources/assets/mysticalagriculture/models/item/supremium_staff.json");
    }

    private static boolean isTestFixture(String path) {
        return path.startsWith("src/test/")
                || path.startsWith("src/gametest/")
                || path.startsWith("src/task")
                || path.startsWith("src/apiCompatibility/");
    }

    private static List<String> changedPaths(Path root) throws IOException, InterruptedException {
        var process = new ProcessBuilder(
                "git",
                "diff",
                "--name-only",
                BASELINE,
                AUDITED_PORT,
                "--"
        ).directory(root.toFile()).redirectErrorStream(true).start();
        var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        var exitCode = process.waitFor();

        assertEquals(0, exitCode, "git baseline comparison failed:\n" + output);
        return output.lines().filter(path -> !path.isBlank()).sorted().toList();
    }

    private static CategoryRule rule(String name, Predicate<String> matches) {
        return new CategoryRule(name, matches);
    }

    private record CategoryRule(String name, Predicate<String> matches) {
    }
}
