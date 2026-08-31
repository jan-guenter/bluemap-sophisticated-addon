# Compatibility

Compatibility is intentionally exact and evidence-locked.

| Component | Accepted identity |
| --- | --- |
| All the Mons | `1.2.0`, repository commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.248` |
| Java | `21` |
| BlueMap | feature backport `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971` |
| BlueMap Adapter API | `0.1.0-alpha.2`, gitlink `e81f08bc4bfbf02d810ec8949a019130e2e61634`, source tree `2f974c9bb2ba13888d69682f86f30f58922d30eb` |
| Sophisticated Core | `sophisticatedcore-1.21.1-1.4.80.2194.jar`, 1,673,669 bytes, SHA-256 `58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0` |
| Sophisticated Storage | `sophisticatedstorage-1.21.1-1.5.83.2017.jar`, 1,828,640 bytes, SHA-256 `354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600` |
| Sophisticated Backpacks | `sophisticatedbackpacks-1.21.1-3.25.73.2027.jar`, 1,144,235 bytes, SHA-256 `ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e` |

At runtime, a child route activates only after its exact file and the exact
Core file are visible in BlueMap's resource roots. Resource baking also
requires the child profile's path/size/hash closure and required textures.

This table does not claim compatibility with later pack releases, other
builds that share a semantic version, legacy All the Mons 1.1.1 artifacts, or
the optional Create/In Motion integrations. Each new byte identity requires a
new profile and review.
