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

- [x] 41 integrated Fabric GameTests pass.
- [x] Essence Furnace, Seed Reprocessor, Soul Extractor, and Ore Infuser
  execute real production recipes across progress save/reload, with upgrade
  timing and exact input/output assertions.
- [x] A mature crop is harvested after Harvester progress save/reload; the
  upgrade changes timing, output is retained, and energy is consumed.
- [x] Enchanter and Tinkering Table menus consume real inputs and produce an
  enchanted book and installed augment.
- [x] Infusion and Awakening multiblocks consume every real ingredient and
  produce their configured outputs across active progress save/reload.
- [x] The Soulium Spawner consumes its recipe and creates exactly one
  configured entity across progress save/reload.
- [x] All powered processor inventory/energy save fields round-trip.
- [x] Awakening altar inventory/active state, extended essence-vessel stack,
  Soulium Spawner input/energy, and Harvester current/legacy fuel keys
  round-trip.
- [x] Sided item insertion/extraction, output rejection, slot limits, nested
  transaction rollback, one-shot commit notification, and energy
  commit/rollback are covered; a committed top-face insertion plus committed
  energy also drives a real furnace recipe to completion.
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
