/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.map.hires.block.color.BlockColorCalculator;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Resolves a persisted decorative block id to exact per-face texture and tint data. */
final class BlockMaterialResolver {

    private final ResourcePack resourcePack;
    private final BlockColorCalculator blockColorCalculator;

    BlockMaterialResolver(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
        this.blockColorCalculator = resourcePack.createBlockColorCalculator();
    }

    Optional<ResolvedBlockMaterial> resolve(String blockId, BlockNeighborhood block) {
        BlockState materialState = new BlockState(Key.parse(blockId));
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource =
                resourcePack.getBlockStates().get(materialState.getId());
        if (resource == null) {
            return Optional.empty();
        }
        if (resource.getMultipart() != null) {
            return Optional.empty();
        }
        List<Variant> variants = new ArrayList<>();
        resource.forEach(variants::add);
        if (variants.isEmpty()) {
            return Optional.empty();
        }

        Color tint = blockColorCalculator.getBlockColor(block, materialState, new Color());
        int tintArgb = tint.getInt() | 0xFF00_0000;
        ResolvedBlockMaterial resolved = null;
        for (Variant variant : variants) {
            Optional<ResolvedBlockMaterial> candidate = resolveVariant(variant, tintArgb);
            if (candidate.isEmpty()) {
                return Optional.empty();
            }
            ResolvedBlockMaterial candidateMaterial = candidate.orElseThrow();
            if (variant.isTransformed() && !canIgnoreTransform(candidateMaterial)) {
                return Optional.empty();
            }
            if (resolved != null && !resolved.equals(candidateMaterial)) {
                return Optional.empty();
            }
            resolved = candidateMaterial;
        }
        return Optional.ofNullable(resolved);
    }

    static boolean canIgnoreTransform(ResolvedBlockMaterial material) {
        ResolvedBlockMaterial.Face reference = material.face(Direction.DOWN);
        if (reference == null) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (!reference.equals(material.face(direction))) {
                return false;
            }
        }
        return true;
    }

    private Optional<ResolvedBlockMaterial> resolveVariant(Variant variant, int tintArgb) {
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (model == null || model.getElements() == null) {
            return Optional.empty();
        }
        Map<Direction, ResolvedBlockMaterial.Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            Face selectedFace = firstFace(model, direction);
            Key texture = texture(model, selectedFace);
            if (selectedFace == null || texture == null) {
                return Optional.empty();
            }
            int argb = selectedFace.getTintindex() >= 0 ? tintArgb : 0xFFFF_FFFF;
            faces.put(direction, new ResolvedBlockMaterial.Face(texture, argb));
        }
        return Optional.of(new ResolvedBlockMaterial(faces));
    }

    private static Face firstFace(Model model, Direction direction) {
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            Face face = element.getFaces().get(direction);
            if (face != null) {
                return face;
            }
        }
        return null;
    }

    private Key texture(Model model, Face face) {
        if (face == null) {
            return null;
        }
        ResourcePath<Texture> path = face.getTexture().getTexturePath(model.getTextures()::get);
        return path != null && resourcePack.getTextures().get(path) != null ? path : null;
    }
}
