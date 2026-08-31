# Release procedure

The pull-request CI is the main implementation gate and performs one complete
compile/test/package/exact-input pass. Do not repeat full local builds between
small edits.

Before tagging:

1. Confirm the reviewed commit, clean repository, exact `gradle.properties`
   version, changelog, and provenance/profile identities.
2. Confirm the PR CI passed on that commit and inspect its production JAR.
3. Record an isolated BlueMap load/render/removal lifecycle and owner visual
   acceptance. Do not infer these from a successful build.
4. Seal the accepted production JAR, sources JAR, POM, and Gradle module sizes
   and SHA-256 values in `gradle.properties` and `provenance/release.json`.
5. Merge the version change through a PR, then create the exact annotated tag
   `v<addon_version>` on the reviewed commit.

The tag workflow reacquires the exact three third-party inputs, verifies the
exact detached BlueMap and Adapter API identities, runs the same authoritative
gate, and rejects any artifact that differs from the sealed accepted bytes.
It then creates SHA-256 checksums and publishes a GitHub prerelease plus the
matching GitHub Packages Maven publication. The publish task reuses the
already-built outputs and does not repeat compilation. It does not deploy to
any server. Third-party JARs are temporary inputs and never release assets.

If the workflow fails before release creation, fix through another PR and use
a new version/tag. Never move or overwrite a published tag or release asset.
