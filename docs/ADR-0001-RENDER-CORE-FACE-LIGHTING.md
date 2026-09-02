# ADR 0001: Shared face-light sampling

## Decision

Compile `FaceLighting` from the exact `bluemap-addon-render-core`
`0.1.0-alpha.2` source gitlink. Remove the byte-equivalent local helper and
keep all Sophisticated-specific emitters, profiles, resources, and fallback
logic local.

## Consequences

The production and sources JARs each contain exactly one allowed render-core
class/source family. The settings preflight rejects a missing, dirty, changed,
or source-tree-mismatched module. No standalone module JAR is installed or
nested, and the accepted visual behavior is unchanged.
