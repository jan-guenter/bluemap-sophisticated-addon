/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTName;

/**
 * BlueNBT projection of the stable Sophisticated fields used by the map renderer.
 * Unknown inventory, display-item, fluid, energy and activity data deliberately stays opaque.
 */
public final class SophisticatedBlockEntityData extends MCABlockEntity {

    @NBTName("storageWrapper")
    private Object storageWrapper;
    private Object materials;

    @NBTName("woodType")
    private String woodType;
    private Boolean packed;
    private Boolean locked;

    @NBTName("showLock")
    private Boolean showLock;

    @NBTName("showTier")
    private Boolean showTier;

    @NBTName("backpackData")
    private Object backpackData;
    private String material;

    @NBTName("overlayHidden")
    private Boolean overlayHidden;

    public SophisticatedBlockEntityData() {
    }

    Object storageWrapper() {
        return storageWrapper;
    }

    Object materials() {
        return materials;
    }

    String woodType() {
        return woodType;
    }

    boolean packed() {
        return Boolean.TRUE.equals(packed);
    }

    boolean locked() {
        return Boolean.TRUE.equals(locked);
    }

    boolean showLock() {
        return showLock == null || showLock;
    }

    boolean showTier() {
        return showTier == null || showTier;
    }

    Object backpackData() {
        return backpackData;
    }

    String material() {
        return material;
    }

    boolean overlayHidden() {
        return Boolean.TRUE.equals(overlayHidden);
    }
}
