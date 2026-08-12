#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Verify the exact All the Mons 1.2.0 Sophisticated renderer inputs."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import struct
import sys
import tomllib
from typing import Mapping
import zipfile


PROFILE_ROOT = Path(
    "src/main/resources/bluemap-sophisticated/profiles"
)
CATALOG_PATH = PROFILE_ROOT / "exact-artifacts.json"
CATALOG_SHA256 = "4ce1359f35eb567f931c1b6d20f348af74b6fd75c64787386acef931bee2bf1a"
LOWER_SHA256 = re.compile(r"[0-9a-f]{64}")


@dataclass(frozen=True)
class CatalogIdentity:
    mod_id: str
    metadata_version: str
    implementation_version: str
    implementation_timestamp: str
    filename: str
    size: int
    sha1: str
    sha256: str
    sha512: str
    project_id: int
    file_id: int
    verification_role: str


@dataclass(frozen=True)
class ArtifactIdentity(CatalogIdentity):
    class_path: str
    required_dependencies: Mapping[str, str]


@dataclass(frozen=True)
class ResourceRow:
    path: str
    size: int
    sha256: str


@dataclass(frozen=True)
class ProfileSpec:
    relative_path: Path
    profile_sha256: str
    manifest_name: str | None
    manifest_rows: int
    manifest_bytes: int
    manifest_sha256: str | None
    block_count: int


CATALOG_ARTIFACTS = {
    "sophisticatedstorage": CatalogIdentity(
        "sophisticatedstorage",
        "1.5.83",
        "1.21.1-1.5.83.2017",
        "2026-07-24T23:33:25+0000",
        "sophisticatedstorage-1.21.1-1.5.83.2017.jar",
        1_828_640,
        "b36fa724fe925e715d8b13929d5789125e97e81b",
        "354f62ef885b3219fb0787d211582d7ea733800ff31787cc85b9af68d260b600",
        "af16494408c31e87a94e1d517c684b4b0c0fdb7ceaf6332ac1c281e18ee0cd9e0e87605f9a991b21ad737dff8eb83d5e0ac811983d881da5e56f989d344d46da",
        619320,
        8503122,
        "required-static-render-input",
    ),
    "sophisticatedbackpacks": CatalogIdentity(
        "sophisticatedbackpacks",
        "3.25.73",
        "1.21.1-3.25.73.2027",
        "2026-08-03T14:11:44+0000",
        "sophisticatedbackpacks-1.21.1-3.25.73.2027.jar",
        1_144_235,
        "e8baceab12d01ff170e7dcf3ab2079206d1407fd",
        "ded30f9269a92cc295ab0a735a86770ca097c30198b8f3f2288ecaac6542b93e",
        "fe92bd732f19d71818ba339cc3799645181c9dadbe669f99284125b6c96f70ed1f96bc4b91dfcf9220cc01a19c1dbfdcf95650bbd8e8166133c40d2660b90805",
        422301,
        8569661,
        "required-static-render-input",
    ),
    "sophisticatedcore": CatalogIdentity(
        "sophisticatedcore",
        "1.4.80",
        "1.21.1-1.4.80.2194",
        "2026-07-24T23:16:40+0000",
        "sophisticatedcore-1.21.1-1.4.80.2194.jar",
        1_673_669,
        "bef1d5186feaed80b11bd1e6f2dc880e8bec0449",
        "58a35e74642de9a7ffd39604f06903df39c166d332551c5770ca2e21685defc0",
        "277d93609e53a70e693f3b492e37b537534e06be3f24313f3930c1bca4d6c556c2cc54e6a2f4d03622b9acbbc4ac29766df80941a93dae245890c5ecf52851ea",
        618298,
        8503041,
        "required-static-render-input",
    ),
    "sophisticatedbackpackscreateintegration": CatalogIdentity(
        "sophisticatedbackpackscreateintegration",
        "0.1.8",
        "1.21.1-0.1.8.134",
        "2026-07-09T08:10:58+0000",
        "sophisticatedbackpackscreateintegration-1.21.1-0.1.8.134.jar",
        82_627,
        "d574c93098fe1189bf21eccaaff97d7a58798b8a",
        "f396364bfb146d05c1c44d21431f357b68742699cf49e6f8815751db129dc068",
        "953ecfe402bb2b622ba649d9e0461a07864b90e1ef107f1f97d069a7c0e415c682835d8627e5cd6887925ef7a99c4939ae1d202872b23f93f200db3727cc760b",
        1238567,
        8398818,
        "context-only-excluded-moving-contraption",
    ),
    "sophisticatedstoragecreateintegration": CatalogIdentity(
        "sophisticatedstoragecreateintegration",
        "0.1.21",
        "1.21.1-0.1.21.209",
        "2026-07-24T23:41:49+0000",
        "sophisticatedstoragecreateintegration-1.21.1-0.1.21.209.jar",
        134_163,
        "d8f72f22459750496008a7c2a39dc865152d075c",
        "0b8b13cd120d45525c4df8b465adf5d381b1ed4518b72a005a3b426c6c980d9b",
        "979bb431e2db4df5836ff21e8c0f6b78c372afcabdcdb795e741d7c7487764af5803f6ca5f80944d987986b906cda9aaa884b8a6db00de092732752e7097f2bb",
        1226755,
        8503147,
        "context-only-excluded-moving-contraption",
    ),
    "sophisticatedstorageinmotion": CatalogIdentity(
        "sophisticatedstorageinmotion",
        "0.10.33",
        "1.21.1-0.10.33.324",
        "2026-07-14T23:00:36+0000",
        "sophisticatedstorageinmotion-1.21.1-0.10.33.324.jar",
        409_586,
        "3ce07b64a52db90416bfe598b95d31c6bc368bc2",
        "4162a128bced3ff9455a4926627b9d2d80029ddd710dfec6659f793322632290",
        "88bf503eba0eb328085b34fb03484c6ceddb3c768136b26fb71429a39ffa08b62325c96e316895878daeeb84df7bebb1a8f8bc3a83d66e73d226746454394cab",
        1166930,
        8434254,
        "context-only-excluded-moving-entity",
    ),
}


ARTIFACTS = {
    "core": ArtifactIdentity(
        **CATALOG_ARTIFACTS["sophisticatedcore"].__dict__,
        class_path="net/p3pp3rf1y/sophisticatedcore/SophisticatedCore.class",
        required_dependencies={
            "minecraft": "[1.21.1,1.21.2)",
            "neoforge": "[21.1.0,)",
        },
    ),
    "storage": ArtifactIdentity(
        **CATALOG_ARTIFACTS["sophisticatedstorage"].__dict__,
        class_path="net/p3pp3rf1y/sophisticatedstorage/SophisticatedStorage.class",
        required_dependencies={
            "minecraft": "[1.21.1,1.21.2)",
            "neoforge": "[21.1.0,)",
            "sophisticatedcore": "[1.4.80,)",
        },
    ),
    "backpacks": ArtifactIdentity(
        **CATALOG_ARTIFACTS["sophisticatedbackpacks"].__dict__,
        class_path="net/p3pp3rf1y/sophisticatedbackpacks/SophisticatedBackpacks.class",
        required_dependencies={
            "minecraft": "[1.21.1,1.21.2)",
            "neoforge": "[21.1.0,)",
            "sophisticatedcore": "[1.4.80,)",
        },
    ),
}


PROFILES = {
    "sophisticatedcore": ProfileSpec(
        Path("sophisticatedcore/1.4.80/profile.json"),
        "a08a58e05642c07553aa7abcd22496b543f31058075b035054b1e69af4a0ae36",
        None,
        0,
        0,
        None,
        0,
    ),
    "sophisticatedstorage": ProfileSpec(
        Path("sophisticatedstorage/1.5.83/profile.json"),
        "a88592e977809129317061d0be80f1befc67a9d9380bc8a706b24e28a60e9428",
        "required-resources.tsv",
        430,
        491_935,
        "95be6bc7e6d27a555823bade8d65f7757c4576c58b0634bcc76866232dfbd627",
        59,
    ),
    "sophisticatedbackpacks": ProfileSpec(
        Path("sophisticatedbackpacks/3.25.73/profile.json"),
        "2686911453d38fa5736596536b787ddd8f4d78c3c1cd5b9e3d5c73a825f6b849",
        "required-resources.tsv",
        31,
        72_558,
        "262348d4df50d183b0fa10f0472d3dfbfc4cd2cba187c92a2670ae8172462cee",
        6,
    ),
}


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_hash(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise ValueError(f"{label} changed: got {actual}, expected {expected}")


def verify_exact_identity(path: Path, identity: CatalogIdentity) -> None:
    if not path.is_file():
        raise ValueError(f"{identity.mod_id} artifact is not a regular file: {path}")
    if path.name != identity.filename:
        raise ValueError(
            f"{identity.mod_id} filename changed: got {path.name}, "
            f"expected {identity.filename}"
        )
    if path.stat().st_size != identity.size:
        raise ValueError(
            f"{identity.mod_id} size changed: got {path.stat().st_size}, "
            f"expected {identity.size}"
        )
    verify_hash(f"{identity.mod_id} SHA-1", digest(path, "sha1"), identity.sha1)
    verify_hash(
        f"{identity.mod_id} SHA-256", digest(path, "sha256"), identity.sha256
    )
    verify_hash(
        f"{identity.mod_id} SHA-512", digest(path, "sha512"), identity.sha512
    )


def _is_safe_resource_path(value: str) -> bool:
    if not value or "\\" in value or value.startswith("/"):
        return False
    path = PurePosixPath(value)
    return not path.is_absolute() and all(part not in {"", ".", ".."} for part in path.parts)


def parse_resource_manifest(path: Path) -> tuple[ResourceRow, ...]:
    raw = path.read_bytes()
    if raw and not raw.endswith(b"\n"):
        raise ValueError(f"{path} is not LF-terminated")
    try:
        text = raw.decode("ascii")
    except UnicodeDecodeError as error:
        raise ValueError(f"{path} is not ASCII") from error
    rows: list[ResourceRow] = []
    for line_number, line in enumerate(text.splitlines(), start=1):
        parts = line.split("\t")
        if len(parts) != 3:
            raise ValueError(f"{path}:{line_number} is not a canonical three-field row")
        resource, size_text, sha256 = parts
        if not _is_safe_resource_path(resource):
            raise ValueError(f"{path}:{line_number} has unsafe resource path {resource!r}")
        if not size_text.isdecimal() or str(int(size_text)) != size_text:
            raise ValueError(f"{path}:{line_number} has noncanonical byte size")
        if LOWER_SHA256.fullmatch(sha256) is None:
            raise ValueError(f"{path}:{line_number} has noncanonical SHA-256")
        rows.append(ResourceRow(resource, int(size_text), sha256))
    names = [row.path for row in rows]
    if names != sorted(names):
        raise ValueError(f"{path} resource paths are not sorted")
    if len(names) != len(set(names)):
        raise ValueError(f"{path} contains duplicate resource paths")
    return tuple(rows)


def verify_resource_manifest(
    archive: zipfile.ZipFile,
    manifest_path: Path,
    expected_rows: int,
) -> tuple[ResourceRow, ...]:
    rows = parse_resource_manifest(manifest_path)
    if len(rows) != expected_rows:
        raise ValueError(
            f"{manifest_path} row count changed: got {len(rows)}, expected {expected_rows}"
        )
    archive_names = set(archive.namelist())
    for row in rows:
        if row.path not in archive_names:
            raise ValueError(f"artifact is missing required resource {row.path}")
        raw = archive.read(row.path)
        if len(raw) != row.size:
            raise ValueError(
                f"{row.path} byte size changed: got {len(raw)}, expected {row.size}"
            )
        verify_hash(
            f"{row.path} SHA-256", hashlib.sha256(raw).hexdigest(), row.sha256
        )
    return rows


def _catalog_record(identity: CatalogIdentity) -> dict[str, object]:
    return {
        "modId": identity.mod_id,
        "metadataVersion": identity.metadata_version,
        "implementationVersion": identity.implementation_version,
        "implementationTimestamp": identity.implementation_timestamp,
        "filename": identity.filename,
        "sizeBytes": identity.size,
        "sha1": identity.sha1,
        "sha256": identity.sha256,
        "sha512": identity.sha512,
        "license": "All Rights Reserved",
        "curseForgeProjectId": identity.project_id,
        "curseForgeFileId": identity.file_id,
        "verificationRole": identity.verification_role,
    }


def verify_profile_set(project: Path) -> dict[str, tuple[ResourceRow, ...]]:
    root = project / PROFILE_ROOT
    catalog_path = project / CATALOG_PATH
    verify_hash("exact-artifacts catalog SHA-256", digest(catalog_path, "sha256"), CATALOG_SHA256)
    catalog = json.loads(catalog_path.read_text(encoding="utf-8"))
    if catalog.get("schemaVersion") != 1:
        raise ValueError("exact-artifacts catalog schema changed")
    baseline = catalog.get("baseline")
    if not isinstance(baseline, dict) or {
        "packVersion": baseline.get("packVersion"),
        "packRepositoryCommit": baseline.get("packRepositoryCommit"),
        "minecraft": baseline.get("minecraft"),
        "neoforge": baseline.get("neoforge"),
    } != {
        "packVersion": "1.2.0",
        "packRepositoryCommit": "c7bb230f21d14d26859d0b92548f089b3a493ad9",
        "minecraft": "1.21.1",
        "neoforge": "21.1.248",
    }:
        raise ValueError("exact-artifacts baseline changed")
    if catalog.get("requiredForStaticRendering") != [
        "sophisticatedstorage",
        "sophisticatedbackpacks",
        "sophisticatedcore",
    ]:
        raise ValueError("required static-render artifact set changed")
    records = catalog.get("artifacts")
    if not isinstance(records, list):
        raise ValueError("exact-artifacts catalog has no artifact list")
    indexed = {
        record.get("modId"): record
        for record in records
        if isinstance(record, dict) and isinstance(record.get("modId"), str)
    }
    if len(indexed) != len(records) or set(indexed) != set(CATALOG_ARTIFACTS):
        raise ValueError("exact-artifacts catalog artifact set changed")
    for mod_id, identity in CATALOG_ARTIFACTS.items():
        record = indexed[mod_id]
        expected = _catalog_record(identity)
        actual = {field: record.get(field) for field in expected}
        if actual != expected:
            raise ValueError(f"exact-artifacts record changed for {mod_id}")
        source = record.get("sourceCorrelation")
        if not isinstance(source, dict) or source.get("status") != "unresolved":
            raise ValueError(f"source correlation status changed for {mod_id}")

    resources: dict[str, tuple[ResourceRow, ...]] = {}
    for mod_id, spec in PROFILES.items():
        profile_path = root / spec.relative_path
        verify_hash(
            f"{mod_id} profile SHA-256",
            digest(profile_path, "sha256"),
            spec.profile_sha256,
        )
        profile = json.loads(profile_path.read_text(encoding="utf-8"))
        identity = CATALOG_ARTIFACTS[mod_id]
        expected_identity = {
            "schemaVersion": 1,
            "profileId": mod_id,
            "modId": mod_id,
            "version": identity.metadata_version,
            "implementationVersion": identity.implementation_version,
            "artifact": identity.filename,
            "sizeBytes": identity.size,
            "sha1": identity.sha1,
            "sha256": identity.sha256,
            "sha512": identity.sha512,
            "minecraft": "1.21.1",
            "neoforge": "21.1.248",
        }
        if {field: profile.get(field) for field in expected_identity} != expected_identity:
            raise ValueError(f"{mod_id} profile identity changed")
        coverage = profile.get("coverage")
        if not isinstance(coverage, dict) or coverage.get("blockCount") != spec.block_count:
            raise ValueError(f"{mod_id} profile block count changed")
        blocks = coverage.get("supportedBlocks", [])
        if len(blocks) != spec.block_count or len(blocks) != len(set(blocks)):
            raise ValueError(f"{mod_id} profile supported-block set is malformed")
        closure = profile.get("resourceClosure")
        if not isinstance(closure, dict) or closure.get("pathCount") != spec.manifest_rows:
            raise ValueError(f"{mod_id} profile resource count changed")
        if spec.manifest_name is None:
            resources[mod_id] = ()
            continue
        manifest_path = profile_path.with_name(spec.manifest_name)
        verify_hash(
            f"{mod_id} resource manifest SHA-256",
            digest(manifest_path, "sha256"),
            spec.manifest_sha256 or "",
        )
        rows = parse_resource_manifest(manifest_path)
        if len(rows) != spec.manifest_rows:
            raise ValueError(f"{mod_id} resource-manifest row count changed")
        if sum(row.size for row in rows) != spec.manifest_bytes:
            raise ValueError(f"{mod_id} resource-manifest byte total changed")
        resources[mod_id] = rows
    return resources


def dependency_by_id(
    metadata: dict[str, object], owner_mod_id: str, dependency_mod_id: str
) -> dict[str, object]:
    dependencies = metadata.get("dependencies")
    if not isinstance(dependencies, dict):
        raise ValueError("NeoForge metadata has no dependency table")
    owner_dependencies = dependencies.get(owner_mod_id)
    if not isinstance(owner_dependencies, list):
        raise ValueError(f"NeoForge metadata has no {owner_mod_id} dependency list")
    matches = [
        dependency
        for dependency in owner_dependencies
        if isinstance(dependency, dict)
        and dependency.get("modId") == dependency_mod_id
    ]
    if len(matches) != 1:
        raise ValueError(
            f"expected one {dependency_mod_id} dependency for {owner_mod_id}, "
            f"got {len(matches)}"
        )
    return matches[0]


def verify_metadata(raw: bytes, identity: ArtifactIdentity) -> None:
    metadata = tomllib.loads(raw.decode("utf-8"))
    if metadata.get("license") != "All Rights Reserved":
        raise ValueError(f"{identity.mod_id} metadata license changed")
    mods = metadata.get("mods")
    if not isinstance(mods, list) or len(mods) != 1 or not isinstance(mods[0], dict):
        raise ValueError(f"{identity.mod_id} metadata must declare exactly one mod")
    mod = mods[0]
    if mod.get("modId") != identity.mod_id or mod.get("version") != identity.metadata_version:
        raise ValueError(f"{identity.mod_id} metadata identity changed")
    for dependency_mod_id, version_range in identity.required_dependencies.items():
        dependency = dependency_by_id(metadata, identity.mod_id, dependency_mod_id)
        expected = {
            "type": "required",
            "versionRange": version_range,
            "side": "BOTH",
        }
        if {field: dependency.get(field) for field in expected} != expected:
            raise ValueError(
                f"{identity.mod_id} {dependency_mod_id} dependency contract changed"
            )


def parse_jar_manifest(raw: bytes) -> dict[str, str]:
    text = raw.decode("utf-8").replace("\r\n", "\n")
    unfolded: list[str] = []
    for line in text.split("\n"):
        if line.startswith(" "):
            if not unfolded:
                raise ValueError("JAR manifest starts with a continuation line")
            unfolded[-1] += line[1:]
        elif line:
            unfolded.append(line)
    values: dict[str, str] = {}
    for line in unfolded:
        if ": " not in line:
            raise ValueError("JAR manifest contains a malformed header")
        key, value = line.split(": ", 1)
        if key in values:
            raise ValueError(f"JAR manifest repeats {key}")
        values[key] = value
    return values


def verify_archive(
    jar: Path,
    identity: ArtifactIdentity,
    resource_rows: tuple[ResourceRow, ...],
    profile_path: Path,
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError(f"{identity.mod_id} contains duplicate ZIP entry names")
        verify_metadata(archive.read("META-INF/neoforge.mods.toml"), identity)
        manifest = parse_jar_manifest(archive.read("META-INF/MANIFEST.MF"))
        if manifest.get("Implementation-Version") != identity.implementation_version:
            raise ValueError(f"{identity.mod_id} implementation version changed")
        if manifest.get("Implementation-Timestamp") != identity.implementation_timestamp:
            raise ValueError(f"{identity.mod_id} implementation timestamp changed")
        header = archive.read(identity.class_path)[:8]
        if len(header) != 8 or header[:4] != b"\xca\xfe\xba\xbe":
            raise ValueError(f"{identity.class_path} has an invalid class-file header")
        class_major = struct.unpack(">H", header[6:8])[0]
        if class_major != 65:
            raise ValueError(
                f"{identity.class_path} major is {class_major}, expected Java 21 (65)"
            )
        if resource_rows:
            verify_resource_manifest(archive, profile_path, len(resource_rows))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--core", required=True, type=Path)
    parser.add_argument("--storage", required=True, type=Path)
    parser.add_argument("--backpacks", required=True, type=Path)
    args = parser.parse_args()

    project = Path(__file__).resolve().parents[1]
    profile_resources = verify_profile_set(project)
    requested = {
        "core": args.core,
        "storage": args.storage,
        "backpacks": args.backpacks,
    }
    for key, jar in requested.items():
        identity = ARTIFACTS[key]
        verify_exact_identity(jar, identity)
        spec = PROFILES[identity.mod_id]
        manifest_path = project / PROFILE_ROOT / spec.relative_path
        if spec.manifest_name is not None:
            manifest_path = manifest_path.with_name(spec.manifest_name)
        verify_archive(jar, identity, profile_resources[identity.mod_id], manifest_path)

    print(
        "Verified exact All the Mons 1.2.0 Sophisticated Core, Storage and "
        "Backpacks artifacts, metadata, Java 21 classes, 461 stable exterior "
        "resources and 65 supported world blocks."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (
        json.JSONDecodeError,
        KeyError,
        OSError,
        tomllib.TOMLDecodeError,
        UnicodeDecodeError,
        ValueError,
        zipfile.BadZipFile,
    ) as error:
        print(f"verification failed: {error}", file=sys.stderr)
        sys.exit(1)
