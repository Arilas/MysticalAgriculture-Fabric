# Fabric 26.2 release checklist

No tag or release may be created from this checklist without a separate release
decision.

## Source and ancestry

- [x] Repository is a GitHub fork of `BlakeBr0/MysticalAgriculture`.
- [x] `upstream/26.1` is still
  `cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462`.
- [x] No upstream `26.2` branch existed at the audit.
- [x] The pinned upstream commit is an ancestor of `fabric/26.2`.
- [x] Cucumber commit `598ee054d6d6826b8c150d32e7bf46ca412d0ff1`
  is present on remote `Arilas/Cucumber` branch `fabric/26.2`.

## Verification

- [x] `./gradlew --stop`
- [x] `./gradlew clean runDatagen`
- [x] A second datagen run writes zero files and generated resources have zero
  unstaged diff.
- [x] `./gradlew clean build`
- [x] `./gradlew runGameTest`
- [x] `./gradlew runClientGameTest`
- [x] `./gradlew compileApiCompatibilityJava`
- [x] Standard client survives `clean` both with a new and reused
  configuration-cache entry.
- [x] Dedicated server loads, reloads, saves, stops, and restarts the retained
  world.
- [x] Clean-room sibling clone build and server/client GameTests pass from
  remote branches.

## Artifact

- [x] Release and sources JAR inventories inspected.
- [x] Fabric metadata, mixins, access widener, classes, assets, data, and
  generated resources present.
- [x] NeoForge metadata/code, access transformer, run/config files, local
  paths, test classes, caches, and vendored optional APIs absent.
- [x] `fabric.mod.json` version/dependencies/contact links verified.
- [x] Release and sources JAR SHA-256 recorded:
  - release:
    `dd2da86f6c5d9c383aa50bc3c5016c3611d68e7c4ce61418e12584bdd640a007`
  - sources:
    `c4bb0bdf4cc3132ed8c16231c74c12b001a88b1df8336534d9707916797147e1`

The inspected release JAR has 4,360 entries and contains one declared embedded
dependency, Team Reborn Energy 5.0.0. The clean-clone sources JAR has 4,311
entries. JEI and Jade integration classes/resources are part of Mystical
Agriculture, but their API implementation packages are not bundled.

## Publication readiness

- [x] Push the required Cucumber commit after explicit approval.
- [x] Push this branch after the Cucumber dependency is available remotely.
- [x] Run the clean-room sibling-clone build and server/client GameTests from those remote
  branches.

## Manual debt

- [ ] Complete unchecked visual items in `client-smoke-checklist.md`.
- [ ] Complete JEI/Jade visual and recipe-transfer items in
  `optional-integrations-checklist.md`.
- [ ] Complete unchecked end-to-end gameplay items in
  `server-smoke-checklist.md`.

Suggested future tag after approval:
`26.2-9.0.4+fabric.1`.
