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
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Resolves a persisted decorative block id to a representative face texture. */
final class BlockMaterialResolver {

    private final ResourcePack resourcePack;

    BlockMaterialResolver(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
    }

    Optional<Key> resolve(String blockId, Direction direction) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource =
                resourcePack.getBlockStates().get(Key.parse(blockId));
        if (resource == null) {
            return Optional.empty();
        }
        AtomicReference<Variant> selected = new AtomicReference<>();
        resource.forEach(variant -> selected.compareAndSet(null, variant));
        Variant variant = selected.get();
        if (variant == null) {
            return Optional.empty();
        }
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (model == null || model.getElements() == null) {
            return Optional.empty();
        }
        Key fallback = null;
        for (Element element : model.getElements()) {
            if (element == null) {
                continue;
            }
            Face face = element.getFaces().get(direction);
            Key resolved = texture(model, face);
            if (resolved != null) {
                return Optional.of(resolved);
            }
            if (fallback == null) {
                for (Face candidate : element.getFaces().values()) {
                    fallback = texture(model, candidate);
                    if (fallback != null) {
                        break;
                    }
                }
            }
        }
        return Optional.ofNullable(fallback);
    }

    private Key texture(Model model, Face face) {
        if (face == null) {
            return null;
        }
        ResourcePath<Texture> path = face.getTexture().getTexturePath(model.getTextures()::get);
        return path != null && resourcePack.getTextures().get(path) != null ? path : null;
    }
}
