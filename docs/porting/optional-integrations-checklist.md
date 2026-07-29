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
- [x] A local world was joined with JEI enabled.
- [ ] All eight categories were opened and visually inspected.
- [ ] Recipe transfer was exercised from each supported menu.

## Jade

- [x] The client reaches the title screen with Jade 26.2.9+fabric.
- [x] Jade discovers the Mystical Agriculture `IWailaPlugin`.
- [x] Jade registers crop, Inferium crop, and infused farmland tooltip
  components.
- [x] Jade registers native progress providers for the Essence Furnace,
  Harvester, Reprocessor, Soul Extractor, Soulium Spawner, and Ore Infuser.
  The custom server-provider ID has a matching client extension that converts
  the synchronized data through Jade's native progress renderer.
- [x] Jade registers read-only native item snapshots for all 11 storage-owning
  block entities and native Team Reborn Energy snapshots for all six powered
  machines. The providers use Jade's built-in item and energy view IDs without
  relaxing null-side Fabric automation.
- [x] A local world was joined with Jade enabled.
- [ ] Crop and farmland tooltips were visually inspected.
- [ ] Machine progress, energy, and inventory views were visually inspected.

## Four runtime matrices

| Runtime | Title/resource reload | World joined | Integration observation |
| --- | --- | --- | --- |
| Neither | Pass | Pass | A pristine disposable save joined with neither optional mod and no optional-entrypoint or classloading failure |
| JEI only | Pass | Pass | JEI 30.14.0.90 started after join; 8 categories, 11 stations, 6 transfers, 6 click areas, and synchronized recipe counts `124/20/44/137/37/22/18` registered |
| Jade only | Pass | Pass | Jade 26.2.9+fabric loaded the Mystical Agriculture plugin, registered 11 item, 6 energy, 6 progress, 3 block-tooltip providers, and the progress renderer, then received server config |
| JEI + Jade | Pass | Pass | Both exact versions loaded together; the same nonzero JEI collections registered and Jade completed its server config handshake |

The disposable client save was copied from the already-generated compatible
`run/world` into `run/saves` and joined with Minecraft's
`--quickPlaySingleplayer` option. No dedicated-server EULA action was performed
for this verification. Each launch completed resource reload, started an
integrated 26.2 server, logged the local player in, and emitted `joined the
game`; each process was then stopped intentionally.

The first JEI world join exposed that Mystical Agriculture's serializers had
not opted into Fabric recipe synchronization: every JEI recipe collection was
zero. All nine custom serializers now use Fabric
`RecipeSynchronization.synchronizeRecipeSerializer`; the repeated JEI-only and
combined joins registered `124` infusion, `20` awakening, `44` enchanter, `137`
reprocessor, `37` soul extractor, `22` soulium spawner, and `18` ore infuser
recipes. The core-only runtime has no crops with crux blocks, so zero crux
recipes is expected.

The executable API suite also constructs every JEI category and validates every
slot role/coordinate, every catalyst, recipe list, click area, transfer range,
and subtype registration. Jade tests call the native providers against real
production block entities and handlers, including a powered Essence Furnace,
the 512-item Soulium Spawner slot, infusion/awakening altar and pedestal
content, essence-vessel content, empty/wrong targets, and immutable snapshots.

Interactive appearance checks remain open because Computer Use did not expose
the Java/LWJGL window. After the client was running, `list_apps` returned only
Arc, ChatGPT, and Finder; direct `get_app_state` probes for `java`, `Minecraft`,
`Minecraft 26.2`, and `org.lwjgl.glfw` each returned
`SkyComputerUseError: Invalid app`. A human smoke pass should complete only the
unchecked visual and recipe-transfer items.

The development session's Mojang/Realms 401 messages are unrelated to the mod.
JEI also logs an upstream warning for its suggested Amecs mixin target when
Amecs is absent; it does not prevent startup or world join.

## Deferred integrations

The One Probe and CraftTweaker executable integrations are removed from the
Fabric source set. Their NeoForge-only dependency and repository configuration
is not retained.

Patchouli book files remain only as inert JSON/resource assets. Executable
Patchouli integration is deferred until a reviewed Minecraft 26.2 Fabric
artifact is accepted; Patchouli is neither a required nor a compile/runtime
dependency of this port.
