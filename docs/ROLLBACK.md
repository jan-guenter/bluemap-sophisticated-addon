# Removal and stock restoration

This add-on writes no world, player, or required configuration state. To
restore stock BlueMap behavior:

1. stop the BlueMap-hosting JVM;
2. remove only the `bluemap-sophisticated-addon-<version>.jar` from
   `config/bluemap/packs`;
3. restart the JVM and rerender affected map regions if required by the host.

No legacy 1.1.1 artifact, migration, or version fallback is maintained. A
runtime profile mismatch already leaves the relevant route on BlueMap's
original resource path.
