/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SophisticatedSnapshotDecoderTest {

    private final SophisticatedSnapshotDecoder decoder = new SophisticatedSnapshotDecoder();

    @Test
    void expandsAggregateBarrelMaterialsBeforeLeafOverrides() {
        Map<String, Object> materials = new LinkedHashMap<>();
        materials.put("all", "minecraft:oak_planks");
        materials.put("top", "minecraft:stone");

        SophisticatedSnapshot snapshot = decoder.storage(
                Map.of("mainColor", -1, "accentColor", 0x0012_3456),
                materials,
                "OAK",
                false,
                true,
                true,
                false
        );

        assertTrue(snapshot.valid());
        assertTrue(snapshot.mainColor().isEmpty());
        assertEquals(0xFF12_3456, snapshot.accentColor().orElseThrow());
        assertEquals("oak", snapshot.woodType());
        assertEquals("minecraft:stone", snapshot.barrelMaterials().get("top"));
        assertEquals("minecraft:oak_planks", snapshot.barrelMaterials().get("side_trim"));
        assertTrue(snapshot.locked());
        assertFalse(snapshot.showTier());
    }

    @Test
    void rejectsMalformedStorageVisualFieldsAtomically() {
        assertFalse(decoder.storage(
                null,
                Map.of(),
                "oak",
                false,
                false,
                true,
                true
        ).valid());
        assertFalse(decoder.storage(
                Map.of("mainColor", "red"),
                Map.of(),
                "oak",
                false,
                false,
                true,
                true
        ).valid());
        assertFalse(decoder.storage(
                Map.of(),
                Map.of("unknown", "minecraft:stone"),
                "oak",
                false,
                false,
                true,
                true
        ).valid());
        assertFalse(decoder.storage(
                Map.of(),
                Map.of("top", "not a block"),
                "oak",
                false,
                false,
                true,
                true
        ).valid());
    }

    @Test
    void decodesExactBackpackStackAndDefaults() {
        SophisticatedSnapshot defaults = decoder.backpack(Map.of(
                "id", "sophisticatedbackpacks:backpack"
        ));
        assertTrue(defaults.valid());
        assertEquals("sophisticatedbackpacks:backpack", defaults.itemId());
        assertEquals(0xFFCC_613A, defaults.mainColor().orElseThrow());
        assertEquals(0xFF62_2E1A, defaults.accentColor().orElseThrow());

        SophisticatedSnapshot colored = decoder.backpack(Map.of(
                "id", "sophisticatedbackpacks:diamond_backpack",
                "components", Map.of(
                        "sophisticatedcore:main_color", 0x0001_0203,
                        "sophisticatedcore:accent_color", 0x0004_0506
                )
        ));
        assertTrue(colored.valid());
        assertEquals(0xFF01_0203, colored.mainColor().orElseThrow());
        assertEquals(0xFF04_0506, colored.accentColor().orElseThrow());
    }

    @Test
    void rejectsMalformedBackpackStack() {
        assertFalse(decoder.backpack(Map.of()).valid());
        assertFalse(decoder.backpack(Map.of(
                "id", "sophisticatedbackpacks:backpack",
                "components", "not-a-compound"
        )).valid());
        assertFalse(decoder.backpack(Map.of(
                "id", "sophisticatedbackpacks:backpack",
                "components", Map.of("sophisticatedcore:main_color", "red")
        )).valid());
    }
}
