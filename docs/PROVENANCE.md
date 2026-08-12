# Provenance

The current evidence lock is machine-readable in
`src/main/resources/bluemap-sophisticated/profiles/exact-artifacts.json` and
`provenance/upstreams.json`.

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
