/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.sophisticated.activation.SophisticatedRuntime;
import io.github.janguenter.bluemap.sophisticated.profile.backpacks.SophisticatedBackpacks32573Profile;
import io.github.janguenter.bluemap.sophisticated.profile.storage.SophisticatedStorage1583Profile;

import java.util.ArrayList;
import java.util.List;

/** Exact BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final KeyedKeySet KEYS = new KeyedKeySet();
    private static final SophisticatedRuntime RUNTIME = SophisticatedRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            KEYS.renderer(),
            (pack, gallery, settings) -> new SophisticatedRenderer(
                    pack, gallery, settings, RUNTIME
            )
    );
    private static final ResourcePack.Extension<SophisticatedResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_sophisticated:exact_profiles"),
                    pack -> new SophisticatedResourceExtension(pack, RENDERER, RUNTIME)
            );

    private BlueMap523Adapter() {
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

        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || entities.stream().anyMatch(type ->
                        !RegistryGuard.canRegister(BlockEntityType.REGISTRY, type))) {
            RUNTIME.disableAll("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.disableAll("registry-collision");
            return false;
        }
        for (BlockEntityType entity : entities) {
            if (!RegistryGuard.register(BlockEntityType.REGISTRY, entity)) {
                RUNTIME.disableAll("block-entity-registry-collision");
                return false;
            }
        }
        return true;
    }

    static ResourcePack.Extension<SophisticatedResourceExtension> extensionType() {
        return EXTENSION;
    }

    private record KeyedKeySet() {
        Key renderer() {
            return Key.parse(
                    "bluemap_sophisticated:sophisticated_shape"
            );
        }
    }
}
