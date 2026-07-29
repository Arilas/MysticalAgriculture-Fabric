# Mystical Agriculture — unofficial Fabric fork

This repository is the unofficial Fabric port of
[BlakeBr0/MysticalAgriculture](https://github.com/BlakeBr0/MysticalAgriculture).
It targets Minecraft 26.2, Fabric Loader 0.19.3 or newer, Fabric API
0.155.2+26.2 or newer, and Java 25. It is not an official Blake's Mods release.

The port preserves the upstream `mysticalagriculture` namespace, registry IDs,
recipe IDs, data-component IDs, and machine save keys wherever the loader API
allows. See [the parity matrix](docs/porting/parity-matrix.md) for intentional
differences and save/add-on impact.

## Required sibling checkout

The build uses the matching unofficial Fabric fork of Cucumber as a Gradle
composite build. Clone both repositories as siblings:

```text
mods/
├── Cucumber-Fabric/
└── MysticalAgriculture/
```

```bash
git clone --branch fabric/26.2 https://github.com/Arilas/Cucumber.git Cucumber-Fabric
git clone --branch fabric/26.2 https://github.com/Arilas/MysticalAgriculture-Fabric.git MysticalAgriculture
cd MysticalAgriculture
./gradlew clean build
```

For a different layout, pass an absolute or relative override:

```bash
./gradlew clean build -Pcucumber_project_path=/path/to/Cucumber-Fabric
```

Relevant repositories:

- [Fabric fork](https://github.com/Arilas/MysticalAgriculture-Fabric)
- [Mystical Agriculture upstream](https://github.com/BlakeBr0/MysticalAgriculture)
- [Cucumber Fabric fork](https://github.com/Arilas/Cucumber)
- [Cucumber upstream](https://github.com/BlakeBr0/Cucumber)

## Development commands

```bash
./gradlew clean build
./gradlew clean runDatagen
./gradlew runClient
./gradlew runServer
./gradlew runGameTest
./gradlew compileApiCompatibilityJava
```

Optional integrations are compile-only and can be added independently:

```bash
./gradlew runClient -Penable_jei_runtime
./gradlew runClient -Penable_jade_runtime
./gradlew runClient -Penable_jei_runtime -Penable_jade_runtime
```

JEI and Jade are supported but are not required at runtime. The One Probe,
CraftTweaker, and executable Patchouli integration are deferred because no
reviewed Minecraft 26.2 Fabric dependency was accepted for this port. Existing
Patchouli book JSON remains inert data.

For verification and release work, use the
[server smoke checklist](docs/porting/server-smoke-checklist.md),
[client smoke checklist](docs/porting/client-smoke-checklist.md),
[optional-integration checklist](docs/porting/optional-integrations-checklist.md),
and [release checklist](docs/porting/release-checklist.md).

## Upstream synchronization

This fork retains upstream history and is designed to receive normal merges.
Do not rebase away the upstream ancestry. Follow
[the upstream synchronization guide](docs/porting/upstream-sync.md).

## License

[MIT License](LICENSE)
