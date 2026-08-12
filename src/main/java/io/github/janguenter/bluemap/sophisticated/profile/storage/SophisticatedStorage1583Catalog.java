/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile.storage;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Closed registry catalog observed in the exact Sophisticated Storage 1.5.83 artifact. */
public final class SophisticatedStorage1583Catalog {

    public static final List<String> TIERS = List.of(
            "", "copper", "iron", "gold", "diamond", "netherite"
    );
    public static final List<String> WOODS = List.of(
            "acacia", "bamboo", "birch", "cherry", "crimson", "dark_oak",
            "jungle", "mangrove", "oak", "spruce", "warped"
    );
    public static final Set<String> ROUTED_BLOCKS = routedBlocks();
    public static final Set<String> BLOCK_ENTITY_IDS = Set.of(
            id("barrel"),
            id("limited_barrel"),
            id("chest"),
            id("shulker_box"),
            id("controller"),
            id("storage_link"),
            id("storage_io"),
            id("storage_input"),
            id("storage_output"),
            id("storage_connector"),
            id("decoration_table")
    );

    private SophisticatedStorage1583Catalog() {
    }

    private static Set<String> routedBlocks() {
        LinkedHashSet<String> blocks = new LinkedHashSet<>();
        for (String tier : TIERS) {
            String prefix = tier.isEmpty() ? "" : tier + "_";
            blocks.add(id(prefix + "barrel"));
            blocks.add(id(prefix + "chest"));
            blocks.add(id(prefix + "shulker_box"));
            for (int slots = 1; slots <= 4; slots++) {
                blocks.add(id("limited_" + prefix + "barrel_" + slots));
            }
        }
        blocks.add(id("controller"));
        blocks.add(id("storage_link"));
        blocks.add(id("storage_io"));
        blocks.add(id("storage_input"));
        blocks.add(id("storage_output"));
        blocks.add(id("decoration_table"));
        for (String wood : WOODS) {
            blocks.add(id(wood + "_storage_connector"));
        }
        if (blocks.size() != 59) {
            throw new IllegalStateException("invalid Sophisticated Storage block catalog");
        }
        return Set.copyOf(blocks);
    }

    private static String id(String path) {
        return "sophisticatedstorage:" + path;
    }
}
