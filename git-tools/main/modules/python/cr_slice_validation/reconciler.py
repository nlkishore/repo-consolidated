"""Compare baseline..expected vs baseline..slice."""

from __future__ import annotations

import fnmatch
from dataclasses import dataclass, field
from pathlib import Path

from cr_slice_validation.bitbucket_cr import BitbucketCrClient
from cr_slice_validation.config import BitbucketEnv
from cr_slice_validation.diff_parser import flatten_ranges, parse_unified_diff
from cr_slice_validation.git_ops import (
    GitError,
    diff_file,
    diff_name_only,
    is_ancestor,
    log_commits,
    normalize_patch,
    patch_hash,
    rev_parse,
)
from cr_slice_validation.manifest import CrManifest


@dataclass
class FileMismatch:
    path: str
    expected_hash: str
    actual_hash: str


@dataclass
class LineLevelGap:
    path: str
    missing_line_count: int
    sample_lines: list[int]


@dataclass
class ReconcileResult:
    cr_id: str
    status: str
    baseline_ref: str
    baseline_sha: str
    expected_ref: str
    expected_sha: str
    slice_ref: str
    slice_sha: str
    missing_files: list[str] = field(default_factory=list)
    extra_files: list[str] = field(default_factory=list)
    file_mismatches: list[FileMismatch] = field(default_factory=list)
    unattributed_commits: list[str] = field(default_factory=list)
    line_level_gaps: list[LineLevelGap] = field(default_factory=list)
    pr_heads_used: list[str] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)
    files_expected_count: int = 0
    files_actual_count: int = 0

    @property
    def ok(self) -> bool:
        return self.status == "PASS"

    def to_dict(self) -> dict:
        return {
            "cr_id": self.cr_id,
            "status": self.status,
            "baseline_ref": self.baseline_ref,
            "baseline_sha": self.baseline_sha,
            "expected_ref": self.expected_ref,
            "expected_sha": self.expected_sha,
            "slice_ref": self.slice_ref,
            "slice_sha": self.slice_sha,
            "summary": {
                "files_expected": self.files_expected_count,
                "files_actual": self.files_actual_count,
                "missing": len(self.missing_files),
                "extra": len(self.extra_files),
                "mismatches": len(self.file_mismatches),
                "line_level_gaps": len(self.line_level_gaps),
                "unattributed_commits": len(self.unattributed_commits),
            },
            "missing_files": self.missing_files,
            "extra_files": self.extra_files,
            "file_mismatches": [
                {
                    "path": m.path,
                    "expected_hash": m.expected_hash,
                    "actual_hash": m.actual_hash,
                }
                for m in self.file_mismatches
            ],
            "unattributed_commits": self.unattributed_commits,
            "line_level_gaps": [
                {
                    "path": g.path,
                    "missing_line_count": g.missing_line_count,
                    "sample_lines": g.sample_lines,
                }
                for g in self.line_level_gaps
            ],
            "pr_heads_used": self.pr_heads_used,
            "errors": self.errors,
        }


def _matches_exclude(path: str, patterns: list[str]) -> bool:
    norm = path.replace("\\", "/")
    for pat in patterns:
        if fnmatch.fnmatch(norm, pat) or fnmatch.fnmatchcase(norm, pat):
            return True
    return False


def _filter_paths(paths: list[str], exclude: list[str]) -> set[str]:
    return {p for p in paths if not _matches_exclude(p, exclude)}


def _resolve_expected_ref(
    manifest: CrManifest,
    repo: Path,
    bb: BitbucketCrClient | None,
) -> tuple[str, str, list[str], list[str]]:
    """Return (expected_ref_label, expected_sha, pr_head_shas, errors)."""
    errors: list[str] = []
    pr_heads: list[str] = []

    if manifest.expected_ref:
        sha = rev_parse(repo, manifest.expected_ref)
        return manifest.expected_ref, sha, pr_heads, errors

    if manifest.pr_branches:
        # Union diff paths across branches; use tip of last branch as expected_sha
        # for file content compare when multiple branches — prefer explicit expected_ref.
        last = manifest.pr_branches[-1]
        sha = rev_parse(repo, last)
        return f"pr_branches:{','.join(manifest.pr_branches)}", sha, pr_heads, errors

    if manifest.pull_requests:
        if not bb:
            errors.append(
                "pull_requests in manifest require Bitbucket env "
                "(BITBUCKET_SERVER_URL, BITBUCKET_TOKEN, BITBUCKET_PROJECT_KEY, "
                "BITBUCKET_REPO_SLUG) or set expected_ref"
            )
            return "", "", pr_heads, errors
        heads = bb.fetch_pr_heads(manifest.pull_requests)
        pr_heads = [h.from_commit for h in heads if h.from_commit]
        if not pr_heads:
            errors.append("No PR head commits resolved from Bitbucket")
            return "", "", pr_heads, errors
        # Use latest PR head as expected tip when multiple PRs (integration branch preferred)
        label = f"pr-heads:{','.join(str(h.pr_id) for h in heads)}"
        return label, pr_heads[-1], pr_heads, errors

    errors.append("Cannot resolve expected ref")
    return "", "", pr_heads, errors


def _union_paths_from_pr_branches(
    repo: Path, baseline_sha: str, branches: list[str]
) -> set[str]:
    paths: set[str] = set()
    for branch in branches:
        head = rev_parse(repo, branch)
        paths.update(diff_name_only(repo, baseline_sha, head))
    return paths


def reconcile(
    manifest: CrManifest,
    repo: Path,
    slice_ref: str,
    expected_ref_override: str = "",
    line_level: bool = False,
    bb_env: BitbucketEnv | None = None,
) -> ReconcileResult:
    slice_ref = slice_ref or manifest.slice_ref
    if not slice_ref:
        return ReconcileResult(
            cr_id=manifest.cr_id,
            status="FAIL",
            baseline_ref=manifest.baseline_ref,
            baseline_sha="",
            expected_ref="",
            expected_sha="",
            slice_ref="",
            slice_sha="",
            errors=["slice_ref is required (manifest or --slice-ref)"],
        )

    bb: BitbucketCrClient | None = None
    if bb_env:
        bb = BitbucketCrClient(bb_env)

    try:
        baseline_sha = rev_parse(repo, manifest.baseline_ref)
        slice_sha = rev_parse(repo, slice_ref)

        if expected_ref_override:
            expected_label = expected_ref_override
            expected_sha = rev_parse(repo, expected_ref_override)
            pr_heads: list[str] = []
        else:
            expected_label, expected_sha, pr_heads, resolve_errors = (
                _resolve_expected_ref(manifest, repo, bb)
            )
            if resolve_errors:
                return ReconcileResult(
                    cr_id=manifest.cr_id,
                    status="FAIL",
                    baseline_ref=manifest.baseline_ref,
                    baseline_sha=baseline_sha,
                    expected_ref=expected_label,
                    expected_sha=expected_sha,
                    slice_ref=slice_ref,
                    slice_sha=slice_sha,
                    errors=resolve_errors,
                )

        if manifest.pr_branches and not manifest.expected_ref and not expected_ref_override:
            paths_expected = _filter_paths(
                sorted(
                    _union_paths_from_pr_branches(
                        repo, baseline_sha, manifest.pr_branches
                    )
                ),
                manifest.exclude_paths,
            )
        else:
            paths_expected = _filter_paths(
                diff_name_only(repo, baseline_sha, expected_sha),
                manifest.exclude_paths,
            )

        paths_actual = _filter_paths(
            diff_name_only(repo, baseline_sha, slice_sha),
            manifest.exclude_paths,
        )

        set_expected = set(paths_expected)
        set_actual = set(paths_actual)

        missing = sorted(set_expected - set_actual)
        extra = sorted(set_actual - set_expected)
        mismatches: list[FileMismatch] = []
        line_gaps: list[LineLevelGap] = []

        branch_shas: list[str] = []
        if manifest.pr_branches and not manifest.expected_ref and not expected_ref_override:
            branch_shas = [rev_parse(repo, b) for b in manifest.pr_branches]

        for path in sorted(set_expected & set_actual):
            patch_e = ""
            patch_a = diff_file(repo, baseline_sha, slice_sha, path)
            if branch_shas:
                patches_e = [
                    diff_file(repo, baseline_sha, sha, path) for sha in branch_shas
                ]
                match = any(
                    normalize_patch(pe) == normalize_patch(patch_a) for pe in patches_e
                )
                patch_e = patches_e[0] if patches_e else ""
                if not match:
                    mismatches.append(
                        FileMismatch(
                            path=path,
                            expected_hash="|".join(patch_hash(pe) for pe in patches_e),
                            actual_hash=patch_hash(patch_a),
                        )
                    )
            else:
                patch_e = diff_file(repo, baseline_sha, expected_sha, path)
                if normalize_patch(patch_e) != normalize_patch(patch_a):
                    mismatches.append(
                        FileMismatch(
                            path=path,
                            expected_hash=patch_hash(patch_e),
                            actual_hash=patch_hash(patch_a),
                        )
                    )
            if line_level and patch_e:
                ranges_e = parse_unified_diff(patch_e)
                ranges_a = parse_unified_diff(patch_a)
                lines_e = flatten_ranges(ranges_e.get(path, []))
                lines_a = flatten_ranges(ranges_a.get(path, []))
                missing_lines = sorted(lines_e - lines_a)
                if missing_lines:
                    line_gaps.append(
                        LineLevelGap(
                            path=path,
                            missing_line_count=len(missing_lines),
                            sample_lines=missing_lines[:20],
                        )
                    )

        attributed = set(manifest.optional_commits) | set(pr_heads)
        unattributed: list[str] = []
        for commit in log_commits(repo, baseline_sha, slice_sha):
            if commit in attributed:
                continue
            if any(is_ancestor(repo, head, commit) for head in pr_heads if head):
                continue
            unattributed.append(commit)

        fail = bool(missing or extra or mismatches or line_gaps)
        status = "FAIL" if fail else "PASS"

        return ReconcileResult(
            cr_id=manifest.cr_id,
            status=status,
            baseline_ref=manifest.baseline_ref,
            baseline_sha=baseline_sha,
            expected_ref=expected_label,
            expected_sha=expected_sha,
            slice_ref=slice_ref,
            slice_sha=slice_sha,
            missing_files=missing,
            extra_files=extra,
            file_mismatches=mismatches,
            unattributed_commits=unattributed,
            line_level_gaps=line_gaps,
            pr_heads_used=pr_heads,
            files_expected_count=len(set_expected),
            files_actual_count=len(set_actual),
        )

    except GitError as exc:
        return ReconcileResult(
            cr_id=manifest.cr_id,
            status="FAIL",
            baseline_ref=manifest.baseline_ref,
            baseline_sha="",
            expected_ref=manifest.expected_ref,
            expected_sha="",
            slice_ref=slice_ref,
            slice_sha="",
            errors=[str(exc)],
        )
    finally:
        if bb:
            bb.close()
