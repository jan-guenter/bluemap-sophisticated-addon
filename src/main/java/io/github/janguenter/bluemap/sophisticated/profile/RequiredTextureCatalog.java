/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile;

import de.bluecolored.bluemap.core.util.Key;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/** Loads the texture-key projection of a packaged exact-resource manifest. */
public final class RequiredTextureCatalog {

    private RequiredTextureCatalog() {
    }

    public static Set<Key> load(
            Class<?> anchor,
            String manifestPath,
            String namespace,
            int expectedCount
    ) {
        String prefix = "assets/" + namespace + "/textures/";
        LinkedHashSet<Key> result = new LinkedHashSet<>();
        try (InputStream input = anchor.getResourceAsStream(manifestPath)) {
            if (input == null) {
                throw new IllegalStateException("missing exact-resource manifest " + manifestPath);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)
            )) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int tab = line.indexOf('\t');
                    String path = tab < 0 ? line : line.substring(0, tab);
                    if (path.startsWith(prefix) && path.endsWith(".png")) {
                        String texture = path.substring(prefix.length(), path.length() - 4);
                        result.add(Key.parse(namespace + ":" + texture));
                    }
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("cannot read exact-resource manifest", exception);
        }
        if (result.size() != expectedCount) {
            throw new IllegalStateException(
                    "exact-resource texture count is " + result.size()
                            + "; expected " + expectedCount
            );
        }
        return Set.copyOf(result);
    }
}
