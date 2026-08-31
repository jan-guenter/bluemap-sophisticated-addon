/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdapterBoundaryTest {

    @Test
    void usesSharedAdapterHelpersWithoutLocalCopies() {
        assertInstanceOf(ResourceExtensionType.class, BlueMap523Adapter.extensionType());
        assertEquals(
                Key.parse("bluemap_sophisticated:exact_profiles"),
                BlueMap523Adapter.extensionType().getKey()
        );
        assertInstanceOf(
                SophisticatedResourceExtension.class,
                BlueMap523Adapter.extensionType().create(null)
        );
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.sophisticated.adapter.bluemap523."
                        + "AdapterCompatibility"
        ));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.sophisticated.adapter.bluemap523."
                        + "SophisticatedResourceExtensionType"
        ));
    }
}
