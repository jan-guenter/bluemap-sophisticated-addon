/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.profile;

import io.github.janguenter.bluemap.sophisticated.profile.backpacks.SophisticatedBackpacks32573Profile;
import io.github.janguenter.bluemap.sophisticated.profile.storage.SophisticatedStorage1583Profile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SophisticatedCatalogTest {

    @Test
    void exactProfilesExposeTheClosedWorldBlockCatalogs() {
        assertEquals(59, SophisticatedStorage1583Profile.ROUTED_BLOCKS.size());
        assertEquals(6, SophisticatedBackpacks32573Profile.ROUTED_BLOCKS.size());
        assertTrue(SophisticatedStorage1583Profile.ROUTED_BLOCKS.contains(
                "sophisticatedstorage:limited_netherite_barrel_4"
        ));
        assertTrue(SophisticatedStorage1583Profile.ROUTED_BLOCKS.contains(
                "sophisticatedstorage:warped_storage_connector"
        ));
        assertTrue(SophisticatedBackpacks32573Profile.ROUTED_BLOCKS.contains(
                "sophisticatedbackpacks:netherite_backpack"
        ));
    }
}
