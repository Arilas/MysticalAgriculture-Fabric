# Upstream synchronization

The Fabric branch is a descendant of Mystical Agriculture upstream commit
`cc1e1a3e9efdb8e9b8e327e9f5e6723fc4d15462` on `upstream/26.1`. GitHub records
`Arilas/MysticalAgriculture-Fabric` as a fork of
`BlakeBr0/MysticalAgriculture`; keep that ancestry intact.

Before every synchronization, inspect remote branches and tags. If upstream
adds a real `26.2` branch, stop and review it as a possible new merge source
before changing this procedure.

```bash
git fetch upstream --prune --tags
git branch -r --list 'upstream/*'
git rev-parse upstream/26.1

git switch 26.1
git merge --ff-only upstream/26.1

git switch fabric/26.2
git merge upstream/26.1
./gradlew clean build
./gradlew runGameTest
```

Resolve loader-boundary conflicts deliberately; do not discard Fabric changes
mechanically and do not rebase the Fabric history away from upstream. Review
the changed-file inventory and update `parity-matrix.md` after each merge.

Cucumber is also a real GitHub fork. Keep its `fabric/26.2` branch available as
the sibling composite build and verify the expected dependency before CI:

```bash
git -C ../Cucumber-Fabric fetch origin --prune --tags
git -C ../Cucumber-Fabric branch -r --contains \
  598ee054d6d6826b8c150d32e7bf46ca412d0ff1
```
