/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.Map;

/** Small cuboid emitter for closed chest, shulker and barrel exterior layers. */
final class PrimitiveEmitter {

    private static final ResourcePath<Model> TRANSFORM_SENTINEL =
            new ResourcePath<>("bluemap", "block/missing");

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;

    PrimitiveEmitter(ResourcePack resourcePack, TextureGallery textureGallery) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
    }

    boolean cuboid(
            BlockNeighborhood block,
            TileModelView target,
            float minX,
            float minY,
            float minZ,
            float maxX,
            float maxY,
            float maxZ,
            Map<Direction, Key> materials,
            int argb,
            float xRotation,
            float yRotation,
            float zRotation,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        boolean emitted = false;
        emitted |= quad(block, target, Direction.DOWN, materials.get(Direction.DOWN), argb,
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ, mapColor);
        emitted |= quad(block, target, Direction.UP, materials.get(Direction.UP), argb,
                minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ, mapColor);
        emitted |= quad(block, target, Direction.NORTH, materials.get(Direction.NORTH), argb,
                maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, mapColor);
        emitted |= quad(block, target, Direction.SOUTH, materials.get(Direction.SOUTH), argb,
                minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, mapColor);
        emitted |= quad(block, target, Direction.WEST, materials.get(Direction.WEST), argb,
                minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, mapColor);
        emitted |= quad(block, target, Direction.EAST, materials.get(Direction.EAST), argb,
                maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, mapColor);

        int count = target.getTileModel().size() - start;
        if (count > 0 && (xRotation != 0F || yRotation != 0F || zRotation != 0F)) {
            target.initialize(start).transform(
                    new Variant(TRANSFORM_SENTINEL, xRotation, yRotation, zRotation)
                            .getTransformMatrix()
            );
        }
        target.initialize(start);
        return emitted;
    }

    boolean plate(
            BlockNeighborhood block,
            TileModelView target,
            Direction direction,
            float minX,
            float minY,
            float maxX,
            float maxY,
            float depth,
            Key texture,
            int argb,
            float xRotation,
            float yRotation,
            float zRotation,
            Color mapColor
    ) {
        int start = target.getTileModel().size();
        boolean emitted = switch (direction) {
            case NORTH -> quad(
                    block, target, direction, texture, argb,
                    maxX, minY, depth, minX, minY, depth,
                    minX, maxY, depth, maxX, maxY, depth, mapColor
            );
            case SOUTH -> quad(
                    block, target, direction, texture, argb,
                    minX, minY, depth, maxX, minY, depth,
                    maxX, maxY, depth, minX, maxY, depth, mapColor
            );
            default -> false;
        };
        if (emitted && (xRotation != 0F || yRotation != 0F || zRotation != 0F)) {
            target.initialize(start).transform(
                    new Variant(TRANSFORM_SENTINEL, xRotation, yRotation, zRotation)
                            .getTransformMatrix()
            );
        }
        target.initialize(start);
        return emitted;
    }

    private boolean quad(
            BlockNeighborhood block,
            TileModelView target,
            Direction direction,
            Key textureKey,
            int argb,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            Color mapColor
    ) {
        Texture texture = textureKey == null ? null : resourcePack.getTextures().get(textureKey);
        if (texture == null) {
            return false;
        }
        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);
        mesh.setUvs(start, 0F, 1F, 1F, 1F, 1F, 0F);
        mesh.setUvs(start + 1, 0F, 1F, 1F, 0F, 0F, 0F);
        int material = textureGallery.get(textureKey);
        mesh.setMaterialIndex(start, material);
        mesh.setMaterialIndex(start + 1, material);
        float red = ((argb >>> 16) & 0xFF) / 255F;
        float green = ((argb >>> 8) & 0xFF) / 255F;
        float blue = (argb & 0xFF) / 255F;
        mesh.setColor(start, red, green, blue);
        mesh.setColor(start + 1, red, green, blue);
        mesh.setAOs(start, 1F, 1F, 1F);
        mesh.setAOs(start + 1, 1F, 1F, 1F);
        LightData light = block.getLightData();
        mesh.setSunlight(start, light.getSkyLight());
        mesh.setSunlight(start + 1, light.getSkyLight());
        mesh.setBlocklight(start, light.getBlockLight());
        mesh.setBlocklight(start + 1, light.getBlockLight());
        if (direction == Direction.UP) {
            Color average = new Color().set(texture.getColorPremultiplied());
            average.r *= red;
            average.g *= green;
            average.b *= blue;
            mapColor.add(average);
        }
        return true;
    }
}
