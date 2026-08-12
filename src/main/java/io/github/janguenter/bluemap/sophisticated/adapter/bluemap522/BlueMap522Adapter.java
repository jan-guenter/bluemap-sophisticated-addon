/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.sophisticated.activation.SophisticatedRuntime;
import io.github.janguenter.bluemap.sophisticated.profile.backpacks.SophisticatedBackpacks32573Profile;
import io.github.janguenter.bluemap.sophisticated.profile.storage.SophisticatedStorage1583Profile;

import java.util.ArrayList;
import java.util.List;

/** BlueMap 5.22 internal ABI boundary. */
public final class BlueMap522Adapter {

    private static final KeyedKeySet KEYS = new KeyedKeySet();
    private static final SophisticatedRuntime RUNTIME = SophisticatedRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            KEYS.renderer(),
            (pack, gallery, settings) -> new SophisticatedRenderer(
                    pack, gallery, settings, RUNTIME
            )
    );
    private static final ResourcePack.Extension<SophisticatedResourceExtension> EXTENSION =
            new SophisticatedResourceExtensionType(RUNTIME);

    private BlueMap522Adapter() {
    }

    public static synchronized boolean install() {
        List<BlockEntityType> entities = new ArrayList<>();
        SophisticatedStorage1583Profile.BLOCK_ENTITY_IDS.forEach(id -> entities.add(
                new BlockEntityType.Impl(
                        de.bluecolored.bluemap.core.util.Key.parse(id),
                        SophisticatedBlockEntityData.class
                )
        ));
        SophisticatedBackpacks32573Profile.BLOCK_ENTITY_IDS.forEach(id -> entities.add(
                new BlockEntityType.Impl(
                        de.bluecolored.bluemap.core.util.Key.parse(id),
                        SophisticatedBlockEntityData.class
                )
        ));

        if (!canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || entities.stream().anyMatch(type -> !canRegister(BlockEntityType.REGISTRY, type))) {
            RUNTIME.disableAll("registry-collision");
            return false;
        }
        if (!register(BlockRendererType.REGISTRY, RENDERER)
                || !register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.disableAll("registry-collision");
            return false;
        }
        for (BlockEntityType entity : entities) {
            if (!register(BlockEntityType.REGISTRY, entity)) {
                RUNTIME.disableAll("block-entity-registry-collision");
                return false;
            }
        }
        return true;
    }

    static boolean isExpectedDispatch(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    private static <T extends Keyed> boolean canRegister(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        return existing == null || existing == candidate;
    }

    private static <T extends Keyed> boolean register(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        if (existing == null) {
            registry.register(candidate);
            existing = registry.get(candidate.getKey());
        }
        return existing == candidate;
    }

    private record KeyedKeySet() {
        de.bluecolored.bluemap.core.util.Key renderer() {
            return de.bluecolored.bluemap.core.util.Key.parse(
                    "bluemap_sophisticated:sophisticated_shape"
            );
        }
    }
}
