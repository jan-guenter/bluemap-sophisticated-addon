/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockMaterialResolverTest {

    @Test
    void ignoresTransformsOnlyForDirectionInvariantMaterials() {
        ResolvedBlockMaterial.Face stone = new ResolvedBlockMaterial.Face(
                Key.parse("minecraft:block/stone"), 0xFFFF_FFFF
        );
        Map<Direction, ResolvedBlockMaterial.Face> isotropicFaces = faces(stone);

        assertTrue(BlockMaterialResolver.canIgnoreTransform(
                new ResolvedBlockMaterial(isotropicFaces)
        ));

        Map<Direction, ResolvedBlockMaterial.Face> directionalFaces = faces(stone);
        directionalFaces.put(Direction.NORTH, new ResolvedBlockMaterial.Face(
                Key.parse("minecraft:block/stone_top"), 0xFFFF_FFFF
        ));

        assertFalse(BlockMaterialResolver.canIgnoreTransform(
                new ResolvedBlockMaterial(directionalFaces)
        ));
    }

    private static Map<Direction, ResolvedBlockMaterial.Face> faces(
            ResolvedBlockMaterial.Face face
    ) {
        Map<Direction, ResolvedBlockMaterial.Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, face);
        }
        return faces;
    }
}
