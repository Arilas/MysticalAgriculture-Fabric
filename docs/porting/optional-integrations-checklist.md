# Mystical Agriculture Fabric optional-integration checklist

Target: Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.155.2+26.2,
Java 25, JEI 30.14.0.90, and Jade 26.2.9+fabric (Curse file 8347273).

A checked runtime item means it was observed in a real client. Registration,
compilation, or an API contract alone is not treated as a completed UI
interaction.

## Packaging and absence safety

- [x] JEI and Jade are compile-only and suggested, not required dependencies.
- [x] The normal client reaches the title screen with neither optional mod.
- [x] The release artifact contains the Mystical Agriculture integration
  classes and does not vendor JEI or Jade API classes.
- [x] Common and client Fabric entrypoints do not link JEI or Jade classes.
- [x] The removed TOP and CraftTweaker integrations are absent from the release
  artifact.

## JEI

- [x] The client reaches the title screen with JEI 30.14.0.90.
- [x] The plugin implements JEI's Fabric discovery API.
- [x] All eight recipe categories compile against the pinned JEI API.
- [x] All six menu-backed recipe types register transfer handlers with their
  concrete menu slot ranges.
- [x] Recipe catalysts, GUI click areas, item subtype handling, category assets,
  and dynamically synchronized recipe collections remain wired.
- [ ] A local world was joined with JEI enabled.
- [ ] All eight categories were opened and visually inspected.
- [ ] Recipe transfer was exercised from each supported menu.

## Jade

- [x] The client reaches the title screen with Jade 26.2.9+fabric.
- [x] Jade discovers the Mystical Agriculture `IWailaPlugin`.
- [x] Jade registers crop, Inferium crop, and infused farmland tooltip
  components.
- [x] Jade registers native progress providers for the Essence Furnace,
  Harvester, Reprocessor, Soul Extractor, Soulium Spawner, and Ore Infuser.
- [x] Jade's universal storage providers remain available for inventories and
  Team Reborn Energy storage exposed by the Fabric port.
- [ ] A local world was joined with Jade enabled.
- [ ] Crop and farmland tooltips were visually inspected.
- [ ] Machine progress, energy, and inventory views were visually inspected.

## Four runtime matrices

| Runtime | Title/resource reload | World joined | Integration observation |
| --- | --- | --- | --- |
| Neither | Pass | Not run | No optional-entrypoint or classloading failure |
| JEI only | Pass | Not run | JEI 30.14.0.90 loaded; JEI atlas created |
| Jade only | Pass | Not run | Jade 26.2.9+fabric loaded the Mystical Agriculture plugin and registered its providers |
| JEI + Jade | Pass | Not run | Both exact versions loaded together; Jade plugin/provider registration and JEI atlas creation completed |

Each launch completed the ResourceManager reload through `Sound engine started`
and was stopped intentionally at the title screen. The development session's
Mojang/Realms 401 messages are unrelated to the mod. JEI also logs an upstream
warning for its suggested Amecs mixin target when Amecs is absent; it does not
prevent title-screen startup.

The world and interactive UI checks remain open because `run/saves` contains no
local world and the Java/LWJGL Minecraft window was not exposed to the available
desktop-control tooling. A human smoke pass should complete the unchecked
items.

## Deferred integrations

The One Probe and CraftTweaker executable integrations are removed from the
Fabric source set. Their NeoForge-only dependency and repository configuration
is not retained.

Patchouli book files remain only as inert JSON/resource assets. Executable
Patchouli integration is deferred until a reviewed Minecraft 26.2 Fabric
artifact is accepted; Patchouli is neither a required nor a compile/runtime
dependency of this port.
