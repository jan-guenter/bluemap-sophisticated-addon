/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile.storage;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.sophisticated.profile.RequiredTextureCatalog;

import java.util.Set;
import java.util.regex.Pattern;

/** Exact All the Mons 1.2.0 profile for Sophisticated Storage 1.5.83. */
public final class SophisticatedStorage1583Profile {

    public static final String PROFILE_ID = "storage";
    public static final String MOD_ID = "sophisticatedstorage";
    public static final String VERSION = "1.5.83";
    public static final String DISTRIBUTION_VERSION = "1.5.83.2017";
    public static final String ARTIFACT =
            "sophisticatedstorage-1.21.1-1.5.83.2017.jar";
    public static final long JAR_SIZE = 1_828_640L;
    public static final long JAR_BYTES = JAR_SIZE;
    public static final String JAR_SHA1 =
            "b36fa724fe925e715d8b13929d5789125e97e81b";
    public static final String JAR_SHA256 =
            "354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600";
    public static final String JAR_SHA512 =
            "af16494408c31e87a94e1d517c684b4b0c0fdb7ceaf6332ac1c281e18ee0cd9"
                    + "e0e87605f9a991b21ad737dff8eb83d5e0ac811983d881da5e56f989d344d46da";
    public static final int CURSEFORGE_PROJECT_ID = 619_320;
    public static final int CURSEFORGE_FILE_ID = 8_503_122;
    public static final long CURSEFORGE_FINGERPRINT = 1_733_245_886L;
    public static final String EXACT_REASON = "exact-atm-1.2.0-storage-1.5.83.2017";
    public static final Set<String> ROUTED_BLOCKS =
            SophisticatedStorage1583Catalog.ROUTED_BLOCKS;
    public static final Set<String> BLOCK_ENTITY_IDS =
            SophisticatedStorage1583Catalog.BLOCK_ENTITY_IDS;
    public static final Set<Key> REQUIRED_TEXTURES = RequiredTextureCatalog.load(
            SophisticatedStorage1583Profile.class,
            "/bluemap-sophisticated/profiles/sophisticatedstorage/1.5.83/required-resources.tsv",
            MOD_ID,
            219
    );

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private SophisticatedStorage1583Profile() {
    }

    public static boolean acceptsArtifact(long size, String sha256) {
        return size == JAR_SIZE
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }
}
