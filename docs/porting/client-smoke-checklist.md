# Mystical Agriculture Fabric client smoke checklist

Target: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2, Java 25.

Automated evidence and manual observations are recorded here. A checked item means
the behavior was observed in a real client or exercised by an executable test;
registration or compilation alone is not treated as a completed UI interaction.

- [x] Title screen starts without Mystical Agriculture errors.
- [x] A local world can be created or joined.
- [ ] Tinkering Table screen opens and its slots/widgets render.
- [ ] Enchanter screen opens and its slots/widgets render.
- [ ] Essence Furnace screen opens and its energy, fuel, and progress widgets render.
- [ ] Seed Reprocessor screen opens and its energy, fuel, and progress widgets render.
- [ ] Soul Extractor screen opens and its energy, fuel, and progress widgets render.
- [ ] Harvester screen opens and its energy/fuel widgets render.
- [ ] Soulium Spawner screen opens and its energy, fuel, and progress widgets render.
- [ ] Ore Infuser screen opens and its energy, fuel, and progress widgets render.
- [ ] Infusion Pedestal item renderer/state pair renders.
- [ ] Infusion Altar item/ghost-pedestal renderer/state pair renders.
- [ ] Tinkering Table item renderer/state pair renders.
- [ ] Enchanter item renderer/state pair renders.
- [ ] Awakening Pedestal item renderer/state pair renders.
- [ ] Awakening Altar item/ghost-pedestal renderer/state pair renders.
- [ ] Essence Vessel contents renderer/state pair renders with the synchronized color.
- [ ] Soulium Spawner entity renderer/state pair renders.
- [ ] Dynamic crop stem and mature flower models render after a resource reload.
- [ ] Dynamically registered crop seed and essence item models render.
- [ ] Crop block/seed/essence tinting renders.
- [ ] Augment item primary/secondary tinting renders.
- [ ] Infusion Crystal durability tinting renders.
- [ ] Filled Soul Jar tinting and fill-level property render.
- [ ] Experience Capsule fill-level property renders.
- [ ] Augment tooltip includes the compatible-equipment image component.
- [ ] Machine block tooltip shows capacity/usage and shift detail.
- [ ] Machine upgrade tooltip shows tier modifiers.
- [ ] Altar/vessel HUD overlay renders its output, requirements, or contents.
- [ ] Holding control and newly pressing an arrow sends one AOE offset payload.
- [x] Holding an arrow does not repeatedly send AOE offset payloads.
- [ ] Holding control shows the AOE block outline.
- [x] Recipe lists/maps populate after recipe synchronization.
- [x] Ingredient-cache and essence-color payloads update client state.
- [ ] Resource reload preserves functional models, tints, and overlays.
- [x] Disconnect clears recipe/cache/color client state.
- [x] Reconnect repopulates recipe/cache/color client state without duplicate callbacks.
- [x] Client remains safe with JEI and Jade absent.

## Evidence

- `./gradlew compileTask6Java --rerun-tasks --console=plain` completed with zero
  diagnostics for all production sources outside the separately owned
  `compat/**` slice.
- `./gradlew task6Test --rerun-tasks --console=plain` passed eight executable
  client contracts. These cover the vanilla item-property/tint dispatch codecs,
  Fabric tooltip factory, model-plugin idempotency, AOE key-edge behavior,
  equipment layer JSON, item-definition schema, and standalone staff particle
  texture, and disconnect cleanup of recipe/cache/color state.
- `./gradlew runClientGameTest --rerun-tasks --console=plain` passed in the
  built-in Fabric client GameTest runtime. It created and joined a real
  integrated world, observed nonzero synchronized recipes, ingredient-cache
  types/items, and four essence-vessel colors. Test-only receiver probes wrapped
  the registered production receivers and observed exactly one cache payload
  and one color payload on each join/reload/rejoin. The test also verified
  disconnect cleanup and coherent repopulation when reopening the same save.
- `./gradlew runTask6FocusedClient --console=plain` launched Minecraft 26.2 with
  Java 25 and Loader 0.19.3. Mystical Agriculture registered its plug-in and
  loaded 136 crops, 6 crop tiers, 2 crop types, 55 augments, and 23 mob soul
  types. The ResourceManager reload completed through all texture-atlas
  creation, and the process remained running at the title screen until stopped.
- The focused launch runtime classpath points directly at
  `build/classes/java/task6Main` and producer-backed
  `build/resources/task6Client`. Clean launches with and without configuration
  cache emitted no missing-classpath/resource warning. The focused run uses
  Loom's bare-classpath mode, so it no longer depends on an undeclared
  argument-file side effect across `clean` and configuration-cache reuse.
- The focused runtime intentionally contained neither JEI nor Jade. No missing
  compatibility entrypoint or optional-mod exception occurred.
- The standard `clean runClient` initially reproduced Loom's deleted-argfile
  failure. After all standard run tasks were moved to Loom's bare classpath,
  two clean launches (the second reusing configuration cache) loaded all core
  registries and completed resource reload.
- Task 7's four optional-mod matrices each joined a real disposable local
  world. JEI-only and combined runs received nonzero synchronized recipe
  collections, proving the client recipe maps populated after login.
- The first runtime pass exposed a missing `particle` reference in the
  `supremium_staff` model. A failing resource contract reproduced it; after the
  one-field resource fix, a second full client reload emitted no missing-model
  or missing-texture warning for that asset.
- The development account's expected Realms/authentication 401 messages and
  macOS OpenGL shader-driver warnings were unrelated to Mystical Agriculture.
- World joins were automated through quick-play, but the GLFW window remained
  unavailable to desktop inspection. Consequently the unchecked UI/render
  interactions above still require a human smoke pass.
