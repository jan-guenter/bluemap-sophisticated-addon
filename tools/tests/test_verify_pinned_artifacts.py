# SPDX-License-Identifier: MIT

from dataclasses import replace
import hashlib
import sys
import tempfile
import unittest
from pathlib import Path
import zipfile


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_pinned_artifacts import (  # noqa: E402
    ARTIFACTS,
    CATALOG_ARTIFACTS,
    PROFILES,
    CatalogIdentity,
    parse_resource_manifest,
    verify_archive,
    verify_exact_identity,
    verify_profile_set,
    verify_resource_manifest,
)


class VerifyPinnedArtifactsTest(unittest.TestCase):
    def test_exact_artifact_sets_are_closed(self):
        self.assertEqual({"core", "storage", "backpacks"}, set(ARTIFACTS))
        self.assertEqual(
            {
                "sophisticatedstorage",
                "sophisticatedbackpacks",
                "sophisticatedcore",
                "sophisticatedbackpackscreateintegration",
                "sophisticatedstoragecreateintegration",
                "sophisticatedstorageinmotion",
            },
            set(CATALOG_ARTIFACTS),
        )
        self.assertEqual(1_673_669, ARTIFACTS["core"].size)
        self.assertEqual(1_828_640, ARTIFACTS["storage"].size)
        self.assertEqual(1_144_235, ARTIFACTS["backpacks"].size)
        self.assertEqual(
            "required-static-render-input",
            CATALOG_ARTIFACTS["sophisticatedstorage"].verification_role,
        )
        self.assertEqual(
            "context-only-excluded-moving-entity",
            CATALOG_ARTIFACTS["sophisticatedstorageinmotion"].verification_role,
        )

    def test_profiles_and_resource_manifests_are_exact(self):
        resources = verify_profile_set(ROOT)
        self.assertEqual(set(PROFILES), set(resources))
        self.assertEqual(0, len(resources["sophisticatedcore"]))
        self.assertEqual(430, len(resources["sophisticatedstorage"]))
        self.assertEqual(31, len(resources["sophisticatedbackpacks"]))
        self.assertEqual(
            491_935,
            sum(row.size for row in resources["sophisticatedstorage"]),
        )
        self.assertEqual(
            72_558,
            sum(row.size for row in resources["sophisticatedbackpacks"]),
        )

    def test_identity_guard_checks_every_digest(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "fixture.jar"
            path.write_bytes(b"exact fixture")
            raw = path.read_bytes()
            identity = CatalogIdentity(
                "fixture",
                "1.0.0",
                "1.0.0.1",
                "2026-08-12T00:00:00+0000",
                path.name,
                len(raw),
                hashlib.sha1(raw).hexdigest(),
                hashlib.sha256(raw).hexdigest(),
                hashlib.sha512(raw).hexdigest(),
                1,
                2,
                "fixture",
            )
            verify_exact_identity(path, identity)
            path.write_bytes(b"wrong fixture")
            with self.assertRaisesRegex(ValueError, "changed"):
                verify_exact_identity(path, identity)
            wrong_filename = replace(identity, filename="different.jar")
            with self.assertRaisesRegex(ValueError, "filename changed"):
                verify_exact_identity(path, wrong_filename)

    def test_manifest_parser_rejects_noncanonical_rows(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "manifest.tsv"
            digest = hashlib.sha256(b"x").hexdigest()
            path.write_text(
                f"assets/test/z.txt\t1\t{digest}\n"
                f"assets/test/a.txt\t1\t{digest}\n",
                encoding="ascii",
            )
            with self.assertRaisesRegex(ValueError, "not sorted"):
                parse_resource_manifest(path)
            path.write_text(f"../escape\t1\t{digest}\n", encoding="ascii")
            with self.assertRaisesRegex(ValueError, "unsafe"):
                parse_resource_manifest(path)
            path.write_text(
                f"assets/test/a.txt\t1\t{digest}\n"
                f"assets/test/a.txt\t1\t{digest}\n",
                encoding="ascii",
            )
            with self.assertRaisesRegex(ValueError, "duplicate"):
                parse_resource_manifest(path)
            path.write_text(
                f"assets/test/a.txt\t01\t{digest}\n", encoding="ascii"
            )
            with self.assertRaisesRegex(ValueError, "noncanonical byte size"):
                parse_resource_manifest(path)

    def test_resource_rows_are_verified_against_archive_bytes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive_path = root / "fixture.jar"
            manifest_path = root / "manifest.tsv"
            resource = "assets/test/value.txt"
            raw = b"verified stable exterior resource"
            with zipfile.ZipFile(archive_path, "w") as archive:
                archive.writestr(resource, raw)
            manifest_path.write_text(
                f"{resource}\t{len(raw)}\t{hashlib.sha256(raw).hexdigest()}\n",
                encoding="ascii",
            )
            with zipfile.ZipFile(archive_path) as archive:
                verify_resource_manifest(archive, manifest_path, 1)
                manifest_path.write_text(
                    f"{resource}\t{len(raw)}\t{'0' * 64}\n",
                    encoding="ascii",
                )
                with self.assertRaisesRegex(ValueError, "SHA-256.*changed"):
                    verify_resource_manifest(archive, manifest_path, 1)

    def test_archive_guards_metadata_manifest_and_java_21(self):
        identity = ARTIFACTS["core"]
        metadata = f'''modLoader="javafml"
loaderVersion="[4,)"
license="All Rights Reserved"
[[mods]]
modId="{identity.mod_id}"
version="{identity.metadata_version}"
[[dependencies.{identity.mod_id}]]
modId="neoforge"
type="required"
versionRange="[21.1.0,)"
ordering="NONE"
side="BOTH"
[[dependencies.{identity.mod_id}]]
modId="minecraft"
type="required"
versionRange="[1.21.1,1.21.2)"
ordering="NONE"
side="BOTH"
'''.encode("utf-8")
        manifest = (
            "Manifest-Version: 1.0\r\n"
            f"Implementation-Version: {identity.implementation_version}\r\n"
            f"Implementation-Timestamp: {identity.implementation_timestamp}\r\n\r\n"
        ).encode("ascii")
        with tempfile.TemporaryDirectory() as temporary:
            archive_path = Path(temporary) / identity.filename
            with zipfile.ZipFile(archive_path, "w") as archive:
                archive.writestr("META-INF/neoforge.mods.toml", metadata)
                archive.writestr("META-INF/MANIFEST.MF", manifest)
                archive.writestr(
                    identity.class_path,
                    b"\xca\xfe\xba\xbe\x00\x00\x00\x41",
                )
            verify_archive(archive_path, identity, (), Path("unused.tsv"))
            with zipfile.ZipFile(archive_path, "a") as archive:
                archive.writestr(identity.class_path, b"not-java-21")
            with self.assertRaisesRegex(ValueError, "duplicate ZIP entry"):
                verify_archive(archive_path, identity, (), Path("unused.tsv"))


if __name__ == "__main__":
    unittest.main()
