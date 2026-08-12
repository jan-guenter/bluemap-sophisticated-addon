/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile.core;

import java.util.regex.Pattern;

/** Shared exact dependency gate for the two sibling Sophisticated routes. */
public final class SophisticatedCore1480Profile {

    public static final String PROFILE_ID = "core";
    public static final String MOD_ID = "sophisticatedcore";
    public static final String VERSION = "1.4.80";
    public static final String DISTRIBUTION_VERSION = "1.4.80.2194";
    public static final String ARTIFACT = "sophisticatedcore-1.21.1-1.4.80.2194.jar";
    public static final long JAR_SIZE = 1_673_669L;
    public static final long JAR_BYTES = JAR_SIZE;
    public static final String JAR_SHA1 =
            "bef1d5186feaed80b11bd1e6f2dc880e8bec0449";
    public static final String JAR_SHA256 =
            "58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0";
    public static final String JAR_SHA512 =
            "277d93609e53a70e693f3b492e37b537534e06be3f24313f3930c1bca4d6c556"
                    + "c2cc54e6a2f4d03622b9acbbc4ac29766df80941a93dae245890c5ecf52851ea";
    public static final int CURSEFORGE_PROJECT_ID = 618_298;
    public static final int CURSEFORGE_FILE_ID = 8_503_041;
    public static final long CURSEFORGE_FINGERPRINT = 1_496_828_859L;
    public static final String EXACT_REASON = "exact-atm-1.2.0-core-1.4.80.2194";

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private SophisticatedCore1480Profile() {
    }

    public static boolean acceptsArtifact(long size, String sha256) {
        return size == JAR_SIZE
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }
}
