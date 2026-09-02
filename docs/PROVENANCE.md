# Provenance

The current evidence lock is machine-readable in
`src/main/resources/bluemap-sophisticated/profiles/exact-artifacts.json` and
`provenance/upstreams.json`.

The BlueMap host is locked to feature-backport commit
`7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` and API commit
`285c9a60eff3ac2b0cab308ce1058d1565be0971`. Four Adapter API sources are
compiled from gitlink `e81f08bc4bfbf02d810ec8949a019130e2e61634` and source
tree `2f974c9bb2ba13888d69682f86f30f58922d30eb`; the settings preflight rejects
an uninitialized, changed, dirty, or mismatched module checkout.

The face-light sampler is compiled from Render Core `0.1.0-alpha.2`, gitlink
`24b84efdc8235f3f1323e1a8e9fd033080e3a79e`, source tree
`424040931680fb82d37693f893ca887c0ed48eae`. The same settings preflight and
archive-boundary rules reject a missing, dirty, mismatched, duplicate, legacy,
or unexpected render-core source/class.

The All the Mons 1.2.0 export and server runtime ledger establish the exact
filenames, project/file IDs, sizes, SHA-1 values, and pack baseline. Each of
the three runtime JARs was independently acquired and measured for SHA-256
and SHA-512. The verifier checks complete file identity, NeoForge metadata,
dependencies, selected exact class evidence, and the packaged per-route
resource path/size/hash manifests.

The Sophisticated JARs declare All Rights Reserved. Their public repositories
are reference locations only; no immutable source tag/build attestation has
been correlated to these shipped build numbers. The exact runtime JARs are
therefore authoritative for this profile.

The implementation is clean-room MIT. Inspection established public-format
facts such as registry IDs, persisted field names/domains, model/resource
paths, byte sizes, and hashes. No upstream source, compiled class, model,
texture, translation, capture, or precomputed mesh is committed or published.

The historical All the Mons 1.1.1 knowledge snapshot was used only to locate
the family during discovery. It is not evidence for this 1.2.0 profile.
