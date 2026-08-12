/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.model;

import java.util.Map;
import java.util.OptionalInt;

/** Stable visual state only; contents, counts, fill, energy and activity are excluded. */
public record SophisticatedSnapshot(
        OptionalInt mainColor,
        OptionalInt accentColor,
        String woodType,
        boolean packed,
        boolean locked,
        boolean showLock,
        boolean showTier,
        String itemId,
        Map<String, String> barrelMaterials,
        boolean valid
) {

    public SophisticatedSnapshot {
        woodType = woodType == null || woodType.isBlank() ? "acacia" : woodType;
        itemId = itemId == null ? "" : itemId;
        barrelMaterials = Map.copyOf(barrelMaterials);
    }
}
