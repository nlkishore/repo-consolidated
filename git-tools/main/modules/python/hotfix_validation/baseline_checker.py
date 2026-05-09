"""Compare fileLists baseline to release-build manifest (design doc §6)."""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional

import yaml

from hotfix_validation.file_lists_validator import FileListsValidator


@dataclass
class BaselineCheckResult:
    ok: bool
    errors: List[str]


def load_manifest(path: Path) -> Dict[str, Any]:
    text = path.read_text(encoding="utf-8")
    data = json.loads(text)
    if not isinstance(data, dict):
        raise ValueError("manifest root must be an object")
    return data


def normalize_sha(sha: str) -> str:
    s = sha.strip().lower()
    if len(s) >= 40:
        return s[:40]
    return s


def check_baseline_against_manifest(
    file_lists_path: Path,
    manifest_path: Path,
    require_artifact: bool = False,
) -> BaselineCheckResult:
    """
    Manifest keys (recommended by design):
      - status: must be "success" if present
      - commit_sha / release_commit: must match baseline.release_commit when set
      - full_build_id / build_id: must match baseline.full_build_id when set
      - artifact_url: optional; if require_artifact, must be non-empty
    """
    errors: List[str] = []

    v = FileListsValidator().validate_file(file_lists_path)
    if not v.ok:
        errors.extend(v.errors)
        return BaselineCheckResult(ok=False, errors=errors)

    raw = yaml.safe_load(file_lists_path.read_text(encoding="utf-8"))
    baseline = raw.get("baseline") if isinstance(raw, dict) else None
    if not isinstance(baseline, dict):
        errors.append(
            "Add a 'baseline' section with release_commit and/or full_build_id "
            "for release build verification."
        )
        return BaselineCheckResult(ok=False, errors=errors)

    try:
        manifest = load_manifest(manifest_path)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        return BaselineCheckResult(ok=False, errors=[f"manifest: {exc}"])

    status = manifest.get("status")
    if status is not None and str(status).lower() != "success":
        errors.append(f"manifest status is not success: {status!r}")

    rel_commit = baseline.get("release_commit")
    man_commit = manifest.get("commit_sha") or manifest.get("release_commit")
    if rel_commit and man_commit:
        if normalize_sha(str(rel_commit)) != normalize_sha(str(man_commit)):
            errors.append(
                f"baseline.release_commit {rel_commit!r} does not match "
                f"manifest commit {man_commit!r}"
            )

    build_id = baseline.get("full_build_id")
    man_build = manifest.get("full_build_id") or manifest.get("build_id")
    if build_id and man_build and str(build_id) != str(man_build):
        errors.append(
            f"baseline.full_build_id {build_id!r} does not match manifest {man_build!r}"
        )

    if require_artifact:
        url = manifest.get("artifact_url") or manifest.get("artifactUrl")
        if not url:
            errors.append("manifest missing artifact_url for required artifact check")

    return BaselineCheckResult(ok=len(errors) == 0, errors=errors)


def main_argv(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    req_art = "--require-artifact" in argv
    argv = [a for a in argv if a != "--require-artifact"]
    if len(argv) < 2:
        print(
            "usage: validate-baseline <fileLists.txt> <release-build-manifest.json>",
            file=sys.stderr,
        )
        return 2

    fl = Path(argv[0]).resolve()
    man = Path(argv[1]).resolve()
    result = check_baseline_against_manifest(fl, man, require_artifact=req_art)
    if result.ok:
        print("OK: baseline matches release manifest.")
        return 0
    for e in result.errors:
        print(e, file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main_argv())
