# Architecture

The release unit is one plain BlueMap add-on JAR for the Sophisticated family.
It is not a NeoForge mod and never loads Minecraft's client renderer.

```text
BlueMap add-on entrypoint
        |
exact BlueMap 5.23 feature-backport adapter
        |
exact Sophisticated Core byte gate
        |
        +-- exact Storage gate   -> Storage route   --+
        |                                             |
        +-- exact Backpacks gate -> Backpacks route --+--> stock fallback
        |
operator-installed exact models/textures + bounded persisted visual state
```

## Family and failure boundaries

Sophisticated Core supplies common persisted semantics but owns neither child
route. Storage and Backpacks activate separately. A missing/mismatched child
artifact, missing required resource, invalid synthetic dispatch, decoder
failure, or renderer failure disables only that child route. A Core mismatch
blocks both. An adapter-registry collision disables the whole add-on.

Routes begin inactive. Only exact artifacts and complete exact resource
closures may activate them. The resource extension changes a blockstate key
only while its route is active and the ID belongs to that route's closed
catalog. Otherwise it returns the original key unchanged.

## Rendering model

The renderer reads a bounded stable projection of blockstate and block-entity
data, resolves models/textures from the operator-installed exact JARs, and
emits BlueMap geometry. It does not package upstream assets or invoke mod
client code.

Stable state includes tier/material, main/accent color, facing, closed
single/double chest topology, placed backpack shell selection, and admitted
static camouflage. Rapid state such as contents, counts, fill, charge,
activity, animations, fluids, or display items is ignored.

Any malformed or incomplete observation resets partial output and invokes
BlueMap's original resource renderer for the complete block. This is a safety
property, not a claim that stock BlueMap is visually client-equivalent for
dynamic upstream renderers.

## Resource ownership

The production JAR owns only its entrypoint, adapter/renderer code, the four
exact source-compiled Adapter API primitives, the exact source-compiled
render-core face-light sampler, synthetic dispatch blockstate, exact
identity/profile facts, and resource path/size/hash manifests. No standalone
Adapter API or Render Core JAR is bundled or installed. BlueMap and all
Minecraft/mod resources remain operator supplied. This keeps the project
clean-room MIT despite the Sophisticated artifacts' All Rights Reserved
declarations.
