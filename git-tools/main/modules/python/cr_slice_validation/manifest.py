"""Load and validate cr-manifest.yaml."""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import yaml

_CR_ID_RE = re.compile(r"^CR-[A-Za-z0-9._-]+$")


@dataclass
class CrManifest:
    cr_id: str
    baseline_ref: str
    title: str = ""
    expected_ref: str = ""
    slice_ref: str = ""
    pull_requests: list[int] = field(default_factory=list)
    pr_branches: list[str] = field(default_factory=list)
    optional_commits: list[str] = field(default_factory=list)
    exclude_paths: list[str] = field(default_factory=list)
    notes: str = ""
    source_path: Path | None = None

    @property
    def has_expected_source(self) -> bool:
        return bool(
            self.expected_ref.strip()
            or self.pr_branches
            or self.pull_requests
        )


@dataclass
class ManifestValidationResult:
    ok: bool
    errors: list[str] = field(default_factory=list)
    manifest: CrManifest | None = None


def load_manifest(path: Path) -> CrManifest:
    raw = yaml.safe_load(path.read_text(encoding="utf-8"))
    if not isinstance(raw, dict):
        raise ValueError(f"Manifest must be a YAML mapping: {path}")
    return _parse_manifest_dict(raw, path)


def validate_manifest(manifest: CrManifest) -> ManifestValidationResult:
    errors: list[str] = []

    if not manifest.cr_id:
        errors.append("cr_id is required")
    elif not _CR_ID_RE.match(manifest.cr_id):
        errors.append(
            f"cr_id '{manifest.cr_id}' must match pattern CR-<alphanumeric>"
        )

    if not manifest.baseline_ref.strip():
        errors.append("baseline_ref is required")

    if not manifest.has_expected_source:
        errors.append(
            "At least one of expected_ref, pr_branches, or pull_requests is required"
        )

    for pr in manifest.pull_requests:
        if pr <= 0:
            errors.append(f"Invalid pull request id: {pr}")

    if errors:
        return ManifestValidationResult(ok=False, errors=errors)
    return ManifestValidationResult(ok=True, manifest=manifest)


def _parse_manifest_dict(raw: dict[str, Any], path: Path) -> CrManifest:
    pr_ids: list[int] = []
    for item in raw.get("pull_requests") or []:
        if isinstance(item, int):
            pr_ids.append(item)
        elif isinstance(item, dict) and "id" in item:
            pr_ids.append(int(item["id"]))
        else:
            raise ValueError(f"Invalid pull_requests entry: {item!r}")

    pr_branches = [str(b) for b in (raw.get("pr_branches") or [])]
    optional_commits = [str(c) for c in (raw.get("optional_commits") or [])]
    exclude_paths = [str(p) for p in (raw.get("exclude_paths") or [])]

    return CrManifest(
        cr_id=str(raw.get("cr_id") or "").strip(),
        title=str(raw.get("title") or "").strip(),
        baseline_ref=str(raw.get("baseline_ref") or "").strip(),
        expected_ref=str(raw.get("expected_ref") or "").strip(),
        slice_ref=str(raw.get("slice_ref") or "").strip(),
        pull_requests=pr_ids,
        pr_branches=pr_branches,
        optional_commits=optional_commits,
        exclude_paths=exclude_paths,
        notes=str(raw.get("notes") or "").strip(),
        source_path=path,
    )
