/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SophisticatedRendererTextureAliasesTest {

    @Test
    void aliasesBarrelTexturesForVanillaCubeBottomTopFaces() {
        Key top = Key.parse("sophisticatedstorage:block/oak_barrel_top");
        Key side = Key.parse("sophisticatedstorage:block/oak_barrel_side");
        Key bottom = Key.parse("sophisticatedstorage:block/oak_barrel_bottom");
        Map<String, Key> textures = new HashMap<>(Map.of(
                "top", top,
                "side", side,
                "bottom", bottom
        ));

        SophisticatedRenderer.aliasBarrelTextures(textures);

        assertEquals(top, textures.get("up"));
        assertEquals(bottom, textures.get("down"));
        assertEquals(side, textures.get("north"));
        assertEquals(side, textures.get("east"));
        assertEquals(side, textures.get("south"));
        assertEquals(side, textures.get("west"));
    }
}
