/*
 * SPDX-License-Identifier: MIT
 *
 * Model emission follows BlueMap's MIT-licensed resource-model coordinate and UV
 * conventions, with only the small hooks needed for stable Sophisticated tints.
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.Map;
import java.util.function.IntFunction;

/** Emits an already-baked ordinary JSON model with caller-owned tint and texture overrides. */
final class JsonModelEmitter {

    private static final float BLOCK_SCALE = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;

    JsonModelEmitter(ResourcePack resourcePack, TextureGallery textureGallery) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
    }

    boolean emit(
            Key modelKey,
            BlockNeighborhood block,
            TileModelView target,
            float xRotation,
            float yRotation,
            float zRotation,
            Map<String, Key> textureOverrides,
            IntFunction<Integer> tintProvider,
            Color mapColor
    ) {
        Model model = resourcePack.getModels().get(modelKey);
        if (!preflight(model, textureOverrides)) {
            return false;
        }

        int modelStart = target.getTileModel().size();
        boolean emitted = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            int elementStart = target.getTileModel().size();
            emitted |= emitElement(
                    model,
                    element,
                    block,
                    target,
                    textureOverrides,
                    tintProvider,
                    mapColor
            );
            int elementCount = target.getTileModel().size() - elementStart;
            if (elementCount > 0) {
                target.initialize(elementStart);
                target.transform(new MatrixM4f()
                        .copy(element.getRotation().getMatrix())
                        .scale(BLOCK_SCALE, BLOCK_SCALE, BLOCK_SCALE));
            }
        }

        int count = target.getTileModel().size() - modelStart;
        if (count > 0 && (xRotation != 0F || yRotation != 0F || zRotation != 0F)) {
            Variant transform = new Variant(
                    new ResourcePath<>(modelKey), xRotation, yRotation, zRotation
            );
            target.initialize(modelStart).transform(transform.getTransformMatrix());
        }
        target.initialize(modelStart);
        return emitted;
    }

    private boolean preflight(Model model, Map<String, Key> overrides) {
        if (model == null || model.getElements() == null || model.getElements().length == 0) {
            return false;
        }
        boolean hasFace = false;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            for (Face face : element.getFaces().values()) {
                hasFace = true;
                Key texture = textureKey(model, face, overrides);
                if (texture == null || resourcePack.getTextures().get(texture) == null) {
                    return false;
                }
            }
        }
        return hasFace;
    }

    private boolean emitElement(
            Model model,
            Element element,
            BlockNeighborhood block,
            TileModelView target,
            Map<String, Key> overrides,
            IntFunction<Integer> tintProvider,
            Color mapColor
    ) {
        Vector3f from = element.getFrom();
        Vector3f to = element.getTo();
        float x0 = from.getX();
        float y0 = from.getY();
        float z0 = from.getZ();
        float x1 = to.getX();
        float y1 = to.getY();
        float z1 = to.getZ();
        boolean emitted = false;
        emitted |= emitFace(model, element, Direction.DOWN, block, target, overrides,
                tintProvider, mapColor, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        emitted |= emitFace(model, element, Direction.UP, block, target, overrides,
                tintProvider, mapColor, x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        emitted |= emitFace(model, element, Direction.NORTH, block, target, overrides,
                tintProvider, mapColor, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        emitted |= emitFace(model, element, Direction.SOUTH, block, target, overrides,
                tintProvider, mapColor, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        emitted |= emitFace(model, element, Direction.WEST, block, target, overrides,
                tintProvider, mapColor, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        emitted |= emitFace(model, element, Direction.EAST, block, target, overrides,
                tintProvider, mapColor, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
        return emitted;
    }

    private boolean emitFace(
            Model model,
            Element element,
            Direction direction,
            BlockNeighborhood block,
            TileModelView target,
            Map<String, Key> overrides,
            IntFunction<Integer> tintProvider,
            Color mapColor,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        Face face = element.getFaces().get(direction);
        if (face == null) {
            return false;
        }

        Key textureKey = textureKey(model, face, overrides);
        if (textureKey == null || resourcePack.getTextures().get(textureKey) == null) {
            return false;
        }

        int start = target.add(2);
        TileModel mesh = target.getTileModel();
        mesh.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        mesh.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);

        Vector4f raw = face.getUv();
        float u0 = raw.getX() / 16F;
        float v0 = raw.getY() / 16F;
        float u1 = raw.getZ() / 16F;
        float v1 = raw.getW() / 16F;
        float[][] corners = {{u0, v1}, {u1, v1}, {u1, v0}, {u0, v0}};
        int rotation = Math.floorMod(face.getRotation() / 90, 4);
        float[] uv0 = corners[rotation];
        float[] uv1 = corners[(rotation + 1) % 4];
        float[] uv2 = corners[(rotation + 2) % 4];
        float[] uv3 = corners[(rotation + 3) % 4];
        mesh.setUvs(start, uv0[0], uv0[1], uv1[0], uv1[1], uv2[0], uv2[1]);
        mesh.setUvs(start + 1, uv0[0], uv0[1], uv2[0], uv2[1], uv3[0], uv3[1]);

        int material = textureGallery.get(textureKey);
        mesh.setMaterialIndex(start, material);
        mesh.setMaterialIndex(start + 1, material);

        int argb = tintProvider.apply(face.getTintindex());
        float red = ((argb >>> 16) & 0xFF) / 255F;
        float green = ((argb >>> 8) & 0xFF) / 255F;
        float blue = (argb & 0xFF) / 255F;
        mesh.setColor(start, red, green, blue);
        mesh.setColor(start + 1, red, green, blue);
        mesh.setAOs(start, 1F, 1F, 1F);
        mesh.setAOs(start + 1, 1F, 1F, 1F);

        LightData own = block.getLightData();
        int sky = own.getSkyLight();
        int blockLight = Math.max(own.getBlockLight(), element.getLightEmission());
        mesh.setSunlight(start, sky);
        mesh.setSunlight(start + 1, sky);
        mesh.setBlocklight(start, blockLight);
        mesh.setBlocklight(start + 1, blockLight);

        if (direction == Direction.UP) {
            Texture texture = resourcePack.getTextures().get(textureKey);
            Color average = new Color().set(texture.getColorPremultiplied());
            average.r *= red;
            average.g *= green;
            average.b *= blue;
            mapColor.add(average);
        }
        return true;
    }

    private static Key textureKey(Model model, Face face, Map<String, Key> overrides) {
        String reference = face.getTexture().getReferenceName();
        if (reference != null && overrides.containsKey(reference)) {
            return overrides.get(reference);
        }
        ResourcePath<Texture> path = face.getTexture().getTexturePath(model.getTextures()::get);
        return path;
    }
}
