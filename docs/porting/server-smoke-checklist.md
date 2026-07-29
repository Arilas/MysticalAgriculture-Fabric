# Dedicated-server and gameplay verification

Target: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2,
Cucumber-Fabric 9.0.5+fabric.1, Java 25.

Checked items were exercised by a real dedicated server or an executable
production-backed test. UI-only behavior is not inferred from registration.

## Dedicated server

- [x] Standard `clean runServer` launches without a client entrypoint or
  client-class linkage.
- [x] The retained `run/world` loads, saves, stops cleanly, and loads again.
- [x] Core registration reports 136 crops, 6 crop tiers, 2 crop types,
  55 augments, and 23 mob soul types.
- [x] Mystical Agriculture appears as an enabled datapack.
- [x] Startup loads 2,501 recipes and 1,688 advancements.
- [x] `/reload` reloads the same counts, stages four essence-vessel colors,
  and rebuilds the ingredient cache.
- [x] No registry, recipe, datapack, mixin, or client-linkage error appears.

Only the generated, gitignored `run/eula.txt` was accepted. No global or
external server EULA was touched.

## Automated gameplay and persistence

- [x] 33 integrated Fabric GameTests pass.
- [x] All powered processor inventory/energy save fields round-trip.
- [x] Awakening altar inventory/active state, extended essence-vessel stack,
  Soulium Spawner input/energy, and Harvester current/legacy fuel keys
  round-trip.
- [x] Sided item insertion/extraction, output rejection, slot limits, nested
  transaction rollback, one-shot commit notification, and energy
  commit/rollback are covered.
- [x] Upgrade slot type/limit and detached Soulium Spawner menu transfer are
  covered.
- [x] Crop growth/fertilizer/farmland, watering permissions, mob souls/drops,
  experience capsules, flight lifecycle, armor growth, attribute augments,
  tilling, and mining AOE are covered by production-backed GameTests.
- [x] Recipe codecs, resource conditions, ingredient matching, all custom
  serializer synchronization, and JEI's nonzero synchronized recipe collections
  are executable-test or real-world-join evidence.

## Manual follow-up

- [ ] Visually operate one recipe in every machine screen.
- [ ] Connect a second real client, disconnect, and reconnect while observing
  cache/color synchronization.
- [ ] Exercise rendered armor, fall protection, flight, tools, and AOE outline
  in survival gameplay.

Those items remain manual presentation/end-to-end checks; they are not claimed
by the server startup or headless tests.
