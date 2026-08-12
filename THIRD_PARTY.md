# Third-party components

| Component | Use | Exact identity | Declared license | Bundled |
| --- | --- | --- | --- | --- |
| BlueMap | Compile-time and runtime host API/internal ABI | Backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`; API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971` | MIT | No |
| Sophisticated Core | Exact runtime dependency gate | `1.4.80.2194`, SHA-256 `58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0` | All Rights Reserved | No |
| Sophisticated Storage | Operator-supplied models, textures, and world data | `1.5.83.2017`, SHA-256 `354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600` | All Rights Reserved | No |
| Sophisticated Backpacks | Operator-supplied models, textures, and world data | `3.25.73.2027`, SHA-256 `ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e` | All Rights Reserved | No |
| JetBrains annotations | Compile-only transitive host dependency | `23.0.0` | Apache-2.0 | No |
| JUnit | Test framework | `5.11.4` | EPL-2.0 | No |
| Checkstyle | Source-style verification | `10.18.2` | LGPL-2.1-or-later | No |
| Gradle | Build tool used by CI | `9.4.0` | Apache-2.0 | No |

The packaged resource manifests contain only paths, byte sizes, and hashes of
operator-supplied resources. They do not contain third-party resource bytes.
