# Mystical Agriculture Fabric 26.2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port Mystical Agriculture 9.0.4 from its latest upstream NeoForge 26.1 baseline to a Fabric-only Minecraft 26.2 build with Java 25, preserving gameplay, identifiers, persistence, and upstream mergeability.

**Architecture:** Keep the upstream single-module layout and translate loader boundaries directly to vanilla, Fabric API, Cucumber-Fabric, and Team Reborn Energy. Use direct vanilla registries, Fabric entrypoints and callbacks, a local Cucumber composite build, narrowly scoped mixins only where callbacks are insufficient, and optional Fabric-native JEI and Jade integrations.

**Tech Stack:** Java 25, Gradle 9.6.1, Fabric Loom 1.17.12, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2, Minecraft 26.2 official Mojang mappings, Cucumber-Fabric 26.2-9.0.5+fabric.1, Team Reborn Energy 5.0.0, JUnit 5.12.2, JEI 30.14.0.90, Jade Fabric 26.2.9.

## Global Constraints

- Minecraft version is exactly `26.2`.
- Java toolchain version is exactly `25`.
- Fabric Loader version is `0.19.3`.
- Fabric API version is `0.155.2+26.2`.
- Fabric Loom version is `1.17.12`.
- Cucumber-Fabric version is `26.2-9.0.5+fabric.1`.
- The required Cucumber source baseline is commit `598ee054d6d6826b8c150d32e7bf46ca412d0ff1`.
- The local Cucumber checkout defaults to `../Cucumber-Fabric` and is overridable with Gradle property `cucumber_project_path`.
- Cucumber commit `598ee054d6d6826b8c150d32e7bf46ca412d0ff1` is currently one commit ahead of its remote branch. Push it or deliberately change and reverify the baseline before CI or release relies on it.
- Repository name and archive base name are `MysticalAgriculture-Fabric`.
- The first Fabric mod version is `9.0.4+fabric.1`; the complete Gradle version is `26.2-9.0.4+fabric.1`.
- Mod ID remains `mysticalagriculture`.
- Java package root remains `com.blakebr0.mysticalagriculture`.
- All existing registry IDs, resource paths, recipe IDs, tags, data component IDs, payload IDs, and persisted keys remain stable.
- JEI and Jade are optional integrations. Neither may become a required runtime dependency or load when absent.
- The One Probe, CraftTweaker, and executable Patchouli integration are excluded from the first port.
- Do not add Architectury, a cross-loader module split, a NeoForge compatibility layer, or local imitations of NeoForge deferred registers, event buses, capabilities, or annotation scanning.
- Keep `26.1` as a clean upstream-tracking branch. Implement only on `fabric/26.2`, and merge future `upstream/26.1` changes into it without rewriting ancestry.
- Preserve the user's untracked `.vscode/` directory and unrelated worktree changes.
- Tasks below are deliberately medium-to-large subsystem slices. Do not split mechanical one-file changes into separate tasks or commits.

## Working and Verification Discipline

- Start each task by comparing its files with upstream commit `cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462`.
- Write the focused test first whenever the behavior can be isolated without a running game. For registry bootstrapping, rendering, and lifecycle behavior, define the failure-producing GameTest or smoke procedure before implementation.
- Use `rg` after each subsystem to eliminate live NeoForge imports from that scope.
- Direct Loom cutover creates one intentional red interval across Tasks 1–5: Gradle configures and resources process, but whole-project Java/test tasks can remain blocked by not-yet-ported packages. An early task is acceptable only when `./gradlew compileJava` has no errors in that task's owned files and the remaining errors are confined to later tasks. Record the remaining file list in the task handoff; do not add a temporary NeoForge dependency, suppress, exclude, move, or stub those sources merely to obtain a green build.
- Task-local JUnit and GameTest selectors in Tasks 1–5 define the required red/green behavior. Run them as soon as whole-project compilation reaches them, and rerun every selector at Task 6's mandatory green checkpoint. A selector blocked by an unrelated later-task compile error is not considered passing.
- A repository-wide clean `compileJava` is mandatory at Task 6 and remains mandatory thereafter.
- Do not weaken tests, mixin requirements, validation, or server authority to make a check pass.
- At each commit boundary, run `git diff --check`, review `git diff --stat`, and confirm `.vscode/` is not staged.

---

### Task 1: Establish the Fabric build, metadata, entrypoints, and typed configuration

This is the foundation slice. It replaces the build and startup contract, creates a Fabric-safe common/client split, and ports configuration without changing the gameplay classes that consume the values.

**Files:**

- Modify: `build.gradle`
- Modify: `gradle.properties`
- Modify: `settings.gradle`
- Modify: `.gitignore`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/MysticalAgriculture.java`
- Create: `src/main/java/com/blakebr0/mysticalagriculture/MysticalAgricultureClient.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/config/ModConfigs.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/config/ModFeatureFlags.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/MysticalAgricultureAPI.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/MysticalAgricultureConfigValues.java`
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/mysticalagriculture.mixins.json`
- Create: `src/main/resources/mysticalagriculture.accesswidener`
- Delete: `src/main/resources/META-INF/neoforge.mods.toml`
- Delete: `src/main/resources/META-INF/accesstransformer.cfg`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/config/ModConfigsTest.java`

- [ ] **1.1 Add failing configuration tests.**

  Cover all nineteen upstream keys through a temporary-directory overload of the configuration loader:

  - a missing file loads every documented default and creates `mysticalagriculture.json`;
  - minimum and maximum numeric values are accepted;
  - an out-of-range number falls back only that key;
  - a wrong JSON type falls back only that key;
  - malformed JSON loads defaults without throwing;
  - unknown keys are ignored and a valid existing file is not rewritten;
  - loading twice resets omitted values to defaults rather than retaining stale in-memory values.

  Use JUnit Jupiter from the JUnit BOM and make the assertions name the configuration key. Run:

  ```bash
  ./gradlew test --tests com.blakebr0.mysticalagriculture.config.ModConfigsTest
  ```

  Expected initially: test compilation or assertions fail because `ModConfigSpec` and the testable JSON loader still exist only in the NeoForge form.

- [ ] **1.2 Replace ModDevGradle with Loom and configure the composite dependency.**

  In `settings.gradle`, retain Foojay, add Fabric's Maven to plugin management, set `rootProject.name = "MysticalAgriculture-Fabric"`, and resolve the Cucumber checkout:

  ```groovy
  def cucumberProjectPath = providers.gradleProperty("cucumber_project_path")
          .getOrElse("../Cucumber-Fabric")
  def cucumberDirectory = file(cucumberProjectPath)

  if (!new File(cucumberDirectory, "settings.gradle").isFile()) {
      throw new GradleException(
              "Cucumber-Fabric was not found at '${cucumberDirectory}'. " +
              "Clone it there or set -Pcucumber_project_path=/absolute/path/to/Cucumber-Fabric."
      )
  }

  includeBuild(cucumberDirectory) {
      dependencySubstitution {
          substitute module("com.blakebr0.cucumber:Cucumber-Fabric") using project(":")
      }
  }
  ```

  In `gradle.properties`, pin:

  ```properties
  minecraft_version=26.2
  fabric_loader_version=0.19.3
  fabric_api_version=0.155.2+26.2
  mod_version=9.0.4+fabric.1
  maven_group=com.blakebr0.mysticalagriculture
  archives_base_name=MysticalAgriculture-Fabric
  cucumber_version=26.2-9.0.5+fabric.1
  energy_version=5.0.0
  jei_version=30.14.0.90
  jade_version=8347273
  ```

  In `build.gradle`, use `net.fabricmc.fabric-loom` `1.17.12`, `java-library`, and `maven-publish`; set the Java 25 toolchain; use official Mojang mappings; add Loader, Fabric API, and the composite-substituted Cucumber module; keep sources/API JARs; and remove NeoForge, Patchouli, CraftTweaker, TOP, and Blake's private publishing target.

  Configure tests with:

  ```groovy
  testImplementation platform("org.junit:junit-bom:5.12.2")
  testImplementation "org.junit.jupiter:junit-jupiter"
  testRuntimeOnly "org.junit.platform:junit-platform-launcher"

  tasks.withType(Test).configureEach {
      useJUnitPlatform()
  }
  ```

  Add Fabric data generation:

  ```groovy
  fabricApi {
      configureDataGeneration {
          modId = "mysticalagriculture"
          outputDirectory = file("src/generated/resources")
          createRunConfiguration = true
      }
      configureTests {
          createSourceSet = true
          modId = "mysticalagriculture-gametest"
          enableGameTests = true
          enableClientGameTests = false
      }
  }
  ```

  Keep `src/generated/resources` in the main resources and exclude only `.cache`.

- [ ] **1.3 Replace NeoForge metadata and establish environment-safe entrypoints.**

  `fabric.mod.json` must declare:

  - common entrypoint `com.blakebr0.mysticalagriculture.MysticalAgriculture`;
  - client entrypoint `com.blakebr0.mysticalagriculture.MysticalAgricultureClient`;
  - data-generation entrypoint `com.blakebr0.mysticalagriculture.data.ModDataGenerators`;
  - JEI entrypoint `com.blakebr0.mysticalagriculture.compat.jei.JeiCompat`;
  - no self-referential plug-in entrypoint: Task 2 registers the core plug-in directly, while external add-ons use `mysticalagriculture:plugin`;
  - the common mixin configuration;
  - access widener;
  - exact required dependency versions from Global Constraints;
  - `jei` and `jade` under `suggests`;
  - the unofficial Fabric fork name, source, issue tracker, authorship, and MIT license.

  Keep common initialization free of `net.minecraft.client` imports and move all current client registration calls to `MysticalAgricultureClient`.

- [ ] **1.4 Implement the typed JSON configuration.**

  Replace `ModConfigSpec` values with a small `ConfigValue<T>` implementation that implements `Supplier<T>` so existing Cucumber `FeatureFlag.create(Identifier, Supplier<Boolean>)` calls remain direct. Each value owns its key, default, parser, validator, and current value.

  Implement:

  ```java
  public static void load() {
      load(FabricLoader.getInstance().getConfigDir()
              .resolve("mysticalagriculture.json"));
  }

  static void load(Path path) {
      resetDefaults();
      // Parse one JsonObject. Validate each known key independently.
      // Write defaults only when the file does not exist.
  }
  ```

  Preserve the exact defaults and ranges in the approved design. Replace `ModList` checks with `FabricLoader.isModLoaded`, but remove `isTheOneProbeInstalled()` because TOP is excluded. Keep the public `MysticalAgricultureConfigValues` API loader-neutral and typed as primitive suppliers.

- [ ] **1.5 Define deterministic common/client startup.**

  Make `MysticalAgriculture` implement `ModInitializer` and expose one `onInitialize()` sequence. At this task it may call later-task `register()` methods only after those methods exist; do not retain an event-bus abstraction. `MysticalAgricultureClient` implements `ClientModInitializer` and contains no common registration.

  The common sequence must be visibly ordered as:

  1. configuration;
  2. API references;
  3. plug-in discovery;
  4. content collection and vanilla registration;
  5. recipe/world/network/storage registration;
  6. dynamic registry finalization;
  7. callbacks and reload listeners.

- [ ] **1.6 Verify foundation behavior and commit.**

  Run:

  ```bash
  ./gradlew help
  ./gradlew processResources
  ./gradlew test --tests com.blakebr0.mysticalagriculture.config.ModConfigsTest
  jq . build/resources/main/fabric.mod.json
  ./gradlew compileJava
  git diff --check
  ```

  `help`, `processResources`, and the metadata parse must pass. The test and compile commands must either pass or report errors only in files owned by Tasks 2–7; record that remaining list for the next task.

  Also rename the sibling Cucumber checkout temporarily and confirm `./gradlew help` fails with the actionable `cucumber_project_path` message, then restore it without changing either repository.

  Commit:

  ```bash
  git add build.gradle gradle.properties settings.gradle .gitignore src/main src/test
  git commit -m "build: establish Fabric 26.2 foundation"
  ```

---

### Task 2: Port direct registries, plug-in discovery, and the public API

This slice replaces deferred holders and annotation scanning with deterministic Fabric entrypoints and vanilla registry values. It establishes the content graph that recipes, machines, and clients consume.

**Files:**

- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/IMysticalAgriculturePlugin.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/MysticalAgriculturePlugin.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/MysticalAgricultureDataComponentTypes.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/tinkering/Augment.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/registry/PluginRegistry.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/registry/CropRegistry.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/registry/AugmentRegistry.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/registry/MobSoulTypeRegistry.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModCorePlugin.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModCrops.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModAugments.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModMobSoulTypes.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModArmorMaterials.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/lib/ModToolMaterials.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModBlocks.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModItems.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModCreativeModeTabs.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModDataComponentTypes.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModTileEntities.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModMenuTypes.java`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/registry/PluginRegistryTest.java`
- Create: `src/apiCompatibility/java/com/blakebr0/mysticalagriculture/api/ApiCompatibility.java`

- [ ] **2.1 Add failing plug-in registry and API compatibility checks.**

  Test that:

  - the core plug-in is always first;
  - external entrypoints retain Fabric Loader's deterministic order;
  - a duplicate crop, augment, mob soul, block, or item ID throws and names both the ID and source mod;
  - an entrypoint exception names its mod and aborts before finalization;
  - registration after finalization fails;
  - API constants expose vanilla/Fabric types and compile without any `net.neoforged` package.

  The API compatibility source set imports representative crop, augment, soul, config, component, and recipe API types from a Fabric consumer perspective.

- [ ] **2.2 Replace annotation scanning with `mysticalagriculture:plugin` entrypoints.**

  Deprecate `@MysticalAgriculturePlugin` for source compatibility but make it behaviorally inert. In `PluginRegistry`, register `ModCorePlugin` explicitly and then load:

  ```java
  FabricLoader.getInstance()
          .getEntrypointContainers("mysticalagriculture:plugin",
                  IMysticalAgriculturePlugin.class)
  ```

  Capture `container.getProvider().getMetadata().getId()` as the source mod. Wrap failures with that ID and the entrypoint definition. Never continue after a partially failed plug-in.

  Document the add-on metadata contract in the interface Javadoc:

  ```json
  {
    "entrypoints": {
      "mysticalagriculture:plugin": [
        "example.addon.ExampleMysticalAgriculturePlugin"
      ]
    }
  }
  ```

- [ ] **2.3 Convert fixed registration to direct registry values.**

  Replace each `DeferredRegister`, `DeferredHolder`, and `DeferredBlock` with actual values returned by `Registry.register`. Use small local helpers scoped by registry type; do not create a generic NeoForge-shaped wrapper.

  Required order:

  1. collect plug-in crops, augments, and mob souls;
  2. register blocks;
  3. register block items and normal items;
  4. register data components;
  5. register block entity types and menus;
  6. create the creative tab and populate it using Fabric's item-group events;
  7. finalize public dynamic registries.

  Preserve every upstream identifier exactly. Add explicit duplicate guards before vanilla registration so the error includes the contributing plug-in rather than only a generic frozen-registry error.

- [ ] **2.4 Remove holder leakage from the public API.**

  Convert `MysticalAgricultureDataComponentTypes` members to direct `DataComponentType<?>` values. Change `Augment` item association from a NeoForge holder to an `Identifier`/`ResourceKey<Item>` and resolve it from `BuiltInRegistries.ITEM` only when requested. Keep method and field names stable where return types can remain loader-neutral.

  Initialize `MysticalAgricultureAPI` through an explicit package-private/static bootstrap method instead of reflection. Freeze its references exactly once and throw on double initialization.

- [ ] **2.5 Verify registrations and commit.**

  Run:

  ```bash
  ./gradlew test --tests com.blakebr0.mysticalagriculture.registry.PluginRegistryTest
  ./gradlew compileApiCompatibilityJava
  ./gradlew compileJava
  rg -n "DeferredRegister|DeferredHolder|DeferredBlock|ModList|getScanData|net\\.neoforged" \
    src/main/java/com/blakebr0/mysticalagriculture/api \
    src/main/java/com/blakebr0/mysticalagriculture/registry \
    src/main/java/com/blakebr0/mysticalagriculture/init/ModBlocks.java \
    src/main/java/com/blakebr0/mysticalagriculture/init/ModItems.java \
    src/main/java/com/blakebr0/mysticalagriculture/init/ModCreativeModeTabs.java \
    src/main/java/com/blakebr0/mysticalagriculture/init/ModDataComponentTypes.java
  git diff --check
  ```

  `rg` must return no live matches in the owned scope. If repository compilation still fails, every remaining error must be in a later task's listed files.

  Commit:

  ```bash
  git add src/main/java/com/blakebr0/mysticalagriculture/api \
          src/main/java/com/blakebr0/mysticalagriculture/registry \
          src/main/java/com/blakebr0/mysticalagriculture/lib \
          src/main/java/com/blakebr0/mysticalagriculture/init \
          src/test src/apiCompatibility
  git commit -m "port: register Mystical Agriculture content on Fabric"
  ```

---

### Task 3: Port recipes, ingredients, resource conditions, data generation, and world generation

This slice owns the complete data-driven content pipeline: recipe codecs, Fabric custom ingredients, Fabric resource conditions, dynamic recipe timing, generated assets/data, and Fabric biome modifications.

**Files:**

- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/crafting/IAwakeningRecipe.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/crafting/IEnchanterRecipe.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/crafting/IOreInfusionRecipe.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/crafting/ISouliumSpawnerRecipe.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/lib/LazyIngredient.java`
- Create: `src/main/java/com/blakebr0/mysticalagriculture/api/crafting/IngredientWithCount.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/recipe/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/ingredient/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/condition/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/DynamicRecipeManager.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/EssenceVesselColorManager.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/util/RecipeIngredientCache.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModRecipeTypes.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModRecipeSerializers.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModIngredientTypes.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModConditionSerializers.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/init/ModWorldFeatures.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/init/ModBiomeModifiers.java`
- Create: `src/main/java/com/blakebr0/mysticalagriculture/world/ModWorldGeneration.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/world/feature/SoulstoneFeature.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/world/modifiers/InferiumOreModifier.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/world/modifiers/ProsperityOreModifier.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/world/modifiers/SoulstoneModifier.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/data/ModDataGenerators.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/data/generator/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/data/recipe/*.java`
- Delete: `src/main/resources/data/mysticalagriculture/neoforge/biome_modifier/*.json`
- Modify: `src/main/resources/data/mysticalagriculture/recipe/**/*.json`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/crafting/IngredientWithCountTest.java`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/crafting/RecipeCodecTest.java`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/crafting/ResourceConditionTest.java`

- [ ] **3.1 Add failing codec and condition tests using real registries.**

  Cover:

  - `IngredientWithCount` JSON and network round trips;
  - rejection of counts below one;
  - component-sensitive crop and filled-soul-jar ingredient matching;
  - JSON and network round trips for all nine concrete recipe classes;
  - crop-enabled, crop-has-material, augment-enabled, and seed-crafting conditions;
  - built-in Fabric tag-populated and negated conditions used by generated recipes;
  - preservation of the upstream serializer IDs and JSON field names.

- [ ] **3.2 Implement `IngredientWithCount` as a domain type.**

  Use a record/value class containing a vanilla `Ingredient` and positive `int count`, with `MapCodec`/`Codec` and `StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount>`. Do not use the name or package of NeoForge `SizedIngredient`, and do not make it a general compatibility API.

  Update awakening, enchanter, ore infusion, and soulium spawner interfaces and recipes to consume it. Keep matching semantics count-aware without mutating candidate stacks.

- [ ] **3.3 Port custom ingredients and resource conditions to Fabric.**

  Make `CropComponentIngredient` and `FilledSoulJarIngredient` implement Fabric `CustomIngredient`, each with one `CustomIngredientSerializer` that owns the existing ID, map codec, and packet codec. Register through `CustomIngredientSerializer.register`.

  Make the four conditions implement `ResourceCondition`, give each a `ResourceConditionType<?>`, and register through Fabric resource conditions. Generated JSON must use Fabric's current `fabric:load_conditions` field and must not contain `neoforge:conditions`, NeoForge serializers, or loader-specific tag-empty conditions.

- [ ] **3.4 Port recipe registries and dynamic recipe lifecycle.**

  Register recipe types and serializers directly in vanilla registries. Preserve every serializer codec and payload codec.

  Register `DynamicRecipeManager` on Cucumber-Fabric's `RecipeManagerLoadingEvent` so crop-derived recipes are injected before final indexing. Register `RecipeIngredientCache` and `EssenceVesselColorManager` after `RecipeManagerLoadedEvent`; Task 4 will add player synchronization.

  Avoid reflection into `RecipeManager`. Use Cucumber-Fabric's event data and public mutable/loading contract.

- [ ] **3.5 Convert data generation to `DataGeneratorEntrypoint`.**

  Implement `ModDataGenerators.onInitializeDataGenerator(FabricDataGenerator)` and place existing generators in the server pack. Reuse upstream custom JSON generators when their byte-for-byte schema remains valid; replace only their provider lifecycle and condition serialization.

  Run data generation into `src/generated/resources`, inspect every deletion/addition, and never bulk-accept changes caused only by ordering.

- [ ] **3.6 Replace biome modifiers with Fabric biome modifications.**

  Keep the existing configured/placed feature resources and register their `ResourceKey<PlacedFeature>` values with `BiomeModifications.addFeature`. Use the same biome tag selectors and `GenerationStep.Decoration` stages as upstream. Gate each addition with its exact configuration value:

  - prosperity ore;
  - inferium ore;
  - soulstone and its custom `SoulstoneFeature`;
  - soulium ore behavior controlled by `souliumOreChance`.

  Remove only NeoForge biome modifier code/resources; do not hard-code the full placement definitions in Java.

- [ ] **3.7 Verify the data pipeline and commit.**

  Run:

  ```bash
  ./gradlew test --tests 'com.blakebr0.mysticalagriculture.crafting.*'
  ./gradlew runDatagen
  git diff -- src/generated/resources src/main/resources/data
  ./gradlew compileJava
  rg -n "SizedIngredient|ICondition|ICustomIngredient|BiomeModifier|neoforge:conditions|net\\.neoforged" \
    src/main/java/com/blakebr0/mysticalagriculture/{api/crafting,crafting,data,world} \
    src/main/resources/data/mysticalagriculture \
    src/generated/resources
  git diff --check
  ```

  Commit:

  ```bash
  git add src/main/java/com/blakebr0/mysticalagriculture/api/crafting \
          src/main/java/com/blakebr0/mysticalagriculture/api/lib/LazyIngredient.java \
          src/main/java/com/blakebr0/mysticalagriculture/crafting \
          src/main/java/com/blakebr0/mysticalagriculture/data \
          src/main/java/com/blakebr0/mysticalagriculture/init \
          src/main/java/com/blakebr0/mysticalagriculture/world \
          src/main/resources/data src/generated/resources src/test
  git commit -m "port: migrate recipes and world data to Fabric"
  ```

---

### Task 4: Port machines, menus, item/energy storage, persistence, networking, and reload sync

This is one cohesive stateful subsystem. It preserves server authority and transaction semantics across all machines instead of porting storage, menus, and packets independently.

**Files:**

- Modify: `src/main/java/com/blakebr0/mysticalagriculture/tileentity/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/container/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/container/slot/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/machine/MachineUpgradeItemStackHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/util/AugmentUtils.java`
- Rename: `src/main/java/com/blakebr0/mysticalagriculture/handler/RegisterCapabilityHandler.java` to `src/main/java/com/blakebr0/mysticalagriculture/handler/ModStorageProviders.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/AwakeningAltarBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/AwakeningPedestalBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/EssenceVesselBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/network/NetworkHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/network/payloads/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/crafting/EssenceVesselColorManager.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/util/RecipeIngredientCache.java`
- Create: `src/gametest/java/com/blakebr0/mysticalagriculture/gametest/MachineStorageGameTests.java`
- Create: `src/gametest/java/com/blakebr0/mysticalagriculture/gametest/MachinePersistenceGameTests.java`
- Create: `src/gametest/resources/fabric.mod.json`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/network/PayloadCodecTest.java`
- Modify: `build.gradle`
- Modify: `src/main/resources/fabric.mod.json`

- [ ] **4.1 Add failing transaction, persistence, and payload tests.**

  Use the `gametest` source set created by Task 1 and give its test-only `fabric.mod.json` ID `mysticalagriculture-gametest`. Register both test classes under `fabric-gametest`; their methods are public instance methods annotated with Fabric API's `@GameTest`, accept exactly one `GameTestHelper`, and call `helper.succeed()`. The test source set is not packaged in the release JAR.

  Test representative inventories before applying the implementation:

  - sided insert/extract restrictions and slot limits;
  - output-only slots;
  - committed and aborted outer transactions;
  - nested rollback;
  - exactly one final content-change notification per committed mutation;
  - energy insert/extract/capacity/rollback through `CEnergyStorage`;
  - representative save/load round trips for a powered processor, an altar, an essence vessel, and the soulium spawner;
  - payload codec round trips and rejection of an invalid AOE offset.

- [ ] **4.2 Register Fabric item and energy providers.**

  In `ModStorageProviders.register()`:

  - register `ItemStorage.SIDED` providers for every machine block entity;
  - adapt existing `CItemStacksHandler`/`SidedInventoryWrapper` inventories with Fabric `ContainerStorage` or a narrow `Storage<ItemVariant>` adapter when a machine's slot policy cannot be expressed by the vanilla adapter;
  - register `EnergyStorage.SIDED` providers returning each machine's existing `CEnergyStorage`;
  - return `null` for unsupported sides instead of exposing an unrestricted fallback.

  Convert direct machine insert/extract code from `ItemResource` to `ItemVariant`. Open `Transaction.openOuter()`, pass nested transaction contexts through all operations, and call `commit()` only after the complete state transition succeeds.

- [ ] **4.3 Preserve Minecraft 26.2 persistence.**

  Keep `ValueInput`/`ValueOutput` and the Cucumber-Fabric persistence helpers. For every block entity, compare upstream keys and verify:

  - inventories and item components;
  - energy amount and capacity;
  - progress and machine counters;
  - upgrade inventory and effective upgrade tier;
  - altar and pedestal state;
  - essence vessel contents;
  - soulium spawner entity/recipe state.

  Missing or invalid optional fields use field-local defaults and must not clear unrelated valid data.

- [ ] **4.4 Port menus and opening data.**

  Replace `IContainerFactory` with Fabric/vanilla extended screen handlers. The server writes only immutable context such as `BlockPos`; the client resolves a view suitable for rendering, while authoritative inventory, energy, progress, and mutations remain server-side.

  Preserve menu IDs, slot coordinates, quick-move behavior, `ContainerData` indices, reachability checks, and all output restrictions. Validate every decoded block position and expected block entity type.

- [ ] **4.5 Port all four payloads and enforce server authority.**

  Keep these IDs and meanings:

  - experience capsule pickup;
  - ingredient cache reload;
  - essence vessel color synchronization;
  - AOE augment offset update.

  Register clientbound codecs with `PayloadTypeRegistry.clientboundPlay()` and the serverbound AOE codec with `PayloadTypeRegistry.serverboundPlay()`. Register receivers with `ClientPlayNetworking.registerGlobalReceiver` and `ServerPlayNetworking.registerGlobalReceiver`.

  The AOE receiver must validate the executing player, current held stack, tinkerable item, installed AOE augment, and computed maximum range, then clamp before changing the data component. Never trust a client-supplied stack, augment, or maximum.

- [ ] **4.6 Rebuild and synchronize reload state in one ordered flow.**

  Register server-data reload listeners and Cucumber recipe lifecycle callbacks so the sequence is:

  1. inject dynamic recipes;
  2. finish recipe manager loading;
  3. rebuild the server ingredient cache;
  4. load essence vessel colors;
  5. send both states to connected players;
  6. send both states after join/datapack synchronization.

  Keep the last valid optional cache on a recoverable color/cache parse failure, log the resource ID, and leave required recipe decode failures visible.

- [ ] **4.7 Verify every machine boundary and commit.**

  Run:

  ```bash
  ./gradlew test --tests com.blakebr0.mysticalagriculture.network.PayloadCodecTest
  ./gradlew runGameTest
  ./gradlew compileJava
  rg -n "IItemHandler|Capability|RegisterCapabilitiesEvent|ItemResource|IContainerFactory|PacketDistributor|net\\.neoforged" \
    src/main/java/com/blakebr0/mysticalagriculture/{tileentity,container,network,handler} \
    src/main/java/com/blakebr0/mysticalagriculture/api/{machine,util}
  git diff --check
  ```

  Commit:

  ```bash
  git add build.gradle src/main/resources/fabric.mod.json \
          src/main/java/com/blakebr0/mysticalagriculture/{tileentity,container,network,handler,block,crafting,util} \
          src/main/java/com/blakebr0/mysticalagriculture/api/{machine,util} \
          src/test src/gametest
  git commit -m "port: migrate machines and synchronization to Fabric"
  ```

---

### Task 5: Port gameplay callbacks, crops, augments, tinkering, tools, armor, and focused mixins

This slice restores all non-machine gameplay behavior and owns the few Minecraft lifecycle points that need mixins. It must preserve effects and exploit protections, not merely compile event handlers.

**Files:**

- Modify: `src/main/java/com/blakebr0/mysticalagriculture/handler/AugmentHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/handler/ExperienceCapsuleHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/handler/MobDropHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/handler/MobSoulHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/handler/TinkerableHandler.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/tinkering/Augment.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/tinkering/AugmentAttributeModifier.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/api/lib/AbilityCache.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/augment/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/InferiumCropBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/MysticalCropBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/InfusedFarmlandBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/block/GrowthAcceleratorBlock.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/item/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/item/armor/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/item/tool/*.java`
- Create: `src/main/java/com/blakebr0/mysticalagriculture/mixin/ExperienceOrbMixin.java`
- Create: `src/main/java/com/blakebr0/mysticalagriculture/mixin/ItemStackAttributeModifiersMixin.java`
- Modify: `src/main/resources/mysticalagriculture.mixins.json`
- Create: `src/gametest/java/com/blakebr0/mysticalagriculture/gametest/GameplayGameTests.java`
- Modify: `src/gametest/resources/fabric.mod.json`
- Create: `src/test/java/com/blakebr0/mysticalagriculture/network/AOEUpdateValidatorTest.java`

- [ ] **5.1 Define failing gameplay checks before translating handlers.**

  Cover representative behavior:

  - inferium/fertilized essence, wither/dragon essence, and cognizant drops;
  - mob soul progress and soul jar component updates;
  - experience capsule pickup and break behavior;
  - effective-farmland growth gating and optional farmland conversion;
  - fake-player watering configuration;
  - fertilizer and watering growth;
  - an AOE tool operation that respects permissions and durability;
  - augment tick application/removal, flight lifecycle, fall protection, and attribute modifiers;
  - awakened supremium set bonus;
  - server-side AOE validation for negative, oversized, absent-augment, and wrong-item requests.

- [ ] **5.2 Translate event handlers to explicit Fabric registrations.**

  Give each handler a `register()` method. Use Fabric callbacks for:

  - server/player ticks;
  - server play join/disconnect;
  - living death and damage;
  - block use/break interaction;
  - server lifecycle and datapack lifecycle;
  - loot modification only where the drop cannot be produced safely from the death callback.

  Spawn server-authored drops directly and preserve killer, weapon, enchantment, boss, feature-flag, and random-chance predicates. Keep logical-side checks explicit.

- [ ] **5.3 Convert augments and ability state to loader-neutral contexts.**

  Remove NeoForge event objects from public augment hooks. Pass the smallest stable context (`ServerPlayer`, level, stack, slot, or computed ability cache) and represent fall protection as an allow/deny result consumed by the registered damage callback.

  Recompute flight and attribute state from equipped tinkerable items. Remove granted flight on disconnect, death, augment removal, or invalid equipment without disabling creative/spectator flight. Preserve all effect amplifiers, durations, operation types, UUID/identifier stability, and resistance semantics.

- [ ] **5.4 Port tools, armor, crops, watering, and fertilizer directly.**

  Replace NeoForge extension interfaces and hooks with Minecraft 26.2 item/block overrides and Fabric callbacks. Preserve:

  - tool tiers and repair ingredients;
  - AOE shapes, origin, exclusions, harvest checks, permissions, and durability costs;
  - bow/crossbow/fishing behavior;
  - armor attributes and unbreakable configuration;
  - crop seed and secondary-drop rules;
  - growth accelerator cooldown/range;
  - watering fake-player policy;
  - farmland conversion and effective-tier checks.

- [ ] **5.5 Add only the two justified common mixins.**

  `ExperienceOrbMixin` injects at the narrow pickup point needed to preserve experience capsule behavior when no Fabric callback supplies the orb/player transaction.

  `ItemStackAttributeModifiersMixin` augments the computed item attribute component/modifier result only for Mystical Agriculture tinkerable stacks. It must not mutate shared vanilla component instances.

  Give each mixin a comment naming the missing callback, use `require = 1` in development, and keep both out of the client-only config.

- [ ] **5.6 Verify common gameplay and dedicated-server class safety.**

  Run:

  ```bash
  ./gradlew test --tests com.blakebr0.mysticalagriculture.network.AOEUpdateValidatorTest
  ./gradlew runGameTest
  ./gradlew compileJava
  rg -n "net\\.neoforged|@SubscribeEvent|NeoForge\\.EVENT_BUS|IClientItemExtensions" \
    src/main/java/com/blakebr0/mysticalagriculture/{handler,augment,block,item,api}
  rg -n "net\\.minecraft\\.client" \
    src/main/java/com/blakebr0/mysticalagriculture/{handler,augment,block,item,api,network,tileentity}
  git diff --check
  ```

  Commit:

  ```bash
  git add src/main/java/com/blakebr0/mysticalagriculture/{handler,augment,block,item,api,mixin} \
          src/main/resources/mysticalagriculture.mixins.json src/test src/gametest
  git commit -m "port: restore Fabric gameplay callbacks and augments"
  ```

---

### Task 6: Port the complete client: screens, renderers, models, tints, tooltips, HUD, input, and recipe state

This slice establishes the first repository-wide green Java compilation and client startup. All client-only types stay reachable exclusively from `MysticalAgricultureClient` or the client mixin config.

**Files:**

- Modify: `src/main/java/com/blakebr0/mysticalagriculture/MysticalAgricultureClient.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/ModClientExtensions.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/ModClientTooltipComponentFactories.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/ModEquipmentAssets.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/ModMenuScreens.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/ModTESRs.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/handler/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/properties/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/screen/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/tesr/renderer/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/tesr/state/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/client/tints/*.java`
- Modify: `src/main/resources/assets/mysticalagriculture/**/*.json`
- Create: `docs/porting/client-smoke-checklist.md`

- [ ] **6.1 Define the client smoke checklist before changing registrations.**

  Include title-screen startup, world join, all eight menu screens, all eight block-entity renderer/state pairs, crop/augment/crystal/soul-jar tints, both item properties, standalone/dynamic crop models, tooltip components, energy/machine widgets, augment tooltips, HUD overlay, AOE input/offset packet, block outline, resource reload, disconnect/reconnect, and absent JEI/Jade.

- [ ] **6.2 Register screens, renderers, tooltip components, and render assets from the client entrypoint.**

  Replace NeoForge client events with direct Fabric/vanilla registration:

  - `MenuScreens.register`;
  - `BlockEntityRenderers.register`;
  - Fabric tooltip component callbacks/factories;
  - current Minecraft 26.2 equipment assets/render layers;
  - Cucumber-Fabric client widgets and rendering helpers.

  Keep renderer construction lazy enough that common class initialization cannot touch client types.

- [ ] **6.3 Port model loading, item properties, and tint sources using Cucumber-Fabric patterns.**

  Reuse Cucumber-Fabric's current 26.2 `RegisterClientItemsEvent`, item model registration, and tint-source approach. Preserve every existing model identifier and JSON resource contract. Update generated item/block model JSON only where Minecraft 26.2 requires a schema change.

  Do not recreate Forge's model event classes or copy Cucumber code into this project.

- [ ] **6.4 Port HUD, input, outlines, tooltips, and client recipe synchronization.**

  Use Fabric rendering and tick/input callbacks for:

  - augment tooltip enrichment;
  - machine/HUD overlay layers;
  - mouse/keyboard AOE offset input;
  - world block-outline rendering;
  - client recipe manager synchronized/disconnect events;
  - ingredient cache and essence color payload receivers.

  Poll the existing control-plus-arrow chord from `ClientTickEvents.END_CLIENT_TICK` with edge detection, then send one offset payload per new key press. Use `LevelRenderEvents.BEFORE_BLOCK_OUTLINE` for the AOE outline. These 26.2 APIs cover the current behavior, so do not add a client input or outline mixin.

- [ ] **6.5 Eliminate all remaining NeoForge compilation dependencies.**

  Run a repository-wide compile, work through every remaining live import/symbol, and translate rather than exclude the source. Remove empty event subscriber classes and obsolete registration shells. The result must compile with no NeoForge artifact on any configuration.

- [ ] **6.6 Verify client and common builds, then commit.**

  Run:

  ```bash
  ./gradlew clean compileJava compileApiCompatibilityJava test
  ./gradlew test --tests com.blakebr0.mysticalagriculture.config.ModConfigsTest
  ./gradlew test --tests com.blakebr0.mysticalagriculture.registry.PluginRegistryTest
  ./gradlew test --tests 'com.blakebr0.mysticalagriculture.crafting.*'
  ./gradlew test --tests com.blakebr0.mysticalagriculture.network.PayloadCodecTest
  ./gradlew test --tests com.blakebr0.mysticalagriculture.network.AOEUpdateValidatorTest
  ./gradlew runGameTest
  ./gradlew runClient
  rg -n "net\\.neoforged|neoforge\\.mods|accesstransformer|DeferredRegister|DeferredHolder" \
    src/main/java src/main/resources build.gradle settings.gradle gradle.properties
  rg -l "net\\.minecraft\\.client" src/main/java | \
    rg -v '/client/|MysticalAgricultureClient\\.java|/mixin/client/'
  git diff --check
  ```

  Manually execute the full client smoke checklist and record results in `docs/porting/client-smoke-checklist.md`. The repository-wide NeoForge search must have no live code or metadata matches; documentation describing the port is allowed.

  Commit:

  ```bash
  git add src/main/java/com/blakebr0/mysticalagriculture/client \
          src/main/java/com/blakebr0/mysticalagriculture/MysticalAgricultureClient.java \
          src/main/resources/assets \
          docs/porting
  git commit -m "port: restore Mystical Agriculture client parity"
  ```

---

### Task 7: Port optional JEI and Jade integrations and remove deferred integrations

This slice ports both accepted integrations against pinned Fabric artifacts, proves classloading safety when absent, and removes executable integrations explicitly deferred by the approved scope.

**Files:**

- Modify: `build.gradle`
- Modify: `gradle.properties`
- Modify: `src/main/resources/fabric.mod.json`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/compat/jei/JeiCompat.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/compat/jei/category/*.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/compat/jei/recipe/CruxRecipe.java`
- Modify: `src/main/java/com/blakebr0/mysticalagriculture/compat/JadeCompat.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/TOPCompat.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/AwakeningCrafting.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/EnchanterCrafting.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/InfusionCrafting.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/ReprocessorCrafting.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/SoulExtractorCrafting.java`
- Delete: `src/main/java/com/blakebr0/mysticalagriculture/compat/crafttweaker/SouliumSpawnerCrafting.java`
- Create: `docs/porting/optional-integrations-checklist.md`

- [ ] **7.1 Add pinned optional dependencies without changing runtime requirements.**

  Add:

  ```groovy
  compileOnly "mezz.jei:jei-${minecraft_version}-common-api:${jei_version}"
  compileOnly "mezz.jei:jei-${minecraft_version}-fabric-api:${jei_version}"
  compileOnly "curse.maven:jade-324717:${jade_version}"
  ```

  Add opt-in local runtime properties:

  ```groovy
  if (project.hasProperty("enable_jei_runtime")) {
      localRuntime "mezz.jei:jei-${minecraft_version}-fabric:${jei_version}"
  }
  if (project.hasProperty("enable_jade_runtime")) {
      localRuntime "curse.maven:jade-324717:${jade_version}"
  }
  ```

  Use JEI `30.14.0.90` and Jade Fabric `26.2.9`/Curse file `8347273`. Keep only `suggests` metadata; do not add either to `depends`.

- [ ] **7.2 Port the complete JEI plug-in.**

  Keep the `jei_mod_plugin` metadata entrypoint and migrate imports to the Fabric JEI API. Preserve:

  - awakening, crux, enchanter, infusion, ore infuser, reprocessor, soul extractor, and soulium spawner categories;
  - recipe type IDs;
  - catalysts/workstations;
  - recipe collection after dynamic recipe finalization;
  - GUI click areas;
  - transfer handlers;
  - ingredient roles, counts, components, energy/progress display, and layouts.

  JEI classes may refer to JEI only behind its entrypoint. No common initializer or static registry may reference `JeiCompat`.

- [ ] **7.3 Port Jade through its Fabric `IWailaPlugin` discovery contract.**

  Implement the current Jade Fabric `IWailaPlugin` interface and registration methods. Preserve machine energy, progress, inventory/content, and special altar/spawner/vessel displays supported by Jade 26.2. Register providers against the direct block and block-entity values from Task 2.

  Do not call Jade from common initialization and do not add a home-grown mod-loaded reflection bridge.

- [ ] **7.4 Remove TOP and CraftTweaker executable code and document Patchouli deferral.**

  Delete the excluded Java integrations and their dependency/repository configuration. Keep inert Patchouli book assets only if they are ordinary resources that do not require Patchouli classes to load. Document that executable Patchouli integration waits for an accepted Minecraft 26.2 Fabric artifact.

- [ ] **7.5 Verify all four optional-runtime matrices and commit.**

  For each matrix, launch through the title screen and join a test world:

  ```bash
  ./gradlew runClient
  ./gradlew runClient -Penable_jei_runtime
  ./gradlew runClient -Penable_jade_runtime
  ./gradlew runClient -Penable_jei_runtime -Penable_jade_runtime
  ```

  Record category/provider results and absence behavior in `docs/porting/optional-integrations-checklist.md`. Also run:

  ```bash
  ./gradlew clean build
  unzip -p build/libs/MysticalAgriculture-Fabric-26.2-9.0.4+fabric.1.jar fabric.mod.json
  rg -n "theoneprobe|crafttweaker|patchouli-neoforge|jei-.*-neoforge|net\\.neoforged" \
    build.gradle gradle.properties src/main/java src/main/resources
  git diff --check
  ```

  Commit:

  ```bash
  git add build.gradle gradle.properties src/main/resources/fabric.mod.json \
          src/main/java/com/blakebr0/mysticalagriculture/compat docs/porting
  git commit -m "port: add optional Fabric JEI and Jade integrations"
  ```

---

### Task 8: Complete parity audit, dedicated-server verification, documentation, CI, and release preparation

This final slice proves the result rather than adding new architecture. It compares every loader-touched subsystem with upstream, exercises the runtime, inspects artifacts, and makes the fork reproducible and syncable.

**Files:**

- Modify: `README.md`
- Create: `.github/workflows/build.yml`
- Modify: `.github/ISSUE_TEMPLATE/bug_report.md`
- Modify: `.github/ISSUE_TEMPLATE/feature_request.md`
- Create: `docs/porting/upstream-sync.md`
- Create: `docs/porting/parity-matrix.md`
- Create: `docs/porting/server-smoke-checklist.md`
- Create: `docs/porting/release-checklist.md`
- Modify: test files from Tasks 1–5 only when the audit finds a missing regression

- [ ] **8.1 Audit every loader-touched class against the pinned upstream baseline.**

  Generate the changed-file list against `cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462`, classify every file under build/bootstrap, registry/API, data/recipes/worldgen, machine/storage/network, gameplay, client, or integration, and record its parity status in `docs/porting/parity-matrix.md`.

  For each intentional behavioral difference, record:

  - upstream behavior;
  - Fabric behavior;
  - reason the difference is necessary;
  - automated or manual verification;
  - whether it affects add-on API or saved data.

  No file may remain “unreviewed.”

- [ ] **8.2 Run the full automated verification suite.**

  First ensure the required Cucumber commit exists on the remote branch used by CI:

  ```bash
  git -C ../Cucumber-Fabric rev-parse HEAD
  git -C ../Cucumber-Fabric branch -r --contains 598ee054d6d6826b8c150d32e7bf46ca412d0ff1
  ```

  If it is still local-only, push that already-approved Cucumber commit or deliberately update the baseline and rerun all tests before continuing.

  Then run:

  ```bash
  ./gradlew --stop
  ./gradlew clean runDatagen
  git diff --exit-code -- src/generated/resources
  ./gradlew clean build
  ./gradlew runGameTest
  ./gradlew compileApiCompatibilityJava
  ```

- [ ] **8.3 Execute dedicated-server and gameplay smoke verification.**

  Launch `./gradlew runServer`, accept the generated development EULA only in the ignored run directory, and verify completed registry/datapack/recipe loading with no client-class linkage.

  In a retained test world, execute and record:

  - dynamic crop, augment, and soul registration;
  - `/reload`, cache/color synchronization, disconnect, and reconnect;
  - one representative recipe for every machine;
  - sided item automation and energy transfer;
  - upgrade scaling;
  - save/restart persistence;
  - crops, fertilizer, watering, farmland, drops, souls, experience capsules;
  - tools, armor, augments, flight, fall protection, and AOE behavior.

- [ ] **8.4 Inspect release artifacts for forbidden and required content.**

  Run:

  ```bash
  jar tf build/libs/MysticalAgriculture-Fabric-26.2-9.0.4+fabric.1.jar | sort
  jar tf build/libs/MysticalAgriculture-Fabric-26.2-9.0.4+fabric.1-sources.jar | sort
  unzip -p build/libs/MysticalAgriculture-Fabric-26.2-9.0.4+fabric.1.jar fabric.mod.json | jq .
  rg -n "net\\.neoforged|neoforge\\.mods|accesstransformer|biome_modifier|DeferredRegister|DeferredHolder" \
    src/main/java src/main/resources build.gradle settings.gradle gradle.properties
  ```

  Required in the release JAR: Fabric metadata, common/client mixin configs, access widener, classes, assets, data, and generated resources.

  Forbidden: NeoForge metadata, access transformer, NeoForge biome modifiers/conditions, development configuration, local paths, run files, test classes, generated caches, and bundled JEI/Jade.

- [ ] **8.5 Document the fork, local dependency, exclusions, and upstream synchronization.**

  Update `README.md` with:

  - clear unofficial Fabric fork status and upstream attribution;
  - Minecraft 26.2/Fabric/Java 25 requirements;
  - exact clone layout and `cucumber_project_path` override;
  - build, data-generation, client, server, and optional-integration commands;
  - preserved IDs and save-compatibility intent;
  - JEI/Jade optional status;
  - TOP/CraftTweaker/Patchouli deferrals;
  - links to both upstream repositories.

  In `docs/porting/upstream-sync.md`, document:

  ```bash
  git fetch upstream
  git switch 26.1
  git merge --ff-only upstream/26.1
  git switch fabric/26.2
  git merge upstream/26.1
  ./gradlew clean build
  ```

  Explain that an eventual upstream 26.2 branch must be reviewed before changing the merge source and that history must not be rebased away from upstream ancestry.

- [ ] **8.6 Replace CI with the sibling-checkout composite build.**

  Configure GitHub Actions to:

  1. check out `Arilas/Cucumber` branch `fabric/26.2` into `Cucumber-Fabric`;
  2. check out the current repository into sibling directory `MysticalAgriculture`;
  3. install a Java 25 distribution;
  4. validate the wrapper and configure Gradle caching;
  5. run `./gradlew clean build` in `MysticalAgriculture`;
  6. upload the release and sources JARs;
  7. never require Blake's private Maven credentials.

  Update issue forms from NeoForge terminology to Fabric and request the Minecraft, Loader, Fabric API, Cucumber-Fabric, Mystical Agriculture, Java, and optional integration versions.

- [ ] **8.7 Perform final clean-room verification, commit, and push the branch.**

  From a temporary sibling directory, clone both fork branches exactly as CI does and run:

  ```bash
  ./gradlew clean build
  ./gradlew runGameTest
  ```

  Back in the working repository:

  ```bash
  git status --short
  git diff --check
  git diff --stat cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462..HEAD
  git add README.md .github docs/porting src/test src/gametest
  git commit -m "docs: verify and document Fabric 26.2 port"
  git push origin fabric/26.2
  ```

  Do not stage `.vscode/`. Do not create or push a release tag without a separate explicit release decision. Suggested future tag after approval: `26.2-9.0.4+fabric.1`.

## Definition of Done

- [ ] `./gradlew clean build`, `runDatagen`, `runGameTest`, and API compatibility compilation pass with Java 25.
- [ ] Fabric client and dedicated server both start and load a world.
- [ ] All core registries and datapack resources load with stable IDs.
- [ ] Every machine preserves inventory, energy, progress, upgrades, and outputs across restart.
- [ ] Core crop, recipe, soul, augment, tool, armor, and machine behavior matches upstream.
- [ ] Reload and login synchronization remain coherent and server-authoritative.
- [ ] JEI and Jade work independently and together, and their absence is harmless.
- [ ] No NeoForge code or metadata remains in the Fabric artifact.
- [ ] Every deliberate exclusion or parity difference is documented.
- [ ] CI builds from two sibling fork checkouts without private credentials.
- [ ] `origin/fabric/26.2` contains the reviewed implementation history and remains mergeable with `upstream/26.1`.
