/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.sophisticated.activation.SophisticatedRuntime;
import io.github.janguenter.bluemap.sophisticated.profile.ExactModArtifactDetector;
import io.github.janguenter.bluemap.sophisticated.profile.ProfileDisablement;
import io.github.janguenter.bluemap.sophisticated.profile.backpacks.SophisticatedBackpacks32573Profile;
import io.github.janguenter.bluemap.sophisticated.profile.core.SophisticatedCore1480Profile;
import io.github.janguenter.bluemap.sophisticated.profile.storage.SophisticatedStorage1583Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/** Exact artifact activation and renderer routing for both independent family routes. */
final class SophisticatedResourceExtension implements ResourcePackExtension {

    private static final Key SYNTHETIC = Key.parse("bluemap_sophisticated:sophisticated_shape");

    private final ResourcePack resourcePack;
    private final SophisticatedRuntime runtime;

    SophisticatedResourceExtension(ResourcePack resourcePack, SophisticatedRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws IOException {
        ProfileDisablement disabled = ProfileDisablement.current();
        if (disabled.isDisabled(SophisticatedRuntime.CORE)) {
            runtime.route(SophisticatedRuntime.CORE).inactive("operator-disabled");
            runtime.route(SophisticatedRuntime.STORAGE).inactive("blocked-by-core");
            runtime.route(SophisticatedRuntime.BACKPACKS).inactive("blocked-by-core");
            return;
        }
        boolean core = ExactModArtifactDetector.matches(
                roots,
                SophisticatedCore1480Profile.JAR_SHA256,
                SophisticatedCore1480Profile.JAR_SIZE
        );
        if (!core) {
            runtime.route(SophisticatedRuntime.CORE).inactive("exact-core-missing");
            runtime.route("storage").inactive("exact-core-missing");
            runtime.route("backpacks").inactive("exact-core-missing");
            return;
        }
        runtime.route(SophisticatedRuntime.CORE).activate();

        activateRoute(
                roots,
                disabled,
                SophisticatedRuntime.STORAGE,
                SophisticatedStorage1583Profile.JAR_SHA256,
                SophisticatedStorage1583Profile.JAR_SIZE,
                "exact-storage-missing"
        );
        activateRoute(
                roots,
                disabled,
                SophisticatedRuntime.BACKPACKS,
                SophisticatedBackpacks32573Profile.JAR_SHA256,
                SophisticatedBackpacks32573Profile.JAR_SIZE,
                "exact-backpacks-missing"
        );

        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState dispatch =
                resourcePack.getBlockStates().get(SYNTHETIC);
        if (!validDispatch(dispatch)) {
            runtime.route("storage").inactive("synthetic-dispatch-invalid");
            runtime.route("backpacks").inactive("synthetic-dispatch-invalid");
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        Set<Key> keys = new HashSet<>();
        if (runtime.route("storage").isActive()) {
            keys.addAll(SophisticatedStorage1583Profile.REQUIRED_TEXTURES);
        }
        if (runtime.route("backpacks").isActive()) {
            keys.addAll(SophisticatedBackpacks32573Profile.REQUIRED_TEXTURES);
        }
        return Set.copyOf(keys);
    }

    @Override
    public void bake() {
        validateTextures("storage", SophisticatedStorage1583Profile.REQUIRED_TEXTURES);
        validateTextures("backpacks", SophisticatedBackpacks32573Profile.REQUIRED_TEXTURES);
    }

    @Override
    public Key getBlockStateKey(Key key) {
        String id = key.getFormatted();
        if (runtime.route("storage").isActive()
                && SophisticatedStorage1583Profile.ROUTED_BLOCKS.contains(id)) {
            return SYNTHETIC;
        }
        if (runtime.route("backpacks").isActive()
                && SophisticatedBackpacks32573Profile.ROUTED_BLOCKS.contains(id)) {
            return SYNTHETIC;
        }
        return key;
    }

    @Override
    public void getBlockProperties(BlockState blockState, BlockProperties.Builder builder) {
        String id = blockState.getId().getFormatted();
        if ((runtime.route("storage").isActive()
                    && SophisticatedStorage1583Profile.ROUTED_BLOCKS.contains(id))
                || (runtime.route("backpacks").isActive()
                    && SophisticatedBackpacks32573Profile.ROUTED_BLOCKS.contains(id))) {
            builder.culling(false).occluding(false).cullingIdentical(false);
        }
    }

    private void validateTextures(String routeId, Set<Key> required) {
        if (!runtime.route(routeId).isActive()) {
            return;
        }
        for (Key key : required) {
            if (resourcePack.getTextures().get(key) == null) {
                runtime.route(routeId).inactive("required-texture-missing");
                return;
            }
        }
    }

    private void activateRoute(
            Iterable<Path> roots,
            ProfileDisablement disabled,
            String routeId,
            String sha256,
            long size,
            String missingReason
    ) throws IOException {
        io.github.janguenter.bluemap.sophisticated.activation.RouteActivation route =
                runtime.route(routeId);
        if (disabled.isDisabled(routeId)) {
            route.inactive("operator-disabled");
        } else if (ExactModArtifactDetector.matches(roots, sha256, size)) {
            route.activate();
        } else {
            route.inactive(missingReason);
        }
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        if (state == null || state.getMultipart() != null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet set = variants.getDefaultVariant();
        if (set.getVariants().length != 1) {
            return false;
        }
        Variant variant = set.getVariants()[0];
        return BlueMap522Adapter.isExpectedDispatch(variant);
    }
}
