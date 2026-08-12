# Agent guide for BlueMap Sophisticated Add-on

Read `/root/work/allthemons/AGENTS.md` and this file before changing this
repository. This is a standalone public MIT project, not a NeoForge mod and
not part of the root orchestration repository.

## Exact baseline

| Component | Identity |
| --- | --- |
| All the Mons | `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft / NeoForge / Java | `1.21.1` / `21.1.248` / `21` |
| BlueMap | backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d` |
| Sophisticated Core | `1.4.80.2194`, 1,673,669 bytes, SHA-256 `58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0` |
| Sophisticated Storage | `1.5.83.2017`, 1,828,640 bytes, SHA-256 `354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600` |
| Sophisticated Backpacks | `3.25.73.2027`, 1,144,235 bytes, SHA-256 `ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e` |

Do not treat the version strings alone as compatibility proof. A new pack or
mod file is a new evidence and implementation task.

## Project boundaries

- The production JAR is a plain BlueMap add-on loaded from BlueMap's packs
  directory. It must contain no NeoForge mod metadata, Mixins, nested JARs,
  client bootstrap, or third-party classes/assets.
- Sophisticated Core is a shared exact gate. Storage and Backpacks are sibling
  profiles with separate activation and failure boundaries.
- Render stable general optics only: shape, tier/material, color, orientation,
  closed pairing/placement, connections, and admitted camouflage.
- Ignore contents, item counts, tank/fluid fill, energy/charge, upgrade
  activity, fill indicators, open animation, transient display items, and
  other frequently changing state.
- Any missing, malformed, unsupported, or mismatched observation must use
  BlueMap's original blockstate/model path atomically for that block or route.
- The implementation is clean-room MIT. Never copy/adapt upstream
  Sophisticated source or package its classes, resources, or captures.

## Validation policy

Develop in complete chunks. Do not rerun compilation after each local edit.
Before review, run the one authoritative gate once with the exact three input
JARs:

```bash
gradle --no-daemon \
  -PsophisticatedCoreJar=/absolute/path/sophisticatedcore-1.21.1-1.4.80.2194.jar \
  -PsophisticatedStorageJar=/absolute/path/sophisticatedstorage-1.21.1-1.5.83.2017.jar \
  -PsophisticatedBackpacksJar=/absolute/path/sophisticatedbackpacks-1.21.1-3.25.73.2027.jar \
  clean check build generatePomFileForAddonPublication verifyPinnedArtifacts
```

Then inspect the production JAR and the exact staged paths. Do not claim a
runtime lifecycle, visual result, production deployment, or release until
that exact observation occurred and was retained. No production operation is
authorized by an implementation or release task.
