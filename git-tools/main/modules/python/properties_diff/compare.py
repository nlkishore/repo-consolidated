"""Compare baseline (production) vs candidate (UAT) property sets."""

from __future__ import annotations

import fnmatch
import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Pattern, Sequence, Tuple

from properties_diff.properties_parser import ParseResult, load_properties


@dataclass
class KeyChange:
    key: str
    baseline_value: str
    candidate_value: str


@dataclass
class FileDiffResult:
    relative_path: str
    baseline_path: str
    candidate_path: str
    added_keys: List[str] = field(default_factory=list)
    removed_keys: List[str] = field(default_factory=list)
    changed_keys: List[KeyChange] = field(default_factory=list)
    parse_warnings: List[str] = field(default_factory=list)

    @property
    def has_rollout_risk(self) -> bool:
        return bool(self.added_keys or self.changed_keys)

    @property
    def has_any_diff(self) -> bool:
        return bool(self.added_keys or self.removed_keys or self.changed_keys)


@dataclass
class TreeDiffResult:
    baseline_root: str
    candidate_root: str
    files: List[FileDiffResult] = field(default_factory=list)
    baseline_only_files: List[str] = field(default_factory=list)
    candidate_only_files: List[str] = field(default_factory=list)

    @property
    def files_with_rollout_risk(self) -> List[FileDiffResult]:
        return [f for f in self.files if f.has_rollout_risk]

    @property
    def has_rollout_risk(self) -> bool:
        return bool(self.candidate_only_files) or any(
            f.has_rollout_risk for f in self.files
        )


def compare_properties_maps(
    baseline: Dict[str, str],
    candidate: Dict[str, str],
    *,
    include_removed: bool = False,
) -> Tuple[List[str], List[str], List[KeyChange]]:
    baseline_keys = set(baseline)
    candidate_keys = set(candidate)

    added = sorted(candidate_keys - baseline_keys)
    removed = sorted(baseline_keys - candidate_keys) if include_removed else []
    changed: List[KeyChange] = []
    for key in sorted(baseline_keys & candidate_keys):
        b_val = baseline[key]
        c_val = candidate[key]
        if b_val != c_val:
            changed.append(
                KeyChange(key=key, baseline_value=b_val, candidate_value=c_val)
            )
    return added, removed, changed


def compare_files(
    baseline_path: Path,
    candidate_path: Path,
    *,
    encoding: str = "utf-8",
    include_removed: bool = False,
    relative_path: Optional[str] = None,
) -> FileDiffResult:
    rel = relative_path or candidate_path.name
    baseline_parse = load_properties(baseline_path, encoding=encoding)
    candidate_parse = load_properties(candidate_path, encoding=encoding)

    added, removed, changed = compare_properties_maps(
        dict(baseline_parse.properties),
        dict(candidate_parse.properties),
        include_removed=include_removed,
    )

    warnings = list(baseline_parse.warnings) + list(candidate_parse.warnings)
    return FileDiffResult(
        relative_path=rel,
        baseline_path=str(baseline_path),
        candidate_path=str(candidate_path),
        added_keys=added,
        removed_keys=removed,
        changed_keys=changed,
        parse_warnings=warnings,
    )


def _matches_glob(rel_posix: str, pattern: str) -> bool:
    if pattern in ("**/*.properties", "*.properties"):
        return rel_posix.endswith(".properties")
    return fnmatch.fnmatch(rel_posix, pattern)


def compare_trees(
    baseline_root: Path,
    candidate_root: Path,
    *,
    glob_pattern: str = "**/*.properties",
    encoding: str = "utf-8",
    include_removed: bool = False,
) -> TreeDiffResult:
    baseline_root = baseline_root.resolve()
    candidate_root = candidate_root.resolve()

    baseline_files: Dict[str, Path] = {}
    for path in baseline_root.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(baseline_root).as_posix()
        if _matches_glob(rel, glob_pattern):
            baseline_files[rel] = path

    candidate_files: Dict[str, Path] = {}
    for path in candidate_root.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(candidate_root).as_posix()
        if _matches_glob(rel, glob_pattern):
            candidate_files[rel] = path

    result = TreeDiffResult(
        baseline_root=str(baseline_root),
        candidate_root=str(candidate_root),
    )

    for rel in sorted(baseline_files):
        if rel not in candidate_files:
            result.baseline_only_files.append(rel)
            continue
        file_diff = compare_files(
            baseline_files[rel],
            candidate_files[rel],
            encoding=encoding,
            include_removed=include_removed,
            relative_path=rel,
        )
        if file_diff.has_any_diff or file_diff.parse_warnings:
            result.files.append(file_diff)

    for rel in sorted(candidate_files):
        if rel not in baseline_files:
            result.candidate_only_files.append(rel)

    return result


def compile_redact_patterns(patterns: Sequence[str]) -> List[Pattern[str]]:
    compiled: List[Pattern[str]] = []
    for p in patterns:
        compiled.append(re.compile(p))
    return compiled


def redact_value(value: str, patterns: Sequence[Pattern[str]]) -> str:
    if not patterns:
        return value
    for pat in patterns:
        if pat.search(value):
            return "***REDACTED***"
    return value


def format_file_diff_text(
    diff: FileDiffResult,
    *,
    include_removed: bool = False,
    redact_patterns: Optional[Sequence[Pattern[str]]] = None,
) -> str:
    lines: List[str] = []
    lines.append(f"=== {diff.relative_path} ===")
    lines.append(f"  baseline:  {diff.baseline_path}")
    lines.append(f"  candidate: {diff.candidate_path}")

    if diff.parse_warnings:
        lines.append("  parse warnings:")
        for w in diff.parse_warnings:
            lines.append(f"    - {w}")

    if diff.added_keys:
        lines.append(f"  NEW keys ({len(diff.added_keys)}):")
        for key in diff.added_keys:
            lines.append(f"    + {key}")

    if include_removed and diff.removed_keys:
        lines.append(f"  REMOVED keys ({len(diff.removed_keys)}):")
        for key in diff.removed_keys:
            lines.append(f"    - {key}")

    if diff.changed_keys:
        lines.append(f"  CHANGED values ({len(diff.changed_keys)}):")
        for ch in diff.changed_keys:
            b = redact_value(ch.baseline_value, redact_patterns or [])
            c = redact_value(ch.candidate_value, redact_patterns or [])
            lines.append(f"    ~ {ch.key}")
            lines.append(f"        prod: {b}")
            lines.append(f"        cand: {c}")

    if not diff.has_any_diff and not diff.parse_warnings:
        lines.append("  (no differences)")

    return "\n".join(lines)


def format_tree_diff_text(
    result: TreeDiffResult,
    *,
    include_removed: bool = False,
    redact_patterns: Optional[Sequence[Pattern[str]]] = None,
) -> str:
    lines: List[str] = []
    lines.append("Properties tree comparison")
    lines.append(f"  baseline root:  {result.baseline_root}")
    lines.append(f"  candidate root: {result.candidate_root}")
    lines.append("")

    risky = result.files_with_rollout_risk
    lines.append(
        f"Summary: {len(risky)} file(s) with new/changed keys, "
        f"{len(result.files)} file(s) with any diff, "
        f"{len(result.baseline_only_files)} baseline-only, "
        f"{len(result.candidate_only_files)} candidate-only"
    )
    lines.append("")

    if result.candidate_only_files:
        lines.append("Candidate-only files (no production baseline path):")
        for rel in result.candidate_only_files:
            lines.append(f"  + {rel}")
        lines.append("")

    if result.baseline_only_files:
        lines.append("Baseline-only files (missing in candidate):")
        for rel in result.baseline_only_files:
            lines.append(f"  - {rel}")
        lines.append("")

    for diff in result.files:
        lines.append(
            format_file_diff_text(
                diff,
                include_removed=include_removed,
                redact_patterns=redact_patterns,
            )
        )
        lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def file_diff_to_dict(
    diff: FileDiffResult,
    *,
    include_removed: bool = False,
    redact_patterns: Optional[Sequence[Pattern[str]]] = None,
) -> dict:
    changed = []
    for ch in diff.changed_keys:
        changed.append(
            {
                "key": ch.key,
                "baseline_value": redact_value(
                    ch.baseline_value, redact_patterns or []
                ),
                "candidate_value": redact_value(
                    ch.candidate_value, redact_patterns or []
                ),
            }
        )
    payload = {
        "relative_path": diff.relative_path,
        "baseline_path": diff.baseline_path,
        "candidate_path": diff.candidate_path,
        "added_keys": diff.added_keys,
        "changed_keys": changed,
        "parse_warnings": diff.parse_warnings,
        "has_rollout_risk": diff.has_rollout_risk,
    }
    if include_removed:
        payload["removed_keys"] = diff.removed_keys
    return payload


def tree_diff_to_dict(
    result: TreeDiffResult,
    *,
    include_removed: bool = False,
    redact_patterns: Optional[Sequence[Pattern[str]]] = None,
) -> dict:
    return {
        "baseline_root": result.baseline_root,
        "candidate_root": result.candidate_root,
        "has_rollout_risk": result.has_rollout_risk,
        "baseline_only_files": result.baseline_only_files,
        "candidate_only_files": result.candidate_only_files,
        "files": [
            file_diff_to_dict(
                f,
                include_removed=include_removed,
                redact_patterns=redact_patterns,
            )
            for f in result.files
        ],
    }


def dump_json(payload: dict) -> str:
    return json.dumps(payload, indent=2, ensure_ascii=False) + "\n"
