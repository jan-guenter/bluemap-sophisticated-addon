/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDisablementTest {

    @Test
    void mergesNormalizedOperatorSwitchesAndIgnoresMalformedTokens() {
        ProfileDisablement disablement = ProfileDisablement.from(
                " STORAGE,not valid,Backpacks ",
                "core,storage,?"
        );

        assertEquals(Set.of("core", "storage", "backpacks"),
                disablement.disabledProfiles());
        assertTrue(disablement.isDisabled("STORAGE"));
        assertTrue(disablement.isDisabled("backpacks"));
        assertFalse(disablement.isDisabled("unknown"));
    }
}
