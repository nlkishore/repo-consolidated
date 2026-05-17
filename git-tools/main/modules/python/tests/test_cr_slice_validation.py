"""Tests for cr_slice_validation."""

from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path

import pytest

from cr_slice_validation.diff_parser import parse_unified_diff
from cr_slice_validation.manifest import load_manifest, validate_manifest
from cr_slice_validation.reconciler import reconcile


def test_parse_unified_diff_added_lines():
    diff = """diff --git a/foo.txt b/foo.txt
index 111..222 100644
--- a/foo.txt
+++ b/foo.txt
@@ -1,2 +1,4 @@
 line1
+added1
+added2
 line2
"""
    ranges = parse_unified_diff(diff)
    assert "foo.txt" in ranges
    assert ranges["foo.txt"] == [(2, 3)]


def test_manifest_validation_ok():
    with tempfile.TemporaryDirectory() as tmp:
        path = Path(tmp) / "cr-manifest.yaml"
        path.write_text(
            """
cr_id: CR-102
baseline_ref: v1.0.0
expected_ref: integration/cr-102
slice_ref: release-cr102
pull_requests:
  - id: 1
""",
            encoding="utf-8",
        )
        m = load_manifest(path)
        r = validate_manifest(m)
        assert r.ok
        assert r.manifest.cr_id == "CR-102"


def test_manifest_validation_missing_expected_source():
    from cr_slice_validation.manifest import CrManifest

    m = CrManifest(cr_id="CR-1", baseline_ref="main")
    r = validate_manifest(m)
    assert not r.ok


def _git(repo: Path, *args: str) -> None:
    subprocess.run(["git", *args], cwd=str(repo), check=True, capture_output=True)


def _init_repo_with_slice_scenario() -> Path:
    tmp = Path(tempfile.mkdtemp())
    _git(tmp, "init")
    _git(tmp, "config", "user.email", "test@example.com")
    _git(tmp, "config", "user.name", "Test")

    (tmp / "README.md").write_text("base\n", encoding="utf-8")
    _git(tmp, "add", "README.md")
    _git(tmp, "commit", "-m", "baseline")
    _git(tmp, "tag", "v1.0.0-baseline")

    _git(tmp, "checkout", "-b", "integration/cr-102-all-prs")
    (tmp / "feature.txt").write_text("expected\n", encoding="utf-8")
    _git(tmp, "add", "feature.txt")
    _git(tmp, "commit", "-m", "expected change")

    _git(tmp, "checkout", "v1.0.0-baseline")
    _git(tmp, "checkout", "-b", "release-cr102")
    # slice missing feature.txt -> FAIL

    return tmp


def test_reconcile_pass_when_slice_matches_expected():
    repo = _init_repo_with_slice_scenario()
    # Fix slice to include expected file
    _git(repo, "checkout", "release-cr102")
    (repo / "feature.txt").write_text("expected\n", encoding="utf-8")
    _git(repo, "add", "feature.txt")
    _git(repo, "commit", "-m", "slice with feature")

    from cr_slice_validation.manifest import CrManifest

    manifest = CrManifest(
        cr_id="CR-102",
        baseline_ref="v1.0.0-baseline",
        expected_ref="integration/cr-102-all-prs",
        slice_ref="release-cr102",
    )
    result = reconcile(manifest, repo, slice_ref="release-cr102")
    assert result.ok
    assert result.status == "PASS"
    assert not result.missing_files


def test_reconcile_fail_when_file_missing_on_slice():
    repo = _init_repo_with_slice_scenario()
    from cr_slice_validation.manifest import CrManifest

    manifest = CrManifest(
        cr_id="CR-102",
        baseline_ref="v1.0.0-baseline",
        expected_ref="integration/cr-102-all-prs",
        slice_ref="release-cr102",
    )
    result = reconcile(manifest, repo, slice_ref="release-cr102")
    assert not result.ok
    assert "feature.txt" in result.missing_files
