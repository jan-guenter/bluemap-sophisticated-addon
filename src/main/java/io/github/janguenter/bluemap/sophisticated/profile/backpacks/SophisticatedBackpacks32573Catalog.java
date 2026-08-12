/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile.backpacks;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Closed placed-block catalog observed in Sophisticated Backpacks 3.25.73. */
public final class SophisticatedBackpacks32573Catalog {

    public static final List<String> TIERS = List.of(
            "", "copper", "iron", "gold", "diamond", "netherite"
    );
    public static final Set<String> ROUTED_BLOCKS = routedBlocks();
    public static final Set<String> BLOCK_ENTITY_IDS = Set.of(
            "sophisticatedbackpacks:backpack"
    );

    private SophisticatedBackpacks32573Catalog() {
    }

    private static Set<String> routedBlocks() {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String tier : TIERS) {
            String prefix = tier.isEmpty() ? "" : tier + "_";
            result.add("sophisticatedbackpacks:" + prefix + "backpack");
        }
        if (result.size() != 6) {
            throw new IllegalStateException("invalid Sophisticated Backpacks block catalog");
        }
        return Set.copyOf(result);
    }
}
