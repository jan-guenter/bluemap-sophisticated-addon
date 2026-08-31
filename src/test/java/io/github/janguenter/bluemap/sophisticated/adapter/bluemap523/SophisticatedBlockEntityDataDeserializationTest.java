/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.sophisticated.model.SophisticatedSnapshot;
import io.github.janguenter.bluemap.sophisticated.model.SophisticatedSnapshotDecoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SophisticatedBlockEntityDataDeserializationTest {

    private final SophisticatedSnapshotDecoder decoder = new SophisticatedSnapshotDecoder();

    @Test
    void exactCamelCaseStorageFieldsSurviveBlueMapNamingStrategy() throws Exception {
        SophisticatedBlockEntityData data = read(blockEntity(
                "sophisticatedstorage:barrel",
                writer -> {
                    writer.name("storageWrapper").beginCompound();
                    writer.name("mainColor").value(-393_218);
                    writer.name("accentColor").value(-14_869_215);
                    writer.name("renderInfo").beginCompound();
                    writer.endCompound();
                    writer.endCompound();
                    writer.name("materials").beginCompound();
                    writer.endCompound();
                    writer.name("woodType").value("oak");
                    writer.name("packed").value((byte) 0);
                    writer.name("locked").value((byte) 1);
                    writer.name("showLock").value((byte) 1);
                    writer.name("showTier").value((byte) 0);
                }
        ));

        SophisticatedSnapshot snapshot = decoder.storage(
                data.storageWrapper(), data.materials(), data.woodType(), data.packed(),
                data.locked(), data.showLock(), data.showTier()
        );

        assertTrue(snapshot.valid());
        assertEquals("oak", snapshot.woodType());
        assertEquals(0xFFF9FFFE, snapshot.mainColor().orElseThrow());
        assertEquals(0xFF1D1D21, snapshot.accentColor().orElseThrow());
        assertTrue(snapshot.locked());
        assertTrue(snapshot.showLock());
        assertFalse(snapshot.showTier());
    }

    @Test
    void exactCamelCaseBackpackAndOverlayFieldsSurviveBlueMapNamingStrategy()
            throws Exception {
        SophisticatedBlockEntityData data = read(blockEntity(
                "sophisticatedbackpacks:backpack",
                writer -> {
                    writer.name("backpackData").beginCompound();
                    writer.name("id").value("sophisticatedbackpacks:diamond_backpack");
                    writer.name("count").value(1);
                    writer.name("components").beginCompound();
                    writer.name("sophisticatedcore:main_color").value(-3_382_982);
                    writer.name("sophisticatedcore:accent_color").value(-10_342_886);
                    writer.endCompound();
                    writer.endCompound();
                    writer.name("material").value("minecraft:oak_planks");
                    writer.name("overlayHidden").value((byte) 1);
                }
        ));

        SophisticatedSnapshot snapshot = decoder.backpack(data.backpackData());

        assertTrue(snapshot.valid());
        assertEquals("sophisticatedbackpacks:diamond_backpack", snapshot.itemId());
        assertEquals("minecraft:oak_planks", data.material());
        assertTrue(data.overlayHidden());
    }

    private static SophisticatedBlockEntityData read(byte[] nbt) throws IOException {
        return MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(nbt),
                SophisticatedBlockEntityData.class
        );
    }

    private static byte[] blockEntity(String id, WriterAction body) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(id);
            writer.name("x").value(220);
            writer.name("y").value(100);
            writer.name("z").value(196);
            body.write(writer);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(NBTWriter writer) throws IOException;
    }
}
