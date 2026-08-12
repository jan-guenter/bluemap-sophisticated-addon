/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.sophisticated.activation.SophisticatedRuntime;

/** Resource-pack extension factory registered before resource loading begins. */
final class SophisticatedResourceExtensionType
        implements ResourcePack.Extension<SophisticatedResourceExtension> {

    static final Key KEY = Key.parse("bluemap_sophisticated:exact_profiles");

    private final SophisticatedRuntime runtime;

    SophisticatedResourceExtensionType(SophisticatedRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public SophisticatedResourceExtension create(ResourcePack pack) {
        return new SophisticatedResourceExtension(pack, runtime);
    }
}
