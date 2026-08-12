# Compatibility

Compatibility is intentionally exact and evidence-locked.

| Component | Accepted identity |
| --- | --- |
| All the Mons | `1.2.0`, repository commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.248` |
| Java | `21` |
| BlueMap | upstream `5.22` at `fe5115d5548a30d34175b8e0449aaca280af199f`, or the exact ATM backport at `9be321df995a1103808621d529eb72773e719d4d` |
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
