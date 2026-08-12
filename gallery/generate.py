#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Generate the ATM 1.2.0 Sophisticated stable-optics review gallery."""

from __future__ import annotations

import argparse
from collections import Counter, defaultdict
from dataclasses import dataclass
import hashlib
import json
import os
from pathlib import Path
import sys
from typing import Any, Iterable


GALLERY_ROOT = Path(__file__).resolve().parent
DATAPACK_ROOT = GALLERY_ROOT / "datapack"
FUNCTION_ROOT = DATAPACK_ROOT / "data/sophisticated_gallery/function"

SCHEMA_VERSION = 1
TARGET_PACK_VERSION = "1.2.0"
TARGET_MINECRAFT_VERSION = "1.21.1"
TARGET_NEOFORGE_VERSION = "21.1.248"

TARGET_ARTIFACTS = {
    "sophisticatedstorage": {
        "version": "1.21.1-1.5.83.2017",
        "bytes": 1_828_640,
        "sha256": "354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600",
        "source_correlation": "unresolved; repository sources are reference-only",
        "curseforge_project_id": 619320,
        "curseforge_file_id": 8503122,
    },
    "sophisticatedbackpacks": {
        "version": "1.21.1-3.25.73.2027",
        "bytes": 1_144_235,
        "sha256": "ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e",
        "source_correlation": "unresolved; repository sources are reference-only",
        "curseforge_project_id": 422301,
        "curseforge_file_id": 8569661,
    },
    "sophisticatedcore": {
        "version": "1.21.1-1.4.80.2194",
        "bytes": 1_673_669,
        "sha256": "58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0",
        "source_correlation": "unresolved; repository sources are reference-only",
        "curseforge_project_id": 618298,
        "curseforge_file_id": 8503041,
    },
}

TIERS = ("wood", "copper", "iron", "gold", "diamond", "netherite")
HORIZONTAL_FACINGS = ("north", "east", "south", "west")
ALL_FACINGS = ("north", "east", "south", "west", "up", "down")
VERTICAL_FACINGS = ("no", "up", "down")
WOOD_TYPES = (
    "oak",
    "spruce",
    "birch",
    "jungle",
    "acacia",
    "dark_oak",
    "mangrove",
    "cherry",
    "bamboo",
    "crimson",
    "warped",
)

def opaque_argb(rgb: int) -> int:
    """Return Minecraft's signed NBT int for FastColor.ARGB32.opaque(rgb)."""

    unsigned = 0xFF000000 | rgb
    return unsigned - 0x1_0000_0000 if unsigned >= 0x8000_0000 else unsigned


DYE_PALETTE = tuple(
    (name, opaque_argb(rgb))
    for name, rgb in (
        ("white", 0xF9FFFE),
        ("orange", 0xF9801D),
        ("magenta", 0xC74EBD),
        ("light_blue", 0x3AB3DA),
        ("yellow", 0xFED83D),
        ("lime", 0x80C71F),
        ("pink", 0xF38BAA),
        ("gray", 0x474F52),
        ("light_gray", 0x9D9D97),
        ("cyan", 0x169C9C),
        ("purple", 0x8932B8),
        ("blue", 0x3C44AA),
        ("brown", 0x835432),
        ("green", 0x5E7C16),
        ("red", 0xB02E26),
        ("black", 0x1D1D21),
    )
)

BARREL_MATERIAL_PARTS = (
    "side",
    "side_trim",
    "bottom",
    "bottom_trim",
    "top",
    "top_trim",
    "top_inner_trim",
)
BARREL_MATERIAL_AGGREGATES = (
    "all",
    "all_trim",
    "all_but_trim",
    "top_all",
    "side_all",
    "bottom_all",
)
BARREL_MATERIAL_BLOCKS = (
    "minecraft:stone",
    "minecraft:gold_block",
    "minecraft:bricks",
    "minecraft:iron_block",
    "minecraft:diamond_block",
    "minecraft:copper_block",
    "minecraft:emerald_block",
)


def tiered_id(stem: str, tier: str) -> str:
    if tier == "wood":
        return f"sophisticatedstorage:{stem}"
    return f"sophisticatedstorage:{tier}_{stem}"


def backpack_id(tier: str) -> str:
    prefix = "" if tier == "wood" else f"{tier}_"
    return f"sophisticatedbackpacks:{prefix}backpack"


REGULAR_BARRELS = tuple(tiered_id("barrel", tier) for tier in TIERS)
LIMITED_BARRELS = tuple(
    tiered_id(f"limited_barrel_{slots}", tier)
    if tier == "wood"
    else f"sophisticatedstorage:limited_{tier}_barrel_{slots}"
    for slots in range(1, 5)
    for tier in TIERS
)
CHESTS = tuple(tiered_id("chest", tier) for tier in TIERS)
SHULKERS = tuple(tiered_id("shulker_box", tier) for tier in TIERS)
UTILITY_BLOCKS = (
    "sophisticatedstorage:controller",
    "sophisticatedstorage:storage_link",
    "sophisticatedstorage:storage_io",
    "sophisticatedstorage:storage_input",
    "sophisticatedstorage:storage_output",
)
CONNECTORS = tuple(
    f"sophisticatedstorage:{wood_type}_storage_connector"
    for wood_type in WOOD_TYPES
)
DECORATION_TABLE = "sophisticatedstorage:decoration_table"
BACKPACKS = tuple(backpack_id(tier) for tier in TIERS)
SIMPLE_MATERIAL_BLOCKS = UTILITY_BLOCKS + CONNECTORS
EXPECTED_BLOCK_IDS = frozenset(
    REGULAR_BARRELS
    + LIMITED_BARRELS
    + CHESTS
    + SHULKERS
    + UTILITY_BLOCKS
    + CONNECTORS
    + (DECORATION_TABLE,)
    + BACKPACKS
)

if len(EXPECTED_BLOCK_IDS) != 65:
    raise RuntimeError(f"expected 65 placed block IDs, got {len(EXPECTED_BLOCK_IDS)}")

RENDER_CONTRACTS = {
    "regular_barrel": {
        "block_ids": list(REGULAR_BARRELS),
        "model_roots": [
            "sophisticatedstorage:models/block/barrel.json",
            "sophisticatedstorage:models/block/flat/barrel.json",
        ],
        "loader": "sophisticatedstorage:barrel",
        "stable_inputs": [
            "facing",
            "flat_top",
            "woodType",
            "packed",
            "storageWrapper.mainColor",
            "storageWrapper.accentColor",
            "materials",
            "locked/showLock",
            "showTier",
        ],
        "texture_roots": ["sophisticatedstorage:textures/block/*barrel*"],
    },
    "limited_barrel": {
        "block_ids": list(LIMITED_BARRELS),
        "model_roots": [
            "sophisticatedstorage:models/block/limited_barrel_{1..4}.json"
        ],
        "loader": "sophisticatedstorage:limited_barrel",
        "stable_inputs": [
            "facing",
            "vertical_facing",
            "flat_top",
            "tier and slot-layout",
            "wood/color/material/presentation inputs shared with regular barrels",
        ],
        "texture_roots": [
            "sophisticatedstorage:textures/block/limited_*barrel*"
        ],
    },
    "chest": {
        "block_ids": list(CHESTS),
        "model_roots": ["sophisticatedstorage:models/block/chest.json"],
        "loader": "sophisticatedstorage:chest",
        "body_renderer": "ChestRenderer",
        "stable_inputs": [
            "facing",
            "type=single|left|right",
            "reciprocal RIGHT-main/LEFT-secondary topology",
            "woodType",
            "packed",
            "main/accent colors and presentation controls",
        ],
        "texture_roots": [
            "sophisticatedstorage:textures/entity/chest/{wood,tier,tint,packed}.png",
            "sophisticatedstorage:textures/entity/chest/left_{wood,tier,tint,packed}.png",
            "sophisticatedstorage:textures/entity/chest/right_{wood,tier,tint,packed}.png",
        ],
    },
    "shulker_box": {
        "block_ids": list(SHULKERS),
        "model_roots": ["sophisticatedstorage:models/block/shulker_box.json"],
        "loader": "sophisticatedstorage:shulker_box",
        "body_renderer": "ShulkerBoxRenderer",
        "stable_inputs": [
            "facing",
            "tier",
            "main/accent colors",
            "locked/showLock",
            "showTier",
        ],
        "texture_roots": [
            "sophisticatedstorage:textures/entity/shulker_box/{no_tint,tintable_main,tintable_accent,*_tier}.png"
        ],
    },
    "simple_material": {
        "block_ids": list(SIMPLE_MATERIAL_BLOCKS),
        "model_roots": [
            "sophisticatedstorage:models/block/{controller,storage_link,storage_io,storage_input,storage_output,*_storage_connector}.json"
        ],
        "loader": "sophisticatedstorage:simple_material",
        "stable_inputs": ["material", "overlayHidden", "opaque", "storage_link.facing"],
        "texture_roots": [
            "each model's nested base and overlay texture references plus the selected material block"
        ],
    },
    "decoration_table": {
        "block_ids": [DECORATION_TABLE],
        "model_roots": [
            "sophisticatedstorage:models/block/decoration_table.json"
        ],
        "loader": "ordinary JSON elements emitted by SophisticatedRenderer",
        "stable_inputs": ["facing"],
        "excluded_renderer": "DecorationTableRenderer transient item display",
    },
    "backpack": {
        "block_ids": list(BACKPACKS),
        "model_roots": [
            "sophisticatedbackpacks:models/block/backpack.json",
            "sophisticatedbackpacks:models/block/{copper,iron,gold,diamond,netherite}_backpack.json",
        ],
        "loader": "sophisticatedbackpacks:backpack",
        "stable_model_parts": [
            "backpack_base",
            "backpack_straps",
            "backpack_front_pouch",
            "backpack_left_pouch",
            "backpack_right_pouch",
            "backpack_left_tank",
            "backpack_right_tank",
            "backpack_battery",
        ],
        "excluded_model_parts": [
            "backpack_left_tank_fluid",
            "backpack_right_tank_fluid",
            "backpack_battery_charge",
            "backpack_display_item",
        ],
        "stable_inputs": [
            "facing",
            "tier block ID and matching backpackData item ID",
            "backpackData main/accent color components",
            "left_tank/right_tank/battery exterior presence",
        ],
        "texture_roots": [
            "sophisticatedbackpacks:textures/block/backpack_{cloth,border,modules,modules_glass}.png",
            "sophisticatedbackpacks:textures/block/{leather,copper,iron,gold,diamond,netherite}_clips.png",
        ],
    },
}


@dataclass(frozen=True, order=True)
class Position:
    x: int
    y: int
    z: int

    def command(self) -> str:
        return f"{self.x} {self.y} {self.z}"

    def manifest(self) -> dict[str, int]:
        return {"x": self.x, "y": self.y, "z": self.z}


@dataclass(frozen=True)
class Anchor:
    anchor_id: str
    case_id: str
    position: Position
    block_id: str
    properties: dict[str, str]
    block_entity_id: str
    nbt: dict[str, Any]
    expected_route: str
    route_reason: str
    stable_optics: tuple[str, ...]
    notes: str = ""

    def block_spec(self) -> str:
        if not self.properties:
            return self.block_id
        properties = ",".join(
            f"{key}={value}" for key, value in sorted(self.properties.items())
        )
        return f"{self.block_id}[{properties}]"

    def manifest(self) -> dict[str, Any]:
        return {
            "anchor_id": self.anchor_id,
            "case_id": self.case_id,
            "position": self.position.manifest(),
            "block_id": self.block_id,
            "blockstate": dict(sorted(self.properties.items())),
            "block_entity_id": self.block_entity_id,
            "nbt": self.nbt,
            "expected_route": self.expected_route,
            "route_reason": self.route_reason,
            "stable_optics": list(self.stable_optics),
            "notes": self.notes,
        }


class Allocator:
    """Allocate well-separated fixture cells in a deterministic grid."""

    def __init__(self) -> None:
        self.index = 0

    def next(self) -> Position:
        column = self.index % 22
        row = self.index // 22
        self.index += 1
        return Position(196 + column * 3, 100, 196 + row * 3)


def default_properties(block_id: str) -> dict[str, str]:
    if block_id in LIMITED_BARRELS:
        return {
            "facing": "north",
            "flat_top": "false",
            "opaque": "true",
            "ticking": "false",
            "vertical_facing": "no",
        }
    if block_id in REGULAR_BARRELS:
        return {
            "facing": "north",
            "flat_top": "false",
            "opaque": "true",
            "open": "false",
            "ticking": "false",
        }
    if block_id in CHESTS:
        return {
            "facing": "north",
            "ticking": "false",
            "type": "single",
            "waterlogged": "false",
        }
    if block_id in SHULKERS:
        return {"facing": "up"}
    if block_id == "sophisticatedstorage:storage_link":
        return {"facing": "up", "opaque": "true"}
    if block_id in SIMPLE_MATERIAL_BLOCKS:
        return {"opaque": "true"}
    if block_id == DECORATION_TABLE:
        return {"facing": "north"}
    if block_id in BACKPACKS:
        return {
            "battery": "false",
            "facing": "north",
            "left_tank": "false",
            "open": "false",
            "right_tank": "false",
            "waterlogged": "false",
        }
    raise ValueError(f"unknown block ID: {block_id}")


def block_entity_id(block_id: str) -> str:
    if block_id in REGULAR_BARRELS:
        return "sophisticatedstorage:barrel"
    if block_id in LIMITED_BARRELS:
        return "sophisticatedstorage:limited_barrel"
    if block_id in CHESTS:
        return "sophisticatedstorage:chest"
    if block_id in SHULKERS:
        return "sophisticatedstorage:shulker_box"
    if block_id == "sophisticatedstorage:controller":
        return "sophisticatedstorage:controller"
    if block_id == "sophisticatedstorage:storage_link":
        return "sophisticatedstorage:storage_link"
    if block_id == "sophisticatedstorage:storage_io":
        return "sophisticatedstorage:storage_io"
    if block_id == "sophisticatedstorage:storage_input":
        return "sophisticatedstorage:storage_input"
    if block_id == "sophisticatedstorage:storage_output":
        return "sophisticatedstorage:storage_output"
    if block_id in CONNECTORS:
        return "sophisticatedstorage:storage_connector"
    if block_id == DECORATION_TABLE:
        return "sophisticatedstorage:decoration_table"
    if block_id in BACKPACKS:
        return "sophisticatedbackpacks:backpack"
    raise ValueError(f"unknown block ID: {block_id}")


def route_for(block_id: str) -> tuple[str, str]:
    if block_id == DECORATION_TABLE:
        return (
            "custom",
            "SophisticatedRenderer emits the ordinary JSON element model; temporary block-entity item display is out of scope",
        )
    if block_id in REGULAR_BARRELS:
        return (
            "custom",
            "sophisticatedstorage:barrel geometry loader plus block-entity model data",
        )
    if block_id in LIMITED_BARRELS:
        return (
            "custom",
            "sophisticatedstorage:limited_barrel geometry loader plus block-entity model data",
        )
    if block_id in CHESTS:
        return (
            "custom",
            "entity-animated body renderer selects single/left/right entity models and textures",
        )
    if block_id in SHULKERS:
        return (
            "custom",
            "entity-animated body renderer selects tier and dye entity textures",
        )
    if block_id in SIMPLE_MATERIAL_BLOCKS:
        return (
            "custom",
            "sophisticatedstorage:simple_material geometry loader selects persisted material and overlay",
        )
    if block_id in BACKPACKS:
        return (
            "custom",
            "sophisticatedbackpacks:backpack geometry loader selects tier, dyes, orientation, and module parts",
        )
    raise ValueError(f"unknown block ID: {block_id}")


def storage_nbt(
    block_id: str,
    *,
    wood_type: str | None = None,
    packed: bool | None = None,
    main_color: int | None = None,
    accent_color: int | None = None,
    locked: bool | None = None,
    show_lock: bool | None = None,
    show_tier: bool | None = None,
    materials: dict[str, str] | None = None,
) -> dict[str, Any]:
    if block_id not in REGULAR_BARRELS + LIMITED_BARRELS + CHESTS + SHULKERS:
        raise ValueError(f"not a storage block: {block_id}")
    root: dict[str, Any] = {}
    wrapper: dict[str, Any] = {}
    if main_color is not None:
        wrapper["mainColor"] = main_color
    if accent_color is not None:
        wrapper["accentColor"] = accent_color
    if wrapper:
        root["storageWrapper"] = wrapper
    if block_id in REGULAR_BARRELS + LIMITED_BARRELS + CHESTS:
        if wood_type is not None:
            root["woodType"] = wood_type
        if packed is not None:
            root["packed"] = packed
    if locked is not None:
        root["locked"] = locked
    if show_lock is not None:
        root["showLock"] = show_lock
    if show_tier is not None:
        root["showTier"] = show_tier
    if materials is not None:
        if block_id not in REGULAR_BARRELS + LIMITED_BARRELS:
            raise ValueError("barrel materials used on a non-barrel")
        root["materials"] = dict(sorted(materials.items()))
    return root


def backpack_nbt(
    block_id: str,
    *,
    main_color: int | None = None,
    accent_color: int | None = None,
    left_tank: bool = False,
    right_tank: bool = False,
    battery: bool = False,
) -> dict[str, Any]:
    components: dict[str, Any] = {}
    if main_color is not None:
        components["sophisticatedcore:main_color"] = main_color
    if accent_color is not None:
        components["sophisticatedcore:accent_color"] = accent_color
    render_info: dict[str, Any] = {}
    tanks: list[dict[str, Any]] = []
    if left_tank:
        tanks.append({"position": "left", "info": {}})
    if right_tank:
        tanks.append({"position": "right", "info": {}})
    if tanks:
        render_info["tanks"] = tanks
    if battery:
        # Compound presence selects the stable exterior shell. Omitting the
        # chargeRatio field deserializes to zero without adding charge data.
        render_info["battery"] = {}
    if render_info:
        components["sophisticatedcore:render_info_tag"] = render_info
    stack: dict[str, Any] = {"id": block_id, "count": 1}
    if components:
        stack["components"] = components
    return {"backpackData": stack}


def simple_material_nbt(
    material: str, *, overlay_hidden: bool = False
) -> dict[str, Any]:
    root: dict[str, Any] = {"material": material}
    if overlay_hidden:
        root["overlayHidden"] = True
    return root


def static_nbt(block_id: str) -> dict[str, Any]:
    if block_id in REGULAR_BARRELS + LIMITED_BARRELS + CHESTS:
        return storage_nbt(block_id, wood_type="oak", packed=False)
    if block_id in SHULKERS:
        return storage_nbt(block_id)
    if block_id in BACKPACKS:
        return backpack_nbt(block_id)
    return {}


def snbt(value: Any) -> str:
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, int):
        return str(value)
    if isinstance(value, str):
        return json.dumps(value, ensure_ascii=False, separators=(",", ":"))
    if isinstance(value, list):
        return "[" + ",".join(snbt(item) for item in value) + "]"
    if isinstance(value, dict):
        return "{" + ",".join(
            f"{json.dumps(str(key), ensure_ascii=False)}:{snbt(item)}"
            for key, item in sorted(value.items())
        ) + "}"
    raise TypeError(f"unsupported SNBT value: {type(value).__name__}")


def slug(value: str) -> str:
    return value.replace(":", "-").replace("/", "-").replace("_", "-")


def build_anchors() -> list[Anchor]:
    allocator = Allocator()
    anchors: list[Anchor] = []

    def add(
        anchor_id: str,
        case_id: str,
        block_id: str,
        *,
        properties: dict[str, str] | None = None,
        nbt: dict[str, Any] | None = None,
        stable_optics: Iterable[str] = (),
        notes: str = "",
        position: Position | None = None,
    ) -> Anchor:
        route, reason = route_for(block_id)
        anchor = Anchor(
            anchor_id=anchor_id,
            case_id=case_id,
            position=position or allocator.next(),
            block_id=block_id,
            properties=properties or default_properties(block_id),
            block_entity_id=block_entity_id(block_id),
            nbt=static_nbt(block_id) if nbt is None else nbt,
            expected_route=route,
            route_reason=reason,
            stable_optics=tuple(stable_optics),
            notes=notes,
        )
        anchors.append(anchor)
        return anchor

    # Complete registry roster: every placed ID appears in this case exactly once.
    for block_id in sorted(EXPECTED_BLOCK_IDS):
        add(
            f"roster-{slug(block_id)}",
            "registry-roster",
            block_id,
            stable_optics=("registered-block-id", "default-static-state"),
        )

    # Regular barrel six-direction and flat-top cross product.
    for facing in ALL_FACINGS:
        for flat_top in (False, True):
            properties = default_properties("sophisticatedstorage:netherite_barrel")
            properties.update(
                {"facing": facing, "flat_top": str(flat_top).lower()}
            )
            add(
                f"barrel-orientation-{facing}-flat-{str(flat_top).lower()}",
                "regular-barrel-orientation-flat-top",
                "sophisticatedstorage:netherite_barrel",
                properties=properties,
                nbt=storage_nbt(
                    "sophisticatedstorage:netherite_barrel",
                    wood_type="oak",
                    packed=False,
                ),
                stable_optics=("facing", "flat-top", "tier"),
            )

    # Limited barrel horizontal reference, vertical direction, and flat-top.
    limited_reference = "sophisticatedstorage:limited_netherite_barrel_4"
    for facing in HORIZONTAL_FACINGS:
        for vertical_facing in VERTICAL_FACINGS:
            for flat_top in (False, True):
                properties = default_properties(limited_reference)
                properties.update(
                    {
                        "facing": facing,
                        "vertical_facing": vertical_facing,
                        "flat_top": str(flat_top).lower(),
                    }
                )
                add(
                    f"limited-orientation-{facing}-{vertical_facing}-flat-{str(flat_top).lower()}",
                    "limited-barrel-orientation-flat-top",
                    limited_reference,
                    properties=properties,
                    nbt=storage_nbt(
                        limited_reference, wood_type="oak", packed=False
                    ),
                    stable_optics=(
                        "horizontal-facing",
                        "vertical-facing",
                        "flat-top",
                        "slot-layout",
                    ),
                )

    # Single chest orientation.
    for facing in HORIZONTAL_FACINGS:
        properties = default_properties("sophisticatedstorage:chest")
        properties["facing"] = facing
        add(
            f"chest-single-{facing}",
            "chest-single-orientation",
            "sophisticatedstorage:chest",
            properties=properties,
            nbt=storage_nbt(
                "sophisticatedstorage:chest", wood_type="oak", packed=False
            ),
            stable_optics=("facing", "single-topology"),
        )

    # Every tier and facing in a correct double-chest RIGHT-main/LEFT-secondary pair.
    left_offset = {
        "north": (-1, 0),
        "east": (0, -1),
        "south": (1, 0),
        "west": (0, 1),
    }
    for tier in TIERS:
        chest = tiered_id("chest", tier)
        for facing in HORIZONTAL_FACINGS:
            main = allocator.next()
            dx, dz = left_offset[facing]
            secondary = Position(main.x + dx, main.y, main.z + dz)
            pair_nbt = storage_nbt(chest, wood_type="oak", packed=False)
            right_properties = default_properties(chest)
            right_properties.update({"facing": facing, "type": "right"})
            left_properties = default_properties(chest)
            left_properties.update({"facing": facing, "type": "left"})
            case_id = f"chest-double-{tier}-{facing}"
            add(
                f"{case_id}-right-main",
                "chest-double-topology",
                chest,
                position=main,
                properties=right_properties,
                nbt=pair_nbt,
                stable_optics=("tier", "facing", "right-main-half"),
                notes="RIGHT is the main block entity; LEFT resolves to this position.",
            )
            add(
                f"{case_id}-left-secondary",
                "chest-double-topology",
                chest,
                position=secondary,
                properties=left_properties,
                nbt=pair_nbt,
                stable_optics=("tier", "facing", "left-secondary-half"),
                notes="LEFT is placed in the facing-dependent counter-clockwise neighbor position.",
            )

    waterlogged_chest_properties = default_properties(
        "sophisticatedstorage:diamond_chest"
    )
    waterlogged_chest_properties["waterlogged"] = "true"
    add(
        "chest-waterlogged-true",
        "waterlogged-static-state",
        "sophisticatedstorage:diamond_chest",
        properties=waterlogged_chest_properties,
        nbt=storage_nbt(
            "sophisticatedstorage:diamond_chest", wood_type="oak", packed=False
        ),
        stable_optics=("waterlogged", "tier"),
    )

    for facing in ALL_FACINGS:
        properties = default_properties("sophisticatedstorage:storage_link")
        properties["facing"] = facing
        add(
            f"storage-link-facing-{facing}",
            "storage-link-orientation",
            "sophisticatedstorage:storage_link",
            properties=properties,
            nbt=simple_material_nbt("minecraft:stone"),
            stable_optics=("storage-link-facing", facing, "simple-material"),
        )

    for facing in HORIZONTAL_FACINGS:
        add(
            f"decoration-table-facing-{facing}",
            "decoration-table-orientation",
            DECORATION_TABLE,
            properties={"facing": facing},
            nbt={},
            stable_optics=("custom-structural-route", "facing", facing),
        )

    # Shulker tier and six-facing full cross product.
    for tier in TIERS:
        shulker = tiered_id("shulker_box", tier)
        for facing in ALL_FACINGS:
            add(
                f"shulker-{tier}-{facing}",
                "shulker-tier-orientation",
                shulker,
                properties={"facing": facing},
                nbt=storage_nbt(shulker),
                stable_optics=("tier", "facing", "closed-body"),
            )

    # Every persisted vanilla wood type on both wood-backed body families.
    for family, block_id in (
        ("barrel", "sophisticatedstorage:barrel"),
        ("chest", "sophisticatedstorage:chest"),
    ):
        for wood_type in WOOD_TYPES:
            add(
                f"wood-{family}-{wood_type}",
                "wood-type-catalog",
                block_id,
                nbt=storage_nbt(block_id, wood_type=wood_type, packed=False),
                stable_optics=("wood-type", family),
            )
    for wood_type in WOOD_TYPES:
        add(
            f"wood-limited-{wood_type}",
            "wood-type-catalog",
            "sophisticatedstorage:limited_barrel_4",
            nbt=storage_nbt(
                "sophisticatedstorage:limited_barrel_4",
                wood_type=wood_type,
                packed=False,
            ),
            stable_optics=("wood-type", wood_type, "limited-barrel"),
        )

    # All dye colors on each tintable placed family. The dual channel uses a
    # deterministic complementary pairing rather than an unbounded 16x16 grid.
    dye_families = (
        ("barrel", "sophisticatedstorage:diamond_barrel"),
        ("limited", "sophisticatedstorage:limited_diamond_barrel_4"),
        ("chest", "sophisticatedstorage:diamond_chest"),
        ("shulker", "sophisticatedstorage:diamond_shulker_box"),
        ("backpack", "sophisticatedbackpacks:diamond_backpack"),
    )
    for family, block_id in dye_families:
        for index, (color_name, color) in enumerate(DYE_PALETTE):
            if block_id in BACKPACKS:
                main_nbt = backpack_nbt(block_id, main_color=color)
                accent_nbt = backpack_nbt(block_id, accent_color=color)
                pair_nbt = backpack_nbt(
                    block_id,
                    main_color=color,
                    accent_color=DYE_PALETTE[-1 - index][1],
                )
            else:
                main_nbt = storage_nbt(
                    block_id,
                    wood_type="oak" if block_id not in SHULKERS else None,
                    packed=False if block_id not in SHULKERS else None,
                    main_color=color,
                )
                accent_nbt = storage_nbt(
                    block_id,
                    wood_type="oak" if block_id not in SHULKERS else None,
                    packed=False if block_id not in SHULKERS else None,
                    accent_color=color,
                )
                pair_nbt = storage_nbt(
                    block_id,
                    wood_type="oak" if block_id not in SHULKERS else None,
                    packed=False if block_id not in SHULKERS else None,
                    main_color=color,
                    accent_color=DYE_PALETTE[-1 - index][1],
                )
            add(
                f"dye-{family}-main-{color_name}",
                f"dye-{family}",
                block_id,
                nbt=main_nbt,
                stable_optics=("main-color", color_name),
            )
            add(
                f"dye-{family}-accent-{color_name}",
                f"dye-{family}",
                block_id,
                nbt=accent_nbt,
                stable_optics=("accent-color", color_name),
            )
            add(
                f"dye-{family}-dual-{color_name}-{DYE_PALETTE[-1 - index][0]}",
                f"dye-{family}",
                block_id,
                nbt=pair_nbt,
                stable_optics=(
                    "dual-color",
                    color_name,
                    DYE_PALETTE[-1 - index][0],
                ),
            )

    # Every leaf and aggregate barrel selector, one mixed topology, and
    # transparent camo. Aggregates are persisted aliases which expand to the
    # leaf topology in the exact runtime renderer.
    material_barrel = "sophisticatedstorage:diamond_barrel"
    for part, material in zip(BARREL_MATERIAL_PARTS, BARREL_MATERIAL_BLOCKS):
        add(
            f"barrel-material-{part}",
            "barrel-material-topology",
            material_barrel,
            nbt=storage_nbt(
                material_barrel,
                wood_type="oak",
                packed=False,
                materials={part: material},
            ),
            stable_optics=("barrel-material", part, material),
        )
    for index, selector in enumerate(BARREL_MATERIAL_AGGREGATES):
        material = BARREL_MATERIAL_BLOCKS[index]
        add(
            f"barrel-material-{selector}",
            "barrel-material-topology",
            material_barrel,
            nbt=storage_nbt(
                material_barrel,
                wood_type="oak",
                packed=False,
                materials={selector: material},
            ),
            stable_optics=("barrel-material-aggregate", selector, material),
        )
    add(
        "barrel-material-mixed-seven-leaf-parts",
        "barrel-material-topology",
        material_barrel,
        nbt=storage_nbt(
            material_barrel,
            wood_type="oak",
            packed=False,
            materials=dict(zip(BARREL_MATERIAL_PARTS, BARREL_MATERIAL_BLOCKS)),
        ),
        stable_optics=("barrel-material", "mixed-seven-leaf-parts"),
    )
    transparent_properties = default_properties(material_barrel)
    transparent_properties["opaque"] = "false"
    add(
        "barrel-material-transparent-glass",
        "barrel-material-topology",
        material_barrel,
        properties=transparent_properties,
        nbt=storage_nbt(
            material_barrel,
            wood_type="oak",
            packed=False,
            materials={part: "minecraft:glass" for part in BARREL_MATERIAL_PARTS},
        ),
        stable_optics=("barrel-material", "transparent", "opaque-false"),
    )
    add(
        "limited-barrel-material-mixed-seven-leaf-parts",
        "barrel-material-topology",
        "sophisticatedstorage:limited_diamond_barrel_4",
        nbt=storage_nbt(
            "sophisticatedstorage:limited_diamond_barrel_4",
            wood_type="oak",
            packed=False,
            materials=dict(zip(BARREL_MATERIAL_PARTS, BARREL_MATERIAL_BLOCKS)),
        ),
        stable_optics=(
            "limited-barrel-material",
            "mixed-seven-leaf-parts",
            "distinct-limited-loader",
        ),
    )

    # Every simple-material body receives a persisted camo. Add independent
    # overlay-hidden and transparent-material representatives.
    simple_material_cycle = (
        "minecraft:stone",
        "minecraft:bricks",
        "minecraft:oak_planks",
        "minecraft:copper_block",
        "minecraft:deepslate_tiles",
        "minecraft:quartz_block",
        "minecraft:amethyst_block",
        "minecraft:moss_block",
    )
    for index, block_id in enumerate(SIMPLE_MATERIAL_BLOCKS):
        material = simple_material_cycle[index % len(simple_material_cycle)]
        add(
            f"simple-material-{slug(block_id)}",
            "simple-material-catalog",
            block_id,
            nbt=simple_material_nbt(material),
            stable_optics=("simple-material", material, "overlay-visible"),
        )
    add(
        "simple-material-controller-overlay-hidden",
        "simple-material-presentation",
        "sophisticatedstorage:controller",
        nbt=simple_material_nbt("minecraft:stone", overlay_hidden=True),
        stable_optics=("simple-material", "overlay-hidden"),
    )
    glass_io_properties = default_properties("sophisticatedstorage:storage_io")
    glass_io_properties["opaque"] = "false"
    add(
        "simple-material-storage-io-glass",
        "simple-material-presentation",
        "sophisticatedstorage:storage_io",
        properties=glass_io_properties,
        nbt=simple_material_nbt("minecraft:glass"),
        stable_optics=("simple-material", "transparent", "opaque-false"),
    )

    # Lock badge, hidden lock badge, and hidden tier badge for each storage
    # body family. Live upgrade-display state is deliberately out of scope.
    presentation_families = (
        ("barrel", "sophisticatedstorage:netherite_barrel"),
        ("limited", "sophisticatedstorage:limited_netherite_barrel_4"),
        ("chest", "sophisticatedstorage:netherite_chest"),
        ("shulker", "sophisticatedstorage:netherite_shulker_box"),
    )
    for family, block_id in presentation_families:
        wood = "oak" if block_id not in SHULKERS else None
        packed = False if block_id not in SHULKERS else None
        add(
            f"presentation-{family}-locked-visible",
            "storage-presentation-controls",
            block_id,
            nbt=storage_nbt(
                block_id,
                wood_type=wood,
                packed=packed,
                locked=True,
            ),
            stable_optics=("locked", "lock-visible", "tier-visible"),
        )
        add(
            f"presentation-{family}-locked-hidden",
            "storage-presentation-controls",
            block_id,
            nbt=storage_nbt(
                block_id,
                wood_type=wood,
                packed=packed,
                locked=True,
                show_lock=False,
            ),
            stable_optics=("locked", "lock-hidden", "tier-visible"),
        )
        add(
            f"presentation-{family}-tier-hidden",
            "storage-presentation-controls",
            block_id,
            nbt=storage_nbt(
                block_id,
                wood_type=wood,
                packed=packed,
                show_tier=False,
            ),
            stable_optics=("unlocked", "tier-hidden"),
        )

    # Packed body treatment for every regular barrel, limited barrel, and
    # chest tier.
    for tier in TIERS:
        for family in ("barrel", "chest"):
            block_id = tiered_id(family, tier)
            add(
                f"packed-{family}-{tier}",
                "packed-storage-tier-catalog",
                block_id,
                nbt=storage_nbt(block_id, wood_type="oak", packed=True),
                stable_optics=("packed", "tier", tier),
            )
        limited_id = (
            "sophisticatedstorage:limited_barrel_4"
            if tier == "wood"
            else f"sophisticatedstorage:limited_{tier}_barrel_4"
        )
        add(
            f"packed-limited-{tier}",
            "packed-storage-tier-catalog",
            limited_id,
            nbt=storage_nbt(limited_id, wood_type="oak", packed=True),
            stable_optics=("packed", "limited-barrel", "tier", tier),
        )

    # Backpack tier/facing cross product.
    for tier in TIERS:
        block_id = backpack_id(tier)
        for facing in HORIZONTAL_FACINGS:
            properties = default_properties(block_id)
            properties["facing"] = facing
            add(
                f"backpack-{tier}-{facing}",
                "backpack-tier-orientation",
                block_id,
                properties=properties,
                nbt=backpack_nbt(block_id),
                stable_optics=("backpack-tier", tier, "facing", facing),
            )

    # Complete three-bit exterior module-presence matrix for every facing.
    module_backpack = "sophisticatedbackpacks:netherite_backpack"
    for facing in HORIZONTAL_FACINGS:
        for mask in range(8):
            left_tank = bool(mask & 1)
            right_tank = bool(mask & 2)
            battery = bool(mask & 4)
            properties = default_properties(module_backpack)
            properties.update(
                {
                    "facing": facing,
                    "left_tank": str(left_tank).lower(),
                    "right_tank": str(right_tank).lower(),
                    "battery": str(battery).lower(),
                }
            )
            add(
                f"backpack-modules-{facing}-{mask:03b}",
                "backpack-module-presence",
                module_backpack,
                properties=properties,
        nbt=backpack_nbt(
            module_backpack,
            left_tank=left_tank,
            right_tank=right_tank,
            battery=battery,
        ),
                stable_optics=(
                    "facing",
                    "left-tank-present" if left_tank else "left-tank-absent",
                    "right-tank-present" if right_tank else "right-tank-absent",
                    "battery-present" if battery else "battery-absent",
                ),
                notes="Persisted exterior module presence only; render-info entries contain no fluid, fillRatio, or chargeRatio.",
            )

    waterlogged_backpack_properties = default_properties(
        "sophisticatedbackpacks:diamond_backpack"
    )
    waterlogged_backpack_properties["waterlogged"] = "true"
    add(
        "backpack-waterlogged-true",
        "waterlogged-static-state",
        "sophisticatedbackpacks:diamond_backpack",
        properties=waterlogged_backpack_properties,
        nbt=backpack_nbt("sophisticatedbackpacks:diamond_backpack"),
        stable_optics=("waterlogged", "backpack-tier"),
    )

    validate_anchors(anchors)
    return anchors


def validate_anchors(anchors: list[Anchor]) -> None:
    ids = [anchor.anchor_id for anchor in anchors]
    if len(ids) != len(set(ids)):
        duplicates = sorted(key for key, count in Counter(ids).items() if count > 1)
        raise ValueError(f"duplicate anchor IDs: {duplicates}")

    positions = [anchor.position for anchor in anchors]
    if len(positions) != len(set(positions)):
        duplicates = sorted(
            key for key, count in Counter(positions).items() if count > 1
        )
        raise ValueError(f"duplicate positions: {duplicates}")

    roster = {
        anchor.block_id
        for anchor in anchors
        if anchor.case_id == "registry-roster"
    }
    if roster != EXPECTED_BLOCK_IDS:
        missing = sorted(EXPECTED_BLOCK_IDS - roster)
        extra = sorted(roster - EXPECTED_BLOCK_IDS)
        raise ValueError(f"registry roster mismatch; missing={missing}, extra={extra}")

    non_custom = [
        anchor.anchor_id for anchor in anchors if anchor.expected_route != "custom"
    ]
    if non_custom:
        raise ValueError(f"all gallery anchors must use the custom route: {non_custom}")
    decoration_routes = {
        anchor.expected_route
        for anchor in anchors
        if anchor.block_id == DECORATION_TABLE
    }
    if decoration_routes != {"custom"}:
        raise ValueError(
            f"decoration table must use the custom route: {sorted(decoration_routes)}"
        )

    for anchor in anchors:
        if anchor.block_id not in EXPECTED_BLOCK_IDS:
            raise ValueError(f"unexpected block ID: {anchor.block_id}")
        if anchor.properties.get("open") == "true":
            raise ValueError(f"open animation state forbidden: {anchor.anchor_id}")
        if anchor.properties.get("ticking") == "true":
            raise ValueError(f"activity/ticking state forbidden: {anchor.anchor_id}")
        if not (194 <= anchor.position.x <= 264):
            raise ValueError(f"anchor outside x bounds: {anchor}")
        if not (194 <= anchor.position.z <= 280):
            raise ValueError(f"anchor outside z bounds: {anchor}")
        if anchor.position.y != 100:
            raise ValueError(f"anchor outside y plane: {anchor}")

    # Gallery NBT must never include the live inventory-display, fluid, charge,
    # activity, or lid/open animation data intentionally excluded by scope.
    forbidden_nbt_keys = {
        "itemDisplay",
        "fluid",
        "fillRatio",
        "chargeRatio",
        "upgradeInventory",
        "showUpgrades",
        "upgradeItems",
    }

    def walk(value: Any) -> Iterable[str]:
        if isinstance(value, dict):
            for key, item in value.items():
                yield key
                yield from walk(item)
        elif isinstance(value, list):
            for item in value:
                yield from walk(item)

    for anchor in anchors:
        bad = forbidden_nbt_keys.intersection(walk(anchor.nbt))
        if bad:
            raise ValueError(
                f"forbidden live-state NBT in {anchor.anchor_id}: {sorted(bad)}"
            )


def case_summaries(anchors: list[Anchor]) -> list[dict[str, Any]]:
    grouped: dict[str, list[Anchor]] = defaultdict(list)
    for anchor in anchors:
        grouped[anchor.case_id].append(anchor)
    return [
        {
            "case_id": case_id,
            "anchor_count": len(case_anchors),
            "custom_count": sum(
                anchor.expected_route == "custom" for anchor in case_anchors
            ),
            "stock_count": sum(
                anchor.expected_route == "stock" for anchor in case_anchors
            ),
            "anchor_ids": [anchor.anchor_id for anchor in case_anchors],
        }
        for case_id, case_anchors in sorted(grouped.items())
    ]


def manifest(anchors: list[Anchor]) -> dict[str, Any]:
    routes = Counter(anchor.expected_route for anchor in anchors)
    return {
        "schema_version": SCHEMA_VERSION,
        "identity": {
            "all_the_mons": TARGET_PACK_VERSION,
            "minecraft": TARGET_MINECRAFT_VERSION,
            "neoforge": TARGET_NEOFORGE_VERSION,
            "source_correlation": "unresolved; repository sources are reference-only",
            "artifacts": TARGET_ARTIFACTS,
        },
        "gallery": {
            "namespace": "sophisticated_gallery",
            "coordinate_bounds": {
                "min": {"x": 194, "y": 99, "z": 194},
                "max": {"x": 264, "y": 102, "z": 280},
            },
            "anchor_count": len(anchors),
            "case_count": len({anchor.case_id for anchor in anchors}),
            "registered_block_id_count": len(EXPECTED_BLOCK_IDS),
            "route_counts": dict(sorted(routes.items())),
            "stock_route_block_ids": [],
        },
        "scope": {
            "included": [
                "placed block IDs and blockstates",
                "block-entity IDs and persisted stable appearance NBT",
                "tier, facing, flat-top, vertical-facing, and chest-pair topology",
                "wood type, packed state, dyes, barrel materials, and simple-material camos",
                "all Storage Link and Decoration Table facing states",
                "stable lock/tier presentation controls",
                "backpack tier, orientation, and persisted exterior tank/battery presence",
            ],
            "excluded": [
                "stored items, counts, slot-fill, and item-display contents",
                "fluid identity or fill and battery charge",
                "activity, ticking, LEDs, particles, and effects",
                "open/lid animation and transient decoration-table display",
            ],
        },
        "render_contracts": RENDER_CONTRACTS,
        "cases": case_summaries(anchors),
        "anchors": [anchor.manifest() for anchor in anchors],
    }


def generated_files(anchors: list[Anchor]) -> dict[Path, bytes]:
    manifest_data = manifest(anchors)
    cases_json = (
        json.dumps(manifest_data, indent=2, sort_keys=True, ensure_ascii=False)
        + "\n"
    ).encode()

    tsv_lines = [
        "anchor_id\tcase_id\tx\ty\tz\tblock_spec\tblock_entity_id\texpected_route\tstable_optics"
    ]
    for anchor in anchors:
        tsv_lines.append(
            "\t".join(
                (
                    anchor.anchor_id,
                    anchor.case_id,
                    str(anchor.position.x),
                    str(anchor.position.y),
                    str(anchor.position.z),
                    anchor.block_spec(),
                    anchor.block_entity_id,
                    anchor.expected_route,
                    ",".join(anchor.stable_optics),
                )
            )
        )
    cases_tsv = ("\n".join(tsv_lines) + "\n").encode()

    pack_mcmeta = (
        json.dumps(
            {
                "pack": {
                    "pack_format": 48,
                    "description": "ATM 1.2.0 Sophisticated stable-optics BlueMap review gallery",
                }
            },
            indent=2,
            sort_keys=True,
        )
        + "\n"
    ).encode()

    load_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "scoreboard objectives add soph_gallery dummy",
        "forceload add 194 194 264 280",
    ]
    clear_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "fill 194 99 194 264 102 280 minecraft:air",
    ]
    build_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "function sophisticated_gallery:clear",
        "scoreboard players set #anchors soph_gallery 0",
    ]
    for anchor in anchors:
        support = Position(
            anchor.position.x, anchor.position.y - 1, anchor.position.z
        )
        build_lines.append(f"setblock {support.command()} minecraft:stone")
        build_lines.append(
            f"setblock {anchor.position.command()} {anchor.block_spec()}"
        )
        if anchor.nbt:
            build_lines.append(
                f"data merge block {anchor.position.command()} {snbt(anchor.nbt)}"
            )
        build_lines.append("scoreboard players add #anchors soph_gallery 1")
    # Reassert both halves after all neighbors exist. This is intentionally
    # limited to the paired-chest case and preserves the already-loaded BEs.
    for anchor in anchors:
        if anchor.case_id == "chest-double-topology":
            build_lines.append(
                f"setblock {anchor.position.command()} {anchor.block_spec()}"
            )
    build_lines.append(
        'tellraw @a [{"text":"Sophisticated gallery built: ","color":"green"},'
        '{"score":{"name":"#anchors","objective":"soph_gallery"}},'
        '{"text":" anchors.","color":"green"}]'
    )

    verify_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "scoreboard players set #failures soph_gallery 0",
        "scoreboard players set #checked soph_gallery 0",
    ]
    for anchor in anchors:
        pos = anchor.position.command()
        support = Position(
            anchor.position.x, anchor.position.y - 1, anchor.position.z
        )
        verify_lines.append(
            f"execute unless block {support.command()} minecraft:stone run scoreboard players add #failures soph_gallery 1"
        )
        verify_lines.append(
            f"execute unless block {pos} {anchor.block_spec()} run scoreboard players add #failures soph_gallery 1"
        )
        verify_lines.append(
            f'execute unless data block {pos} {{id:"{anchor.block_entity_id}"}} run scoreboard players add #failures soph_gallery 1'
        )
        if anchor.nbt:
            verify_lines.append(
                f"execute unless data block {pos} {snbt(anchor.nbt)} run scoreboard players add #failures soph_gallery 1"
            )
        verify_lines.append("scoreboard players add #checked soph_gallery 1")
    verify_lines.extend(
        (
            'execute if score #failures soph_gallery matches 0 run tellraw @a [{"text":"Sophisticated gallery verification passed: ","color":"green"},{"score":{"name":"#checked","objective":"soph_gallery"}},{"text":" anchors.","color":"green"}]',
            'execute unless score #failures soph_gallery matches 0 run tellraw @a [{"text":"Sophisticated gallery verification failures: ","color":"red"},{"score":{"name":"#failures","objective":"soph_gallery"}}]',
        )
    )

    pose_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "tp @s 228.5 116 186.5 0 30",
    ]
    release_lines = [
        "# Generated by gallery/generate.py; do not edit.",
        "forceload remove 194 194 264 280",
    ]
    load_tag = (
        json.dumps(
            {"replace": False, "values": ["sophisticated_gallery:load"]},
            indent=2,
            sort_keys=True,
        )
        + "\n"
    ).encode()

    files = {
        GALLERY_ROOT / "cases.json": cases_json,
        GALLERY_ROOT / "cases.tsv": cases_tsv,
        DATAPACK_ROOT / "pack.mcmeta": pack_mcmeta,
        FUNCTION_ROOT / "load.mcfunction": ("\n".join(load_lines) + "\n").encode(),
        FUNCTION_ROOT / "clear.mcfunction": ("\n".join(clear_lines) + "\n").encode(),
        FUNCTION_ROOT / "build.mcfunction": ("\n".join(build_lines) + "\n").encode(),
        FUNCTION_ROOT / "verify.mcfunction": ("\n".join(verify_lines) + "\n").encode(),
        FUNCTION_ROOT / "pose.mcfunction": ("\n".join(pose_lines) + "\n").encode(),
        FUNCTION_ROOT / "release.mcfunction": ("\n".join(release_lines) + "\n").encode(),
        DATAPACK_ROOT / "data/minecraft/tags/function/load.json": load_tag,
    }

    checksum_lines = []
    for path, content in sorted(
        files.items(), key=lambda item: item[0].relative_to(GALLERY_ROOT).as_posix()
    ):
        relative = path.relative_to(GALLERY_ROOT).as_posix()
        checksum_lines.append(f"{hashlib.sha256(content).hexdigest()}  {relative}")
    files[GALLERY_ROOT / "SHA256SUMS"] = (
        "\n".join(checksum_lines) + "\n"
    ).encode()
    return files


def write_files(files: dict[Path, bytes]) -> None:
    for path, content in files.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        temporary = path.with_name(f".{path.name}.tmp")
        temporary.write_bytes(content)
        os.replace(temporary, path)


def check_files(files: dict[Path, bytes]) -> bool:
    ok = True
    for path, expected in sorted(files.items()):
        if not path.exists():
            print(f"missing generated file: {path.relative_to(GALLERY_ROOT)}", file=sys.stderr)
            ok = False
            continue
        actual = path.read_bytes()
        if actual != expected:
            print(f"stale generated file: {path.relative_to(GALLERY_ROOT)}", file=sys.stderr)
            ok = False
    return ok


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--check",
        action="store_true",
        help="fail if any generated file is absent or stale",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    anchors = build_anchors()
    files = generated_files(anchors)
    if args.check:
        if not check_files(files):
            return 1
        print(
            f"gallery is current: {len(anchors)} anchors, "
            f"{len({anchor.case_id for anchor in anchors})} cases"
        )
        return 0
    write_files(files)
    print(
        f"generated {len(files)} files for {len(anchors)} anchors across "
        f"{len({anchor.case_id for anchor in anchors})} cases"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
