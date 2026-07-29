# Mystical Agriculture Fabric 26.2 Port Design

**Date:** 2026-07-29
**Status:** Approved
**Target:** Fabric-only Mystical Agriculture for Minecraft 26.2 and Java 25
**Upstream repository:** `BlakeBr0/MysticalAgriculture`
**Upstream baseline:** branch `26.1`, commit `cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462`
**Fabric fork:** `Arilas/MysticalAgriculture-Fabric`
**Local dependency:** Cucumber-Fabric commit `598ee054d6d6826b8c150d32e7bf46ca412d0ff1`

## Context

The original checkout was a shallow clone of Mystical Agriculture branch `26.1` at release `9.0.4`, commit `2c959f08`. The remote branch had advanced by two commits. The checkout has been unshallowed and fast-forwarded to the current upstream head, `cc1e1a3e`, which updates Patchouli integration and the Gradle wrapper. Upstream has no Minecraft 26.2 branch as of this design.

The repository has been forked through GitHub as `Arilas/MysticalAgriculture-Fabric`, and GitHub reports it as a fork of `BlakeBr0/MysticalAgriculture`. The local remote layout reserves `origin` for the Fabric fork and `upstream` for BlakeBr0's repository. The protected synchronization branch `26.1` tracks `upstream/26.1`; Fabric work belongs on `fabric/26.2`.

The upstream NeoForge baseline compiles successfully with Java 25. It contains 284 Java source files. Of those, 112 directly import NeoForge APIs, 116 import Cucumber APIs, and 42 are client-side classes. The loader-specific surface includes initialization, deferred registration, event buses, configuration, item and energy capabilities, item transactions, custom ingredients, resource conditions, biome modifiers, networking, data generation, plug-in discovery, client models, rendering, and optional integrations.

The neighboring Cucumber-Fabric project already provides a working Minecraft 26.2 port of the shared inventory, energy, persistence, recipe lifecycle, tool, and client foundations used by Mystical Agriculture. Mystical Agriculture must consume that project locally rather than copy its classes or reproduce its loader abstractions.

## Goals

- Produce a Fabric-only port for Minecraft `26.2` and Java `25`.
- Preserve Mystical Agriculture gameplay and data compatibility wherever Fabric can represent the same behavior.
- Preserve mod ID `mysticalagriculture`, resource namespace, registry identifiers, Java package root `com.blakebr0.mysticalagriculture`, data component identifiers, recipe identifiers, tags, payload identifiers, and persistent state keys.
- Keep the Fabric history rooted in the latest upstream `26.1` commit so future upstream work can be merged normally.
- Reuse Fabric, Fabric API, vanilla, Cucumber-Fabric, and Team Reborn Energy facilities before adding mixins or local compatibility code.
- Use the repository and artifact suffix `-Fabric` without renaming in-game identifiers.
- Support a configurable local Cucumber-Fabric composite build during development.
- Deliver core gameplay parity plus optional JEI and Jade integration in the first complete port.
- Keep changes organized as medium-to-large, independently reviewable subsystems.

## Non-goals

- Maintaining NeoForge and Fabric from one source tree in the first port.
- Introducing Architectury or another cross-loader framework.
- Recreating NeoForge's event bus, deferred-register system, capability system, or annotation scanner on Fabric.
- Renaming the mod ID, registry namespace, packages, saved registries, recipes, tags, or data components with a `-fabric` suffix.
- Porting The One Probe, CraftTweaker, or Patchouli integration in the first milestone.
- Redesigning established gameplay, recipes, machine balance, configuration semantics, textures, models, or documentation.
- Publishing to BlakeBr0's Maven repository.
- Creating a broad test suite for loader-neutral gameplay that is unchanged from upstream.

## Chosen Strategy

Use a direct, parity-first Fabric branch.

Branch `fabric/26.2` starts at upstream commit `cc1e1a3e`. The existing single module is converted from NeoForge ModDevGradle to Fabric Loom. Loader-neutral classes and resources remain in place. NeoForge boundaries are translated to the narrowest current vanilla or Fabric API. Focused mixins are allowed only where Fabric has no suitable callback or extension point.

This approach is preferred over a common/Fabric module split because the project is Fabric-only for now and moving nearly every file would make upstream merges unnecessarily noisy. It is preferred over a NeoForge compatibility shim because Mystical Agriculture uses too much of NeoForge's surface for a partial imitation to remain smaller or safer than direct translation.

## Global Constraints

- Minecraft version is exactly `26.2`.
- Java toolchain version is exactly `25`.
- Fabric Loader version is `0.19.3`.
- Fabric API version is `0.155.2+26.2`.
- Fabric Loom version is `1.17.12`, matching the working Cucumber-Fabric project.
- Cucumber-Fabric version is `26.2-9.0.5+fabric.1`.
- The local Cucumber checkout defaults to `../Cucumber-Fabric` and can be overridden with Gradle property `cucumber_project_path`.
- Repository name and archive base name are `MysticalAgriculture-Fabric`.
- The first Fabric mod version is `9.0.4+fabric.1`; the complete Gradle version is `26.2-9.0.4+fabric.1`.
- Mod ID remains `mysticalagriculture`.
- Java package root remains `com.blakebr0.mysticalagriculture`.
- JEI and Jade are optional integrations, not required runtime dependencies.
- The One Probe, CraftTweaker, and Patchouli are excluded from the first port.

## Repository and Upstream Synchronization

`origin` points to `https://github.com/Arilas/MysticalAgriculture-Fabric.git`. `upstream` points to `https://github.com/BlakeBr0/MysticalAgriculture.git`.

The upstream tracking branch remains free of Fabric commits. All port commits land on `fabric/26.2`, which is published to `origin/fabric/26.2`. Routine synchronization is:

1. fetch `upstream`;
2. fast-forward the local `26.1` branch to `upstream/26.1`;
3. review new upstream commits by subsystem;
4. merge `upstream/26.1` into `fabric/26.2`;
5. resolve conflicts only at affected Fabric boundaries;
6. rerun the clean build, server/client startup, and relevant subsystem checks.

If upstream later creates a 26.2 branch, its ancestry and changes are reviewed before changing the synchronization source. The port does not discard or rewrite its upstream ancestry.

## Build and Dependency Architecture

The project uses Fabric Loom with Fabric Loader and Fabric API dependencies matching Cucumber-Fabric. NeoForge repositories, ModDevGradle configuration, run definitions, metadata processing, and private Maven publication are removed.

`settings.gradle` includes the Cucumber-Fabric checkout as a composite build using `cucumber_project_path`, defaulting to `../Cucumber-Fabric`. Dependency substitution resolves the Cucumber module to that local project. A missing or invalid checkout fails during Gradle configuration with an actionable message that names the property and expected directory. Mystical Agriculture does not vendor Cucumber, use a Git submodule, or depend on an opaque copied JAR during active development.

`fabric.mod.json` defines `main`, `client`, `fabric-datagen`, `jei_mod_plugin`, and `mysticalagriculture:plugin` entrypoints. Jade integration follows the pinned Jade 26.2 Fabric `IWailaPlugin` discovery contract. The metadata requires Minecraft, Java, Fabric Loader, Fabric API, Cucumber, and Team Reborn Energy at the compatible versions. JEI and Jade are suggestions. Client entrypoints and client-only classes are never referenced by common initialization.

The release JAR retains upstream assets and data while excluding NeoForge metadata, access transformers, NeoForge biome modifiers, development files, generated cache files, and local configuration.

## Initialization and Registration

Common initialization follows a deterministic order:

1. load and validate configuration;
2. initialize public API references;
3. discover built-in and external Mystical Agriculture Fabric plug-ins;
4. register fixed and plug-in-provided blocks, items, crops, augments, and mob soul types;
5. register data components, creative tabs, block entity types, menus, recipe types, recipe serializers, custom ingredients, resource conditions, features, networking payloads, item storage, and energy storage;
6. finalize crop, augment, and soul registries;
7. attach lifecycle, reload, and gameplay callbacks.

Client initialization then registers screens, block entity renderers, tooltip components, models, tint sources, item properties, HUD rendering, AOE input and outlines, recipe synchronization, JEI, and Jade.

NeoForge `DeferredRegister` and `DeferredHolder` do not receive local imitations. Registration classes expose actual vanilla registry values or `ResourceKey` values and use small, registry-specific registration functions. Fixed blocks and items register directly in the correct order. Dynamic crop and plug-in content is collected before the corresponding registry pass.

The public data component class continues to expose the same named constants, but their Java types become vanilla `DataComponentType` values rather than NeoForge holders. Other public signatures are converted similarly. Package and member names remain stable where the loader-neutral contract permits it.

## Plug-in Discovery

NeoForge classpath annotation scanning is replaced by a Fabric Loader entrypoint named `mysticalagriculture:plugin`. Add-ons declare implementations of the existing Mystical Agriculture plug-in interface in their `fabric.mod.json`.

Plug-ins are instantiated before dynamic content registration. Duplicate crop, augment, mob soul, block, item, or identifier registration fails with a message containing the conflicting identifier and source plug-in. A plug-in exception identifies the responsible mod and entrypoint and aborts initialization; the port does not continue with a partially registered add-on.

The main mod registers the built-in core plug-in directly before loading external Fabric entrypoints. The core plug-in exercises the same registry APIs available to external Fabric add-ons.

## Configuration

Fabric provides no built-in equivalent to NeoForge's `ModConfigSpec`, so the port uses one typed JSON configuration at `config/mysticalagriculture.json`. The implementation follows the proven Cucumber-Fabric persistence and validation approach and uses the JSON support already present in the runtime.

All upstream settings, defaults, meanings, and numeric bounds are preserved:

- `inferiumDropChance`: `0.2`, range `0.0` through `1.0`;
- `infusionCrystalUses`: `1000`, range `10` through `Integer.MAX_VALUE`;
- `growthAcceleratorCooldown`: `10`, range `1` through `Integer.MAX_VALUE`;
- `fertilizedEssenceChance`: `0.1`, range `0.0` through `1.0`;
- `secondarySeedDrops`: `true`;
- `requiresEffectiveFarmland`: `false`;
- `witherDropsEssence`: `true`;
- `witherDropsCognizant`: `true`;
- `dragonDropsEssence`: `true`;
- `dragonDropsCognizant`: `true`;
- `essenceFarmlandConversion`: `true`;
- `seedCraftingRecipes`: `false`;
- `unbreakableSupremiumArmor`: `false`;
- `fakePlayerWatering`: `true`;
- `awakenedSupremiumSetBonus`: `true`;
- `generateProsperityOre`: `true`;
- `generateInferiumOre`: `true`;
- `generateSoulstone`: `true`;
- `souliumOreChance`: `0.05`, range `0.0` through `1.0`.

Missing files create documented defaults. Unknown keys are ignored, and a successfully parsed existing file is not automatically rewritten. A malformed key logs its name and bad value, then falls back only that key. A malformed file logs the parse failure and loads defaults without preventing startup. Configuration values remain available through `MysticalAgricultureConfigValues` without exposing loader classes.

## Recipes, Ingredients, Conditions, and Data Generation

Recipe types, serializers, codecs, network codecs, IDs, and JSON field names remain stable. NeoForge `SizedIngredient` is replaced by a small count-bearing Mystical Agriculture value type built from vanilla `Ingredient`, a positive count, a map codec, and a stream codec. This is a domain value needed by awakening, enchanter, soulium spawner, and ore infuser recipes, not a general NeoForge compatibility layer.

Filled soul jar and crop component ingredients implement Fabric `CustomIngredient` and register through `CustomIngredientSerializer`. Component-sensitive matching uses Fabric's existing ingredient facilities rather than copying NeoForge `DataComponentIngredient`. Recipe matching uses vanilla or Fabric recipe facilities where available, with focused Mystical Agriculture matching logic only for the mod's multi-input machines.

Crop-enabled, augment-enabled, crop-material, and seed-recipe conditions implement Fabric `ResourceCondition` and register through `ResourceConditions`. Generated recipes use Fabric's condition format. Built-in tag-populated and negation conditions replace NeoForge `TagEmptyCondition` and `NotCondition`.

Data generation moves to Fabric's data-generation entrypoint. Existing custom generators may remain when their output is already correct, but registration and condition serialization use Fabric APIs. Generated output is compared against the committed assets and data so path or format changes are explicit.

Dynamic recipe injection continues to use Cucumber-Fabric's recipe manager loading and loaded lifecycle events. The dynamic manager prepares crop-dependent recipes before final recipe indexing. Ingredient caches and client recipe lists rebuild only after the final recipe manager state is available.

## Machines, Storage, and Persistence

Machine gameplay remains based on Cucumber's `CItemStacksHandler`, `SidedInventoryWrapper`, `CEnergyStorage`, `BaseInventoryTileEntity`, and container helpers.

Mystical Agriculture registers inventory providers with `ItemStorage.SIDED`. Vanilla `Container` and `WorldlyContainer` adapters expose the existing Cucumber inventory rules to Fabric automation. Direct machine mutations use Fabric `ItemVariant` and transaction contexts. Transactions must preserve insert/extract restrictions, sided behavior, slot limits, output-only slots, nested rollback, and one final content-change notification.

Energy providers register with Team Reborn `EnergyStorage.SIDED`. Existing machine capacities, per-tick costs, upgrade scaling, extraction and insertion permissions, and UI synchronization remain unchanged. Cucumber's `CEnergyStorage` owns transaction and persistence behavior.

Block entity persistence continues to use Minecraft 26.2 `ValueInput` and `ValueOutput`. Save/load round trips preserve inventories, empty slots, item components, energy amount and capacity, progress, upgrades, machine-specific counters, essence vessel contents, altar state, and spawner state. Invalid or absent optional values use safe defaults without clearing unrelated valid state.

Menus use vanilla/Fabric extended opening data rather than NeoForge `IContainerFactory`. The server supplies only the block position and other required immutable context; inventories and machine state are resolved from the authoritative block entity. Client input cannot directly set machine inventory or energy.

## Networking and Reload Synchronization

The four current payload identifiers and wire meanings are retained:

- experience capsule pickup;
- ingredient cache reload;
- essence vessel color synchronization;
- AOE augment offset update.

Payload types and codecs register through `PayloadTypeRegistry`. Clientbound handlers use `ClientPlayNetworking`; the serverbound AOE handler uses `ServerPlayNetworking`. Handlers execute on the correct game thread.

The AOE serverbound payload carries only requested offset changes. The server verifies the player, held item, installed augment, and current maximum range before applying clamped data component values. Invalid requests do not mutate the stack.

Datapack reload follows this flow:

1. prepare and inject dynamic recipes;
2. finalize the recipe manager;
3. rebuild server ingredient caches;
4. reload essence vessel colors;
5. synchronize caches and colors to all connected players;
6. send the same state to a player after login or datapack synchronization.

A reload failure logs the responsible resource. Where state can remain coherent, the last valid color or cache data stays active. Required recipe decoding failures remain visible rather than being silently discarded.

## Gameplay Events and Mixins

Fabric callbacks replace NeoForge events for player ticks, connection lifecycle, living death, block interaction, block breaking, loot table modification, server lifecycle, resource reloads, commands, client connection, world rendering, and other supported hooks.

Cucumber-Fabric's `RecipeManagerLoadingEvent`, `RecipeManagerLoadedEvent`, `ItemBreakEvent`, and `RegisterClientItemsEvent` remain the source for Mystical Agriculture behavior already expressed through those public Cucumber events.

Mob drops, mob souls, augments, flight, fall protection, tinkering, experience capsules, watering cans, fertilizer, crop growth, farmland conversion, tool AOE, armor bonuses, and item attribute changes preserve upstream conditions and side effects.

Mixins are limited to lifecycle points with no adequate Fabric callback. Expected candidates include experience orb pickup and specialized client input or outline extraction. Each mixin targets a narrow Minecraft 26.2 method, separates common and client configuration, documents why a public callback is insufficient, and fails visibly in development when its injection point changes.

## World Generation

NeoForge biome modifier codecs and JSON files are replaced by Fabric `BiomeModifications`.

Prosperity ore, inferium ore, soulstone, and soulium ore retain their configured placed features, biome selections, generation stages, target states, distributions, and configuration switches. Soulstone keeps its custom feature algorithm and the `souliumOreChance` behavior. Generation is registered only when the matching configuration option is enabled.

Existing configured and placed feature resources remain data-driven. The port does not hard-code complete ore placement definitions merely to avoid resource loading.

## Client Architecture

All client-only work lives behind `ClientModInitializer` or Fabric client entrypoints.

The client registers:

- menu screens;
- block entity renderers and render states;
- client tooltip component factories;
- block and item colors;
- item model properties and standalone models;
- render layers;
- augment tooltips;
- machine and energy widgets;
- HUD overlays;
- AOE input and block outlines;
- recipe synchronization and client caches.

Model loading uses Fabric model-loading facilities and current Minecraft 26.2 item model APIs. A mixin is used only if an upstream Cucumber or vanilla extension point required by dynamic crop models is unavailable.

Dedicated-server verification is the guard against accidental references to `net.minecraft.client` from common entrypoints, metadata, static initializers, payload classes, or optional integrations.

## Optional Integrations

JEI and Jade are the first-port integrations.

JEI uses the Fabric JEI entrypoint and the 26.2 Fabric API artifact already used by Cucumber-Fabric. Existing recipe categories, catalysts, transfer handlers, and recipe displays are ported without changing recipe IDs or machine semantics.

Jade uses its Fabric plug-in entrypoint and API. Existing machine energy, progress, and content displays are preserved where Jade exposes the equivalent Fabric contract.

Both integrations are compile-time optional and runtime suggestions. Their classes must not load when the mod is absent.

The One Probe and CraftTweaker remain excluded because their upstream integration is already disabled or stale. Patchouli assets remain in the repository, but executable integration and runtime dependency are deferred until a Minecraft 26.2 Fabric build is available and explicitly accepted.

## Failure Handling

- Missing local Cucumber-Fabric produces an actionable Gradle configuration error.
- Invalid configuration values fall back per key and log the key and supplied value.
- Duplicate dynamic registrations report the identifier and responsible plug-in.
- External plug-in initialization errors report the source mod and fail before registry finalization.
- Optional integrations never trigger class loading when absent.
- Invalid serverbound payloads are rejected without mutation.
- Required mixin injection failures are fatal in development.
- Transaction aborts restore prior inventory and energy state.
- Persistence decoding uses safe defaults for individual missing or malformed optional fields.
- Reload errors identify the resource and preserve the last coherent optional cache where possible.
- Dedicated-server startup fails the verification milestone if any client class leaks into common initialization.

## Implementation Milestones

### 1. Fabric Foundation and Bootstrap

Create and publish `fabric/26.2`, convert the build to Loom, configure the local Cucumber composite dependency, add Fabric metadata, implement typed configuration, and establish common/client/data entrypoints and startup ordering.

### 2. Registries, Public API, Recipes, and World Generation

Convert fixed and dynamic registries, public API loader types, plug-in discovery, custom ingredients, count-bearing ingredients, resource conditions, recipe types and serializers, data generation, placed features, and biome modifications.

### 3. Machines, Menus, Storage, and Networking

Port block entities and menus as one subsystem, register sided inventory and energy providers, translate item transactions, preserve state serialization, and port the four payloads and reload synchronization.

### 4. Gameplay Systems and Events

Restore crop behavior, mob drops and souls, experience capsules, augments, tinkering, tools, armor, watering and fertilizer interactions, dynamic recipes, caches, and lifecycle behavior through callbacks and focused mixins.

### 5. Client Parity and Optional Integrations

Port screens, renderers, models, colors, tooltips, widgets, HUD, AOE controls and outlines, client recipe state, JEI, and Jade while maintaining dedicated-server safety.

### 6. Parity Audit and Release Preparation

Compare every materially changed class against upstream, run automated and manual verification, inspect the artifacts, document deliberate exclusions, and prepare reproducible build and synchronization instructions.

Each milestone is independently reviewable and ends in a buildable or runtime-testable state. Commits remain cohesive by subsystem rather than splitting mechanical edits into unrelated micro-commits.

## Verification

### Build and Static Checks

- `./gradlew clean build` succeeds from a documented checkout layout.
- Fabric data generation succeeds and produces expected paths and condition formats.
- A compile-only API compatibility source set confirms exported API packages contain no NeoForge types and can be consumed by Fabric code.
- No direct `net.neoforged` imports remain.
- No NeoForge metadata, access transformers, biome modifiers, resource conditions, or service declarations remain in the release JAR.
- The release JAR and sources JAR contain Fabric metadata, mixin configuration, classes, assets, and data.

### Automated Focused Tests

- configuration defaults, range validation, malformed values, and round trips;
- custom ingredient and count-bearing ingredient codecs;
- resource condition codecs and evaluation;
- inventory insert, extract, sided restrictions, nested commit, rollback, and callbacks;
- energy insert, extract, capacity, rollback, and persistence;
- block entity save/load for representative machine states;
- payload codec and server-side AOE validation.

### Runtime Smoke Checks

- Fabric client reaches the title screen and joins a world.
- Fabric dedicated server reaches completed datapack and recipe loading.
- Dynamic crop, augment, and mob soul registration completes.
- Datapack reload rebuilds recipes and synchronizes ingredient and color caches.
- Reconnection receives current synchronized cache state.
- Every machine processes one representative recipe, respects sided automation, consumes energy where applicable, applies upgrades, and survives save/reload.
- Crops, fertilizer, watering cans, farmland, mob drops, mob souls, experience capsules, tools, armor, augments, flight, fall protection, and AOE behavior are spot-checked.
- Client models, colors, block entity renderers, screens, widgets, tooltips, HUD, input, and outlines render without errors.
- JEI and Jade work when installed and are harmless when absent.

## Completion Criteria

The first Fabric port is complete when:

1. the clean build and data generation succeed;
2. client and dedicated-server startup succeed;
3. all core registrations and datapack resources load;
4. machines preserve inventory, energy, progress, upgrades, and outputs across restart;
5. core crop, recipe, soul, augment, tool, armor, and machine behavior matches upstream;
6. reload and login synchronization remain coherent;
7. JEI and Jade work as optional integrations;
8. no NeoForge code or metadata remains in the Fabric artifact;
9. every deliberate exclusion is documented;
10. the fork contains reproducible local dependency and upstream synchronization instructions.

Cucumber-Fabric commit `598ee054d6d6826b8c150d32e7bf46ca412d0ff1` is one commit ahead of its current GitHub branch. Before CI or a release build depends on that behavior, the commit must be pushed or the required Cucumber baseline must be deliberately changed and reverified.
