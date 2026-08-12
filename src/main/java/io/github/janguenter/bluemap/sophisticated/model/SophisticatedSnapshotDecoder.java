/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

/** Fail-closed decoder for the bounded persisted visual fields. */
public final class SophisticatedSnapshotDecoder {

    private static final Set<String> MATERIAL_KEYS = Set.of(
            "side", "side_trim", "bottom", "bottom_trim", "top", "top_trim",
            "top_inner_trim", "all", "all_trim", "all_but_trim", "top_all",
            "side_all", "bottom_all"
    );
    private static final Set<String> MATERIAL_LEAVES = Set.of(
            "side", "side_trim", "bottom", "bottom_trim", "top", "top_trim",
            "top_inner_trim"
    );
    private static final Map<String, List<String>> MATERIAL_EXPANSIONS = Map.of(
            "all", List.of("side", "side_trim", "bottom", "bottom_trim", "top", "top_trim", "top_inner_trim"),
            "all_trim", List.of("side_trim", "bottom_trim", "top_trim", "top_inner_trim"),
            "all_but_trim", List.of("side", "bottom", "top"),
            "top_all", List.of("top", "top_trim", "top_inner_trim"),
            "side_all", List.of("side", "side_trim"),
            "bottom_all", List.of("bottom", "bottom_trim")
    );

    public SophisticatedSnapshot storage(
            Object rawWrapper,
            Object rawMaterials,
            String woodType,
            boolean packed,
            boolean locked,
            boolean showLock,
            boolean showTier
    ) {
        Map<?, ?> wrapper = map(rawWrapper);
        String normalizedWood = normalizedWood(woodType);
        boolean valid = rawWrapper instanceof Map<?, ?>
                && validOptionalColor(wrapper, "mainColor")
                && validOptionalColor(wrapper, "accentColor")
                && normalizedWood != null
                && validMaterials(rawMaterials);
        return new SophisticatedSnapshot(
                storageColor(wrapper.get("mainColor")),
                storageColor(wrapper.get("accentColor")),
                normalizedWood == null ? "acacia" : normalizedWood,
                packed,
                locked,
                showLock,
                showTier,
                "",
                materials(rawMaterials),
                valid
        );
    }

    public SophisticatedSnapshot backpack(Object rawBackpackData) {
        Map<?, ?> stack = map(rawBackpackData);
        Object rawComponents = stack.get("components");
        Map<?, ?> components = map(rawComponents);
        String itemId = string(stack.get("id"));
        boolean valid = namespaced(itemId)
                && (!stack.containsKey("components") || rawComponents instanceof Map<?, ?>)
                && validOptionalColor(components, "sophisticatedcore:main_color")
                && validOptionalColor(components, "sophisticatedcore:accent_color");
        return new SophisticatedSnapshot(
                color(components.get("sophisticatedcore:main_color"), -3_382_982),
                color(components.get("sophisticatedcore:accent_color"), -10_342_886),
                "",
                false,
                false,
                false,
                true,
                itemId,
                Map.of(),
                valid
        );
    }

    /** Returns the supported backpack item id or an empty string for malformed input. */
    public String backpackItemId(Object rawBackpackData) {
        String itemId = string(map(rawBackpackData).get("id"));
        return namespaced(itemId) ? itemId : "";
    }

    private static Map<String, String> materials(Object raw) {
        Map<String, String> source = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map(raw).entrySet()) {
            String key = string(entry.getKey());
            String value = string(entry.getValue());
            if (MATERIAL_KEYS.contains(key) && namespaced(value)) {
                source.put(key, value);
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        MATERIAL_EXPANSIONS.forEach((aggregate, leaves) -> {
            String value = source.get(aggregate);
            if (value != null) {
                leaves.forEach(leaf -> result.put(leaf, value));
            }
        });
        MATERIAL_LEAVES.forEach(leaf -> {
            String value = source.get(leaf);
            if (value != null) {
                result.put(leaf, value);
            }
        });
        return Map.copyOf(result);
    }

    private static boolean validMaterials(Object raw) {
        if (raw == null) {
            return true;
        }
        if (!(raw instanceof Map<?, ?> rawMap)) {
            return false;
        }
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            String key = string(entry.getKey());
            String value = string(entry.getValue());
            if (!MATERIAL_KEYS.contains(key) || !namespaced(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validOptionalColor(Map<?, ?> source, String key) {
        return !source.containsKey(key) || source.get(key) instanceof Number;
    }

    private static OptionalInt color(Object value) {
        return color(value, Integer.MIN_VALUE);
    }

    private static OptionalInt storageColor(Object value) {
        if (value instanceof Number number && number.intValue() == -1) {
            return OptionalInt.empty();
        }
        return color(value);
    }

    private static OptionalInt color(Object value, int fallback) {
        if (value instanceof Number number) {
            return OptionalInt.of(opaque(number.intValue()));
        }
        return fallback == Integer.MIN_VALUE
                ? OptionalInt.empty()
                : OptionalInt.of(opaque(fallback));
    }

    private static int opaque(int color) {
        return color | 0xFF00_0000;
    }

    private static String normalizedWood(String value) {
        if (value == null || value.isBlank()) {
            return "acacia";
        }
        String normalized = value.trim().toLowerCase();
        if (!normalized.matches("[a-z0-9_.-]+")) {
            return null;
        }
        return normalized;
    }

    private static boolean namespaced(String value) {
        return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+");
    }

    private static String string(Object value) {
        return value instanceof String text ? text : "";
    }

    private static Map<?, ?> map(Object value) {
        return value instanceof Map<?, ?> map ? map : Map.of();
    }
}
