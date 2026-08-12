/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.sophisticated.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.sophisticated.activation.SophisticatedRuntime;
import io.github.janguenter.bluemap.sophisticated.model.SophisticatedSnapshot;
import io.github.janguenter.bluemap.sophisticated.model.SophisticatedSnapshotDecoder;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/** Static stable-optics renderer for the exact Storage and Backpacks routes. */
final class SophisticatedRenderer implements BlockRenderer {

    private static final int WHITE = 0xFFFF_FFFF;
    private static final Set<String> WOODS = Set.of(
            "acacia", "bamboo", "birch", "cherry", "crimson", "dark_oak",
            "jungle", "mangrove", "oak", "spruce", "warped"
    );

    private final ResourcePack resourcePack;
    private final SophisticatedRuntime runtime;
    private final ResourceModelRenderer stock;
    private final JsonModelEmitter json;
    private final PrimitiveEmitter primitives;
    private final BlockMaterialResolver blockMaterials;
    private final SophisticatedSnapshotDecoder decoder = new SophisticatedSnapshotDecoder();

    SophisticatedRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            SophisticatedRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
        this.stock = new ResourceModelRenderer(resourcePack, textureGallery, renderSettings);
        this.json = new JsonModelEmitter(resourcePack, textureGallery);
        this.primitives = new PrimitiveEmitter(resourcePack, textureGallery);
        this.blockMaterials = new BlockMaterialResolver(resourcePack);
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant original,
            TileModelView target,
            Color mapColor
    ) {
        int start = target.getStart();
        Color initialMapColor = new Color().set(mapColor);
        String blockId = block.getBlockState().getId().getFormatted();
        String route = blockId.startsWith("sophisticatedbackpacks:")
                ? "backpacks"
                : "storage";
        if (!runtime.route(route).isActive()) {
            renderStock(block, target, mapColor);
            return;
        }

        try {
            SophisticatedBlockEntityData data = block.getBlockEntity()
                    instanceof SophisticatedBlockEntityData found ? found : null;
            boolean success = route.equals("backpacks")
                    ? renderBackpack(blockId, block, data, target, mapColor)
                    : renderStorage(blockId, block, data, target, mapColor);
            if (!success) {
                resetAndRenderStock(block, target, start, mapColor, initialMapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            runtime.route(route).fail("render-failed");
            resetAndRenderStock(block, target, start, mapColor, initialMapColor);
        }
    }

    private void resetAndRenderStock(
            BlockNeighborhood block,
            TileModelView target,
            int start,
            Color mapColor,
            Color initialMapColor
    ) {
        target.getTileModel().reset(start);
        target.initialize(start);
        mapColor.set(initialMapColor);
        renderStock(block, target, mapColor);
    }

    private void renderStock(
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(block.getBlockState().getId());
        if (state == null) {
            return;
        }
        state.forEach(
                block.getBlockState(),
                block.getX(),
                block.getY(),
                block.getZ(),
                variant -> stock.render(block, variant, target, mapColor)
        );
    }

    private boolean renderStorage(
            String blockId,
            BlockNeighborhood block,
            SophisticatedBlockEntityData data,
            TileModelView target,
            Color mapColor
    ) {
        if (barrel(blockId)) {
            if (data == null) {
                return false;
            }
            SophisticatedSnapshot snapshot = decoder.storage(
                    data.storageWrapper(), data.materials(), data.woodType(), data.packed(),
                    data.locked(), data.showLock(), data.showTier()
            );
            if (!snapshot.valid()) {
                return false;
            }
            return renderBarrel(blockId, block, target, snapshot, mapColor);
        }
        if (chest(blockId)) {
            SophisticatedBlockEntityData visualData = chestVisualData(blockId, block, data);
            if (visualData == null) {
                return false;
            }
            SophisticatedSnapshot snapshot = decoder.storage(
                    visualData.storageWrapper(), visualData.materials(), visualData.woodType(),
                    visualData.packed(), visualData.locked(), visualData.showLock(),
                    visualData.showTier()
            );
            if (!snapshot.valid()) {
                return false;
            }
            return renderChest(blockId, block, target, snapshot, mapColor);
        }
        if (shulker(blockId)) {
            if (data == null) {
                return false;
            }
            SophisticatedSnapshot snapshot = decoder.storage(
                    data.storageWrapper(), data.materials(), data.woodType(), data.packed(),
                    data.locked(), data.showLock(), data.showTier()
            );
            if (!snapshot.valid()) {
                return false;
            }
            return renderShulker(blockId, block, target, snapshot, mapColor);
        }
        if (blockId.equals("sophisticatedstorage:decoration_table")) {
            return json.emit(
                    Key.parse("sophisticatedstorage:block/decoration_table"),
                    block, target, 0F, horizontalRotation(block), 0F, Map.of(),
                    ignored -> WHITE, mapColor
            );
        }
        return data != null && renderSimpleMaterial(blockId, block, data, target, mapColor);
    }

    private boolean renderBarrel(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            SophisticatedSnapshot snapshot,
            Color mapColor
    ) {
        String blockPath = path(blockId);
        boolean flat = Boolean.parseBoolean(
                block.getBlockState().getProperties().getOrDefault("flat_top", "false")
        );
        int limited = limitedSlots(blockPath);
        String tier = tier(blockPath);
        if (!WOODS.contains(snapshot.woodType())) {
            return false;
        }
        String wood = snapshot.woodType();

        Map<String, Key> overrides = new HashMap<>();
        String topName = limited > 0
                ? "limited_" + wood + "_barrel_" + limited + "_top"
                : wood + "_barrel_top";
        overrides.put("top", key("sophisticatedstorage:block/" + topName));
        overrides.put("side", key("sophisticatedstorage:block/" + wood + "_barrel_side"));
        overrides.put("bottom", key("sophisticatedstorage:block/" + wood + "_barrel_bottom"));
        aliasBarrelTextures(overrides);

        if (!applyBarrelCamos(snapshot, overrides)) {
            return false;
        }
        int main = color(snapshot.mainColor(), WHITE);
        int accent = color(snapshot.accentColor(), WHITE);
        float xRotation = barrelXRotation(block, limited > 0);
        float yRotation = horizontalRotation(block);
        boolean emitted;
        if (snapshot.barrelMaterials().isEmpty()) {
            Key baseModel = key(flat
                    ? "minecraft:block/cube_bottom_top"
                    : "sophisticatedstorage:block/barrel_part/base");
            emitted = json.emit(baseModel, block, target, xRotation, yRotation, 0F,
                    overrides, ignored -> WHITE, mapColor);
        } else {
            String prefix = flat ? "flat_" : "";
            emitted = json.emit(
                    key("sophisticatedstorage:block/barrel_part/" + prefix + "core"),
                    block, target, xRotation, yRotation, 0F, overrides,
                    ignored -> WHITE, mapColor
            );
            emitted = json.emit(
                    key("sophisticatedstorage:block/barrel_part/" + prefix + "trim"),
                    block, target, xRotation, yRotation, 0F, overrides,
                    ignored -> WHITE, mapColor
            ) && emitted;
        }
        if (!flat && limited > 1) {
            emitted = emitLimitedDividerParts(
                    limited,
                    "",
                    block,
                    target,
                    xRotation,
                    yRotation,
                    overrides,
                    WHITE,
                    mapColor
            ) && emitted;
        }

        if (snapshot.mainColor().isPresent() || snapshot.accentColor().isPresent()) {
            String mainModel = flat
                    ? limited > 0 ? "flat_limited_tintable_main" : "flat_tintable_main"
                    : limited > 0 ? "limited_tintable_main" : "tintable_main";
            String accentModel = flat ? "flat_tintable_accent" : "tintable_accent";
            Map<String, Key> mainOverrides = new HashMap<>();
            Map<String, Key> accentOverrides = new HashMap<>();
            if (limited > 0) {
                mainOverrides.put("top", key("sophisticatedstorage:block/limited_barrel_"
                        + limited + "_top_tintable_main"));
                accentOverrides.put("top", key("sophisticatedstorage:block/limited_barrel_"
                        + limited + "_top_tintable_accent"));
                accentOverrides.put("top_trim", accentOverrides.get("top"));
                accentOverrides.put("top_inner_trim", accentOverrides.get("top"));
            }
            if (snapshot.mainColor().isPresent()) {
                emitted = json.emit(
                        key("sophisticatedstorage:block/barrel_part/" + mainModel),
                        block, target, xRotation, yRotation, 0F, mainOverrides,
                        tint -> tint == 1000 ? main : WHITE, mapColor
                ) && emitted;
            }
            if (snapshot.accentColor().isPresent()) {
                emitted = json.emit(
                        key("sophisticatedstorage:block/barrel_part/" + accentModel),
                        block, target, xRotation, yRotation, 0F, accentOverrides,
                        tint -> tint == 1001 ? accent : WHITE, mapColor
                ) && emitted;
            }
            if (!flat && limited > 1 && snapshot.accentColor().isPresent()) {
                emitted = emitLimitedDividerParts(
                        limited,
                        "_tintable_accent",
                        block,
                        target,
                        xRotation,
                        yRotation,
                        accentOverrides,
                        accent,
                        mapColor
                ) && emitted;
            }
        }

        if (snapshot.showTier() && !tier.equals("wood")) {
            Map<String, Key> tierOverrides = barrelTextures(
                    "sophisticatedstorage:block/" + tier + "_barrel_top",
                    "sophisticatedstorage:block/" + tier + "_barrel_side",
                    "sophisticatedstorage:block/" + tier + "_barrel_bottom"
            );
            emitted = json.emit(
                    key(flat ? "minecraft:block/cube_bottom_top"
                            : "sophisticatedstorage:block/barrel_part/base"),
                    block, target, xRotation, yRotation, 0F, tierOverrides,
                    ignored -> WHITE, mapColor
            ) && emitted;
        }
        if (snapshot.packed()) {
            emitted = json.emit(
                    key("sophisticatedstorage:block/barrel_part/packed"), block, target,
                    xRotation, yRotation, 0F,
                    Map.of(), ignored -> WHITE, mapColor
            ) && emitted;
            if (!flat && limited > 1) {
                emitted = emitLimitedDividerParts(
                        limited,
                        "",
                        block,
                        target,
                        xRotation,
                        yRotation,
                        Map.of(
                                "top_inner_trim",
                                key("sophisticatedstorage:block/barrel_top_packed")
                        ),
                        WHITE,
                        mapColor
                ) && emitted;
            }
        } else if (snapshot.locked() && snapshot.showLock()) {
            emitted = json.emit(
                    Key.parse("sophisticatedstorage:block/barrel_part/locked"), block, target,
                    xRotation, yRotation, 0F,
                    Map.of(), ignored -> WHITE, mapColor
            ) && emitted;
        }
        return emitted;
    }

    private boolean applyBarrelCamos(
            SophisticatedSnapshot snapshot,
            Map<String, Key> overrides
    ) {
        for (Map.Entry<String, String> entry : snapshot.barrelMaterials().entrySet()) {
            String slot = entry.getKey();
            Optional<Key> resolved;
            if (slot.startsWith("side")) {
                resolved = resolveMaterial(entry.getValue(), Direction.NORTH);
            } else if (slot.startsWith("top")) {
                resolved = resolveMaterial(entry.getValue(), Direction.UP);
            } else if (slot.startsWith("bottom")) {
                resolved = resolveMaterial(entry.getValue(), Direction.DOWN);
            } else {
                return false;
            }
            if (resolved.isEmpty()) {
                return false;
            }
            overrides.put(slot, resolved.orElseThrow());
        }
        return true;
    }

    private boolean emitLimitedDividerParts(
            int slots,
            String modelSuffix,
            BlockNeighborhood block,
            TileModelView target,
            float xRotation,
            float yRotation,
            Map<String, Key> overrides,
            int accent,
            Color mapColor
    ) {
        boolean emitted = json.emit(
                key("sophisticatedstorage:block/barrel_part/middle_inner_trim" + modelSuffix),
                block,
                target,
                xRotation,
                yRotation,
                0F,
                overrides,
                tint -> tint == 1001 ? accent : WHITE,
                mapColor
        );
        if (slots >= 3) {
            emitted = json.emit(
                    key("sophisticatedstorage:block/barrel_part/down_inner_trim" + modelSuffix),
                    block,
                    target,
                    xRotation,
                    yRotation,
                    0F,
                    overrides,
                    tint -> tint == 1001 ? accent : WHITE,
                    mapColor
            ) && emitted;
        }
        if (slots >= 4) {
            emitted = json.emit(
                    key("sophisticatedstorage:block/barrel_part/up_inner_trim" + modelSuffix),
                    block,
                    target,
                    xRotation,
                    yRotation,
                    0F,
                    overrides,
                    tint -> tint == 1001 ? accent : WHITE,
                    mapColor
            ) && emitted;
        }
        return emitted;
    }

    private Optional<Key> resolveMaterial(String id, Direction face) {
        return blockMaterials.resolve(id, face);
    }

    private boolean renderSimpleMaterial(
            String blockId,
            BlockNeighborhood block,
            SophisticatedBlockEntityData data,
            TileModelView target,
            Color mapColor
    ) {
        String blockPath = path(blockId);
        boolean link = blockPath.equals("storage_link");
        boolean connector = blockPath.endsWith("_storage_connector");
        boolean fullCube = connector || Set.of(
                "controller", "storage_io", "storage_input", "storage_output"
        ).contains(blockPath);
        if (!link && !fullCube) {
            return false;
        }

        Map<Direction, Key> base = new EnumMap<>(Direction.class);
        String material = data.material();
        if (material != null && !material.isBlank()) {
            if (!material.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
                return false;
            }
            for (Direction direction : Direction.values()) {
                Optional<Key> resolved = resolveMaterial(material, direction);
                if (resolved.isEmpty()) {
                    return false;
                }
                base.put(direction, resolved.orElseThrow());
            }
        } else if (link) {
            base.put(Direction.UP, key("sophisticatedstorage:block/storage_link_top"));
            base.put(Direction.DOWN, key("sophisticatedstorage:block/storage_link_bottom"));
            fillSides(base, key("sophisticatedstorage:block/storage_link_side"));
        } else if (connector) {
            base.putAll(allFaces(key("sophisticatedstorage:block/" + blockPath)));
        } else {
            Key top = key("sophisticatedstorage:block/" + blockPath + "_top");
            base.put(Direction.UP, top);
            base.put(Direction.DOWN, top);
            fillSides(base, key("sophisticatedstorage:block/" + blockPath + "_side"));
        }

        float min = link ? 1F / 16F : 0F;
        float max = link ? 15F / 16F : 1F;
        float maxY = link ? 2F / 16F : 1F;
        float xRotation = link ? stateRotation(block, "facing", 0) : 0F;
        float yRotation = link ? stateRotation(block, "facing", 1) : 0F;
        boolean emitted = primitives.cuboid(
                block, target, min, 0F, min, max, maxY, max,
                Map.copyOf(base), WHITE, xRotation, yRotation, 0F, mapColor
        );
        if (data.overlayHidden()) {
            return emitted;
        }

        Map<Direction, Key> overlay = new EnumMap<>(Direction.class);
        if (link) {
            overlay.put(Direction.UP,
                    key("sophisticatedstorage:block/storage_link_overlay_top"));
            overlay.put(Direction.DOWN,
                    key("sophisticatedstorage:block/storage_link_overlay_bottom"));
            fillSides(overlay,
                    key("sophisticatedstorage:block/storage_link_overlay_side"));
        } else if (connector) {
            overlay.putAll(allFaces(
                    key("sophisticatedstorage:block/storage_connector_overlay")
            ));
        } else {
            Key top = key("sophisticatedstorage:block/" + blockPath + "_overlay_top");
            overlay.put(Direction.UP, top);
            overlay.put(Direction.DOWN, top);
            fillSides(overlay,
                    key("sophisticatedstorage:block/" + blockPath + "_overlay_side"));
        }
        return primitives.cuboid(
                block, target, min, 0F, min, max, maxY, max,
                Map.copyOf(overlay), WHITE, xRotation, yRotation, 0F, mapColor
        ) && emitted;
    }

    private boolean renderChest(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            SophisticatedSnapshot snapshot,
            Color mapColor
    ) {
        String tier = tier(path(blockId));
        if (!WOODS.contains(snapshot.woodType())) {
            return false;
        }
        String wood = snapshot.woodType();
        String chestType = block.getBlockState().getProperties().getOrDefault("type", "single");
        if (!Set.of("single", "left", "right").contains(chestType)) {
            return false;
        }
        String texturePrefix = chestType.equals("single") ? "" : chestType + "_";
        Key model = key(switch (chestType) {
            case "left" -> "minecraft:entity/chest/chest_double_left";
            case "right" -> "minecraft:entity/chest/chest_double_right";
            default -> "minecraft:entity/chest/chest";
        });
        Key base = key("sophisticatedstorage:entity/chest/" + texturePrefix
                + (tier.equals("wood") ? wood : "wood_tier"));
        Key tierTexture = key("sophisticatedstorage:entity/chest/" + texturePrefix + tier + "_tier");
        Key mainTexture = key("sophisticatedstorage:entity/chest/" + texturePrefix + "tintable_main");
        Key accentTexture = key("sophisticatedstorage:entity/chest/" + texturePrefix + "tintable_accent");
        float yRotation = chestRotation(block);
        boolean emitted = chestLayer(model, block, target, base, WHITE, yRotation, mapColor);
        if (snapshot.mainColor().isPresent()) {
            emitted = chestLayer(model, block, target, mainTexture,
                    snapshot.mainColor().getAsInt(), yRotation, mapColor) && emitted;
        }
        if (snapshot.accentColor().isPresent()) {
            emitted = chestLayer(model, block, target, accentTexture,
                    snapshot.accentColor().getAsInt(), yRotation, mapColor) && emitted;
        }
        if (snapshot.showTier() && !tier.equals("wood")) {
            emitted = chestLayer(model, block, target, tierTexture, WHITE, yRotation, mapColor) && emitted;
        }
        if (snapshot.packed()) {
            emitted = chestLayer(model, block, target,
                    key("sophisticatedstorage:entity/chest/" + texturePrefix + "packed"),
                    WHITE, yRotation, mapColor) && emitted;
        }
        if (!snapshot.packed() && snapshot.locked() && snapshot.showLock()
                && !chestType.equals("left")) {
            float centerX = chestType.equals("right") ? 0F : 0.5F;
            emitted = primitives.plate(
                    block,
                    target,
                    Direction.NORTH,
                    centerX - 0.5F / 16F,
                    13F / 16F,
                    centerX + 0.5F / 16F,
                    14F / 16F,
                    -0.001F,
                    key("sophisticatedstorage:block/lock"),
                    WHITE,
                    0F,
                    horizontalRotation(block),
                    0F,
                    mapColor
            ) && emitted;
        }
        return emitted;
    }

    private boolean chestLayer(
            Key model,
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            int color,
            float yRotation,
            Color mapColor
    ) {
        return json.emit(model, block, target, 0F, yRotation, 0F,
                Map.of("chest", texture), ignored -> color, mapColor);
    }

    private boolean renderShulker(
            String blockId,
            BlockNeighborhood block,
            TileModelView target,
            SophisticatedSnapshot snapshot,
            Color mapColor
    ) {
        String tier = tier(path(blockId));
        Key base = key("sophisticatedstorage:entity/shulker_box/no_tint");
        Key main = key("sophisticatedstorage:entity/shulker_box/tintable_main");
        Key accent = key("sophisticatedstorage:entity/shulker_box/tintable_accent");
        Key tierTexture = key("sophisticatedstorage:entity/shulker_box/"
                + (tier.equals("wood") ? "base" : tier) + "_tier");
        float x = stateRotation(block, "facing", 0);
        float y = stateRotation(block, "facing", 1);
        boolean emitted = shulkerLayer(block, target, base, WHITE, x, y, mapColor);
        if (snapshot.mainColor().isPresent()) {
            emitted = shulkerLayer(block, target, main,
                    snapshot.mainColor().getAsInt(), x, y, mapColor) && emitted;
        }
        if (snapshot.accentColor().isPresent()) {
            emitted = shulkerLayer(block, target, accent,
                    snapshot.accentColor().getAsInt(), x, y, mapColor) && emitted;
        }
        if (snapshot.showTier() && !tier.equals("wood")) {
            emitted = shulkerLayer(block, target, tierTexture, WHITE, x, y, mapColor) && emitted;
        }
        if (snapshot.locked() && snapshot.showLock()) {
            emitted = primitives.plate(
                    block,
                    target,
                    Direction.NORTH,
                    0.5F - 0.5F / 16F,
                    15F / 16F,
                    0.5F + 0.5F / 16F,
                    1F,
                    -0.001F,
                    key("sophisticatedstorage:block/lock"),
                    WHITE,
                    x,
                    y,
                    0F,
                    mapColor
            ) && emitted;
        }
        return emitted;
    }

    private boolean shulkerLayer(
            BlockNeighborhood block,
            TileModelView target,
            Key texture,
            int color,
            float xRotation,
            float yRotation,
            Color mapColor
    ) {
        return json.emit(key("minecraft:entity/shulker_box"), block, target,
                xRotation, yRotation, 0F, Map.of("0", texture), ignored -> color, mapColor);
    }

    private boolean renderBackpack(
            String blockId,
            BlockNeighborhood block,
            SophisticatedBlockEntityData data,
            TileModelView target,
            Color mapColor
    ) {
        if (data == null) {
            return false;
        }
        SophisticatedSnapshot snapshot = decoder.backpack(data.backpackData());
        String tier = tier(path(blockId));
        if (!snapshot.valid() || !snapshot.itemId().equals(blockId)) {
            return false;
        }
        Map<String, Key> overrides = Map.of(
                "clips", key("sophisticatedbackpacks:block/"
                        + (tier.equals("wood") ? "leather" : tier) + "_clips")
        );
        float yRotation = horizontalRotation(block);
        boolean emitted = json.emit(
                key("sophisticatedbackpacks:block/backpack_base"), block, target,
                0F, yRotation, 0F, overrides,
                tint -> tint == 0
                        ? snapshot.mainColor().orElse(WHITE)
                        : tint == 1 ? snapshot.accentColor().orElse(WHITE) : WHITE,
                mapColor
        );
        emitted = backpackPart("backpack_straps", block, target, overrides,
                snapshot, yRotation, mapColor) && emitted;

        Map<String, String> properties = block.getBlockState().getProperties();
        if (Boolean.parseBoolean(properties.get("left_tank"))) {
            emitted = backpackPart("backpack_left_tank", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        } else {
            emitted = backpackPart("backpack_left_pouch", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        }
        if (Boolean.parseBoolean(properties.get("right_tank"))) {
            emitted = backpackPart("backpack_right_tank", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        } else {
            emitted = backpackPart("backpack_right_pouch", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        }
        if (Boolean.parseBoolean(properties.get("battery"))) {
            emitted = backpackPart("backpack_battery", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        } else {
            emitted = backpackPart("backpack_front_pouch", block, target, overrides,
                    snapshot, yRotation, mapColor) && emitted;
        }
        return emitted;
    }

    private boolean backpackPart(
            String part,
            BlockNeighborhood block,
            TileModelView target,
            Map<String, Key> overrides,
            SophisticatedSnapshot snapshot,
            float rotation,
            Color mapColor
    ) {
        return json.emit(
                key("sophisticatedbackpacks:block/" + part), block, target,
                0F, rotation, 0F, overrides,
                tint -> tint == 0
                        ? snapshot.mainColor().orElse(WHITE)
                        : tint == 1 ? snapshot.accentColor().orElse(WHITE) : WHITE,
                mapColor
        );
    }

    private static Map<Direction, Key> allFaces(Key key) {
        Map<Direction, Key> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, key);
        }
        return Map.copyOf(faces);
    }

    private static SophisticatedBlockEntityData chestVisualData(
            String blockId,
            BlockNeighborhood block,
            SophisticatedBlockEntityData local
    ) {
        Map<String, String> properties = block.getBlockState().getProperties();
        String type = properties.getOrDefault("type", "single");
        if (type.equals("single")) {
            return local;
        }
        if (!type.equals("left") && !type.equals("right")) {
            return null;
        }
        String facingName = properties.getOrDefault("facing", "");
        Direction facing;
        try {
            facing = Direction.fromString(facingName);
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
        if (facing == Direction.UP || facing == Direction.DOWN) {
            return null;
        }
        Direction connected = type.equals("left") ? facing.getRight() : facing.getLeft();
        var offset = connected.toVector();
        var neighbor = block.getNeighborBlock(offset.getX(), offset.getY(), offset.getZ());
        if (!neighbor.getBlockState().getId().getFormatted().equals(blockId)) {
            return null;
        }
        Map<String, String> neighborProperties = neighbor.getBlockState().getProperties();
        String expectedType = type.equals("left") ? "right" : "left";
        if (!expectedType.equals(neighborProperties.get("type"))
                || !facingName.equals(neighborProperties.get("facing"))) {
            return null;
        }
        if (type.equals("right")) {
            return local;
        }
        return neighbor.getBlockEntity() instanceof SophisticatedBlockEntityData main
                ? main
                : null;
    }

    private static void fillSides(Map<Direction, Key> faces, Key key) {
        faces.put(Direction.NORTH, key);
        faces.put(Direction.SOUTH, key);
        faces.put(Direction.WEST, key);
        faces.put(Direction.EAST, key);
    }

    private static int limitedSlots(String blockPath) {
        if (!blockPath.startsWith("limited_") || blockPath.length() < 2) {
            return 0;
        }
        char last = blockPath.charAt(blockPath.length() - 1);
        return last >= '1' && last <= '4' ? last - '0' : 0;
    }

    private static Map<String, Key> barrelTextures(
            String top,
            String side,
            String bottom
    ) {
        Map<String, Key> textures = new HashMap<>();
        textures.put("top", key(top));
        textures.put("side", key(side));
        textures.put("bottom", key(bottom));
        aliasBarrelTextures(textures);
        return Map.copyOf(textures);
    }

    private static void aliasBarrelTextures(Map<String, Key> textures) {
        Key top = textures.get("top");
        Key side = textures.get("side");
        Key bottom = textures.get("bottom");
        if (top != null) {
            textures.putIfAbsent("top_trim", top);
            textures.putIfAbsent("top_inner_trim", top);
            textures.putIfAbsent("particle", top);
        }
        if (side != null) {
            textures.putIfAbsent("side_trim", side);
        }
        if (bottom != null) {
            textures.putIfAbsent("bottom_trim", bottom);
        }
    }

    private static float barrelXRotation(BlockNeighborhood block, boolean limited) {
        if (!limited) {
            return stateRotation(block, "facing", 0);
        }
        return switch (block.getBlockState().getProperties()
                .getOrDefault("vertical_facing", "no")) {
            case "up" -> 0F;
            case "down" -> 180F;
            default -> 90F;
        };
    }

    private static float chestRotation(BlockNeighborhood block) {
        return switch (block.getBlockState().getProperties()
                .getOrDefault("facing", "north")) {
            case "north" -> 90F;
            case "east" -> 180F;
            case "south" -> 270F;
            default -> 0F;
        };
    }

    private static int color(OptionalInt value, int fallback) {
        return value.isPresent() ? value.getAsInt() : fallback;
    }

    private static boolean barrel(String id) {
        return id.endsWith("barrel") || id.contains("barrel_");
    }

    private static boolean chest(String id) {
        return id.endsWith("chest");
    }

    private static boolean shulker(String id) {
        return id.endsWith("shulker_box");
    }

    private static String path(String id) {
        int separator = id.indexOf(':');
        return separator < 0 ? id : id.substring(separator + 1);
    }

    private static String tier(String path) {
        if (path.contains("netherite")) {
            return "netherite";
        }
        if (path.contains("diamond")) {
            return "diamond";
        }
        if (path.contains("gold")) {
            return "gold";
        }
        if (path.contains("iron")) {
            return "iron";
        }
        if (path.contains("copper")) {
            return "copper";
        }
        return "wood";
    }

    private static float horizontalRotation(BlockNeighborhood block) {
        return switch (block.getBlockState().getProperties().getOrDefault("facing", "north")) {
            case "east" -> 90F;
            case "south" -> 180F;
            case "west" -> 270F;
            default -> 0F;
        };
    }

    private static float stateRotation(BlockNeighborhood block, String property, int axis) {
        String facing = block.getBlockState().getProperties().getOrDefault(property, "up");
        if (axis == 0) {
            return switch (facing) {
                case "down" -> 180F;
                case "north", "south", "east", "west" -> 90F;
                default -> 0F;
            };
        }
        return switch (facing) {
            case "east" -> 90F;
            case "south" -> 180F;
            case "west" -> 270F;
            default -> 0F;
        };
    }

    private static Key key(String value) {
        return Key.parse(value);
    }
}
