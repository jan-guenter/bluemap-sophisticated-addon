# BlueMap Sophisticated Add-on

[![CI](https://github.com/jan-guenter/bluemap-sophisticated-addon/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/jan-guenter/bluemap-sophisticated-addon/actions/workflows/ci.yml)

An exact-profile BlueMap 5.23 feature-backport add-on for the stable world
appearance of Sophisticated Storage and Sophisticated Backpacks.

## Status and compatibility

Version `0.1.0-alpha.2` is an unpublished BlueMap 5.23 adapter migration
candidate for the same owner-accepted rendering scope. Version
`0.1.0-alpha.1` remains the latest published release.

The candidate production JAR is 116,914 bytes with SHA-256
`8d434eff624a831ac1327e9b17347fa6b4325c02a001be418fe6593b6c300dab`.

- All the Mons `1.2.0`, Minecraft `1.21.1`, NeoForge `21.1.248`, Java `21`;
- BlueMap feature backport
  `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`;
- Sophisticated Core `1.4.80.2194`;
- Sophisticated Storage `1.5.83.2017`;
- Sophisticated Backpacks `3.25.73.2027`.

The candidate compiles the four exact BlueMap 5.23 Adapter API sources from
commit `e81f08bc4bfbf02d810ec8949a019130e2e61634` and source tree
`2f974c9bb2ba13888d69682f86f30f58922d30eb`. It does not bundle or install a
standalone Adapter API JAR. Renderer, profile, resource, gallery, and stock
fallback behavior remain local and unchanged.

The runtime checks exact JAR sizes and SHA-256 hashes. A same-named or
same-version file with different bytes stays inactive. Storage and Backpacks
are independently routed, while both require the exact Core artifact.

## Visual scope

The target is the stable silhouette and finish players recognize from the
client:

- all 59 exact Storage block IDs, including tiered chests, barrels, limited
  barrels, shulker boxes, controller/I/O blocks, decoration table, and the 11
  wood storage connectors;
- all six placed backpack tiers;
- tier/material skins, main/accent color, orientation, closed chest pairing,
  stable module shells, and admitted static camouflage materials.

Inventory contents, counts, fill indicators, open animation, fluid surfaces,
battery charge, display items, upgrade activity, particles, and moving
contraption/entity integrations are deliberately excluded. Unknown or
malformed data falls back to BlueMap's original rendering for the whole block.

See [coverage](docs/COVERAGE.md), [architecture](docs/ARCHITECTURE.md),
[compatibility](docs/COMPATIBILITY.md), and [provenance](docs/PROVENANCE.md).

## Build

Clone with submodules, then use Java 21, Gradle 9.6.1, and the exact sibling
BlueMap checkout. Supply the exact three operator-downloaded JARs to the
single authoritative gate:

```bash
gradle --no-daemon \
  -PbluemapSourcePath=/absolute/path/to/bluemap-backport \
  -PsophisticatedCoreJar=/absolute/path/sophisticatedcore-1.21.1-1.4.80.2194.jar \
  -PsophisticatedStorageJar=/absolute/path/sophisticatedstorage-1.21.1-1.5.83.2017.jar \
  -PsophisticatedBackpacksJar=/absolute/path/sophisticatedbackpacks-1.21.1-3.25.73.2027.jar \
  clean check build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyPinnedArtifacts
```

The build never redistributes those inputs. CI downloads them ephemerally,
verifies their metadata, complete hashes, and exact resource closures, then
discards them.

Tagged releases publish the production/source JARs, POM, module metadata, and
checksums on GitHub Releases and the matching Maven coordinates
`io.github.jan-guenter:bluemap-sophisticated-addon:<version>` on GitHub
Packages. Tag names must equal `v<addon_version>`.

## Installation

After a reviewed prerelease exists, place only its production JAR in
BlueMap's `config/bluemap/packs` directory and restart the JVM. The add-on is
not a NeoForge mod and does not belong in the server `mods` directory.

Removing the JAR and restarting restores stock BlueMap behavior. No world or
player data is written. See [rollback](docs/ROLLBACK.md).

## License

The add-on is independently written and released under the [MIT License](LICENSE).
Third-party software and resource packs are not bundled; see
[THIRD_PARTY.md](THIRD_PARTY.md) and [NOTICE.md](NOTICE.md).
