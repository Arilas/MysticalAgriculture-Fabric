# Fabric 26.2 parity matrix

Baseline: upstream `26.1` commit
`cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462`.
Audited port snapshot:
`14e9e7a58f36ecccd48127fdd88d0d4b745d894f`.

The categories below are ordered, disjoint, and exhaustive for that snapshot:
893 changed files, zero unreviewed. `ParityCategoryRulesTest` runs the same
rules against the Git diff, fails on zero or multiple matches, and pins every
aggregate below. Generated/resource families remain covered by their schema and
datagen contracts instead of a duplicated 893-row manifest.

## Exhaustive file classification

| Category | Path rule / explicit exceptions | Files | Status |
| --- | --- | ---: | --- |
| Build, docs, metadata | Root Gradle/settings/git files; `.github/**`; `README.md`; `docs/**`; Fabric metadata, mixins, access widener; deleted NeoForge metadata | 21 | Reviewed |
| Registry, API, bootstrap | `MysticalAgriculture.java`; `api/**`, `registry/**`, `init/**`, `lib/**`, `config/**` | 40 | Reviewed |
| Data, recipes, worldgen | `src/generated/resources/**`, `src/main/resources/data/**`, `crafting/**`, `data/**`, `world/**`, `util/RecipeIngredientCache.java` | 635 | Reviewed |
| Machines, storage, network | `tileentity/**`, `container/**`, `network/**`, `api/machine/**`, storage/reload handlers, machine/altar/vessel blocks | 50 | Reviewed |
| Gameplay | `augment/**`, `item/**`, `mixin/**`, remaining gameplay handlers/blocks, `api/tinkering/**`, `api/util/**` | 49 | Reviewed |
| Client | `MysticalAgricultureClient.java`, `client/**`, staff model asset, deleted NeoForge client-extension bridge | 41 | Reviewed |
| Optional integrations | `compat/**`, including deleted TOP/CraftTweaker sources | 17 | Reviewed |
| Tests and integration fixtures | `src/test/**`, `src/gametest/**`, `src/task*/**`, `src/apiCompatibility/**` | 40 | Reviewed |
| **Total** | Every entry from `git diff --name-status <baseline>` | **893** | **0 unreviewed** |

Data detail: 136 generated recipe resources, five deleted stale upstream
datagen cache manifests, and 459 main data-resource files changed. All current
JSON parses; 590 recipe files declare Fabric load conditions and two dagger
recipes use Fabric's `fabric:components` custom-ingredient schema. None uses a
NeoForge condition/type. Three deleted biome-modifier JSON files are replaced
by Fabric biome modifications. The two remaining `#forge:` strings are
intentional legacy cross-loader material tag aliases, not loader metadata.
Generator cache manifests are ignored and excluded from artifacts.

## Behavioral parity and intentional differences

| Subsystem | Upstream behavior | Fabric behavior / reason | Verification | Add-on or save impact |
| --- | --- | --- | --- | --- |
| Bootstrap and metadata | NeoForge mod/event bus, TOML metadata, access transformer | Fabric common/client/datagen entrypoints, `fabric.mod.json`, mixins, access widener; required by loader | Clean client/server launches; artifact inspection | Loader metadata changes only; IDs and data namespace retained |
| Registries | Deferred holders and register events | Direct vanilla registration in deterministic order with duplicate/freeze diagnostics; dynamic crops retain upstream all-essence then all-seed order | Registry tests and real startup counts | Registry IDs stable; add-ons must register during plugin lifecycle |
| Plugin discovery | NeoForge annotation/scan integration | Fabric `mysticalagriculture:plugin` entrypoint; legacy annotation is deprecated because Fabric cannot scan it safely | 18 plugin/registry tests, including fatal-error propagation | Add-ons add a Fabric metadata entrypoint; API objects/IDs retained |
| Public recipe API | NeoForge `SizedIngredient` | Loader-neutral `IngredientWithCount` with map/network codecs | API consumer compile; codec and loader tests | Source signature change is necessary to remove NeoForge API; serialized count/ingredient fields retained |
| Conditions and data | NeoForge conditions and biome modifiers | Fabric conditions and biome modification API | Datagen execution, JSON parse, 590 condition files plus 2 custom-ingredient schema files, server datapack load | Recipe IDs/results retained; no save impact |
| Enchanter matching | NeoForge matcher accepts unordered inputs; remaining stacks were positional | Fabric backtracking match and consumption use the same assignment, preventing wrong-slot consumption | Red/green unordered-consumption regression | Correctness fix; recipe/save format unchanged |
| Machines and storage | NeoForge item/energy capabilities | Fabric Transfer API sided storage and Team Reborn Energy with transaction rollback/commit semantics | Storage unit tests and integrated GameTests | Inventory, energy, progress, upgrade and output keys retained; Harvester reads legacy fuel key |
| Menus | NeoForge extended menu factories | Fabric extended menu codecs with level-aware block-entity validation | Menu boundary and integrated Spawner transfer tests | Container IDs and save data unaffected |
| Networking and reload | NeoForge payload registration/events | Fabric typed payload codecs, server validation, login/reload synchronization and disconnect cleanup | Payload/AOE tests, client state tests, real `/reload` | Wire protocol is loader-specific; server remains authoritative |
| Gameplay callbacks | NeoForge event bus/capability hooks | Fabric events plus two narrow mixins for XP pickup and item attribute iteration | 22 production-backed gameplay GameTests | Gameplay intent and component/save IDs retained |
| Client | NeoForge client events/extensions | Fabric screen, renderer, model-loading, tint, tooltip, HUD and key callbacks | Client contracts, standard title/resource reload, four real world joins | No save impact; unchecked visual presentation remains manual |
| JEI | NeoForge JEI plugin | Fabric JEI discovery; nine serializers explicitly synchronize recipes | API/layout tests and JEI world join with nonzero recipe counts | Optional, no save impact |
| Jade | NeoForge Waila bridge | Fabric Jade plugin with native item, energy and progress providers | Provider tests and Jade world join | Optional, read-only, no save impact |
| TOP/CraftTweaker | Executable NeoForge integrations | Removed until reviewed Fabric 26.2 dependencies exist | Artifact/source absence checks | Add-on integration unavailable; core saves unaffected |
| Patchouli | Resource book plus executable integration | Inert book JSON retained; no runtime dependency | Dependency/artifact inventory | Book UI unavailable; world data unaffected |
| Configuration | NeoForge configuration facilities | Loader-neutral JSON-backed configuration | Config tests and startup | File format/path differs; gameplay defaults preserved |
| Build dependency | Published/private Maven paths available upstream | Local sibling Cucumber composite build, no private credentials | Local build and clean-room CI layout | Development layout requirement only |

## Deferred items disposition

- Plugin fatal errors: fixed; only `RuntimeException` is wrapped and registry
  state is not fabricated after unrecoverable VM errors.
- Dynamic crop item ordering: fixed to upstream two-pass order.
- Enchanter remaining items: fixed to consume the same unordered assignment
  that established the multiplier.
- Standard launch argfile: fixed for client, server, GameTest, and datagen by
  using Loom's bare classpath; a clean launch failed before the change and
  reached resource reload both with new and reused configuration-cache entries
  after it.
- Production menus/persistence/storage: covered by normal integrated GameTests
  now that the complete source set compiles.
- Compiler warnings: local unchecked Jade proxy warnings were isolated to typed
  cast helpers. Remaining warnings are inventoried API deprecations (Minecraft,
  Fabric resource reload, Cucumber, JEI), five removal warnings for the only
  GameTest helper that returns a `ServerPlayer`, one generic test-registry
  bridge cast, and JOML's Java 25 `Unsafe` warning. None is hidden in production
  with a blanket suppression.
- Visual behavior: retained as explicit human-only debt in the client and
  optional integration checklists.

## Final executable and artifact evidence

- Java 25 clean build: 92 JUnit tests passed with zero failures, errors, or
  skips; all 41 required integrated server GameTests passed.
- Standalone `runGameTest`, real integrated `runClientGameTest`, and
  API-consumer compatibility compilation passed. The client test covered join,
  `/reload`, disconnect cleanup, and reopening the same save with coherent
  synchronized recipe/cache/color state.
- Datagen reported 1,061 old and new resources, removed zero stale resources,
  and wrote zero files on the second run.
- The release JAR contains 4,360 entries and the clean-clone sources JAR
  contains 4,311.
  Both expanded artifacts were scanned for NeoForge residue, development/run
  files, generated caches, absolute local paths, test classes, and vendored
  JEI/Jade API packages. No forbidden content remains.
- Release SHA-256:
  `dd2da86f6c5d9c383aa50bc3c5016c3611d68e7c4ce61418e12584bdd640a007`.
  Sources SHA-256:
  `c4bb0bdf4cc3132ed8c16231c74c12b001a88b1df8336534d9707916797147e1`.
- Remote sibling-clone CI run `30488753759` passed from the final pushed
  Cucumber and Mystical Agriculture Fabric branches. After the first attempt
  encountered a transient Maven Central HTTP 429, unchanged rerun job
  `90701283131` completed the full build, server/client GameTests, and artifact
  upload successfully.
