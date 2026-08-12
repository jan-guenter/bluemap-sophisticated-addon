# Visual coverage

This document defines the accepted prerelease's positive scope. Acceptance is
bounded to the exact identities in the compatibility and provenance records.

## Sophisticated Storage

The exact closed catalog contains 59 blocks:

- six barrel tiers and four limited-barrel layouts across all six tiers;
- six chest tiers and six shulker-box tiers;
- controller, storage link, storage I/O, input, output, and decoration table;
- eleven wood-specific storage connectors.

The supported stable projection includes block shape, tier finish, facing,
flat/limited barrel layout, packed/lock/tier overlays, main and accent colors,
closed single/left/right chest topology, and valid persisted camouflage
materials. Controller/I/O/connector material facades are in scope when their
persisted material resolves to an admitted static installed block model.

## Sophisticated Backpacks

The exact catalog contains the placed leather, copper, iron, gold, diamond,
and netherite backpack blocks. The supported stable projection includes
facing, tier clips, default or persisted main/accent colors, straps, and the
static empty shell for the three module positions.

## Deliberately excluded

- inventory contents, item counts, fill indicators, or capacity use;
- chest/shulker open progress and all animation;
- tank fluid and fill level;
- battery charge and active/unlit changes;
- displayed or held items;
- upgrade activity, particles, sound, and other transient state;
- moving Create contraptions and Sophisticated Storage In Motion entities;
- arbitrary or unresolved camouflage, malformed NBT, unknown tiers, and
  mismatched block/item identities.

Excluded or malformed cases use atomic stock fallback. No partially emitted
custom geometry may remain in the tile model.
