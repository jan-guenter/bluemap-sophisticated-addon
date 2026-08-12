/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile.backpacks;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.sophisticated.profile.RequiredTextureCatalog;

import java.util.Set;
import java.util.regex.Pattern;

/** Exact All the Mons 1.2.0 profile for Sophisticated Backpacks 3.25.73. */
public final class SophisticatedBackpacks32573Profile {

    public static final String PROFILE_ID = "backpacks";
    public static final String MOD_ID = "sophisticatedbackpacks";
    public static final String VERSION = "3.25.73";
    public static final String DISTRIBUTION_VERSION = "3.25.73.2027";
    public static final String ARTIFACT =
            "sophisticatedbackpacks-1.21.1-3.25.73.2027.jar";
    public static final long JAR_SIZE = 1_144_235L;
    public static final long JAR_BYTES = JAR_SIZE;
    public static final String JAR_SHA1 =
            "e8baceab12d01ff170e7dcf3ab2079206d1407fd";
    public static final String JAR_SHA256 =
            "ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e";
    public static final String JAR_SHA512 =
            "fe92bd732f19d71818ba339cc3799645181c9dadbe669f99284125b6c96f70ed1f"
                    + "96bc4b91dfcf9220cc01a19c1dbfdcf95650bbd8e8166133c40d2660b90805";
    public static final int CURSEFORGE_PROJECT_ID = 422_301;
    public static final int CURSEFORGE_FILE_ID = 8_569_661;
    public static final long CURSEFORGE_FINGERPRINT = 3_818_397_160L;
    public static final String EXACT_REASON = "exact-atm-1.2.0-backpacks-3.25.73.2027";
    public static final Set<String> ROUTED_BLOCKS =
            SophisticatedBackpacks32573Catalog.ROUTED_BLOCKS;
    public static final Set<String> BLOCK_ENTITY_IDS =
            SophisticatedBackpacks32573Catalog.BLOCK_ENTITY_IDS;
    public static final Set<Key> REQUIRED_TEXTURES = RequiredTextureCatalog.load(
            SophisticatedBackpacks32573Profile.class,
            "/bluemap-sophisticated/profiles/sophisticatedbackpacks/3.25.73/required-resources.tsv",
            MOD_ID,
            11
    );

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private SophisticatedBackpacks32573Profile() {
    }

    public static boolean acceptsArtifact(long size, String sha256) {
        return size == JAR_SIZE
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }
}
