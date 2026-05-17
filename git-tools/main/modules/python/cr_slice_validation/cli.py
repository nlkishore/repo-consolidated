"""CLI: validate-manifest | reconcile | run-all | build-manifest."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import List, Optional

from cr_slice_validation.config import BitbucketEnv
from cr_slice_validation.manifest import load_manifest, validate_manifest
from cr_slice_validation.reconciler import reconcile
from cr_slice_validation.report import write_html, write_json


def _cmd_validate_manifest(args: argparse.Namespace) -> int:
    manifest = load_manifest(Path(args.manifest).resolve())
    result = validate_manifest(manifest)
    if result.ok:
        print(f"OK: manifest {manifest.cr_id}")
        return 0
    for err in result.errors:
        print(err, file=sys.stderr)
    return 1


def _cmd_reconcile(args: argparse.Namespace) -> int:
    manifest_path = Path(args.manifest).resolve()
    manifest = load_manifest(manifest_path)
    val = validate_manifest(manifest)
    if not val.ok:
        for err in val.errors:
            print(err, file=sys.stderr)
        return 1

    slice_ref = args.slice_ref or manifest.slice_ref
    expected_ref = args.expected_ref or ""
    bb_env = BitbucketEnv.from_env() if args.use_bitbucket else None

    result = reconcile(
        manifest,
        Path(args.repo).resolve(),
        slice_ref=slice_ref,
        expected_ref_override=expected_ref,
        line_level=args.line_level,
        bb_env=bb_env,
    )

    out = Path(args.output).resolve() if args.output else None
    if out:
        if args.format == "html":
            write_html(result, out)
        else:
            write_json(result, out)
        print(f"Report written: {out}")

    print(f"Status: {result.status}")
    if result.missing_files:
        print(f"  missing_files: {len(result.missing_files)}")
    if result.extra_files:
        print(f"  extra_files: {len(result.extra_files)}")
    if result.file_mismatches:
        print(f"  file_mismatches: {len(result.file_mismatches)}")
    for err in result.errors:
        print(err, file=sys.stderr)

    if result.ok:
        return 0
    if args.allow_waiver and manifest.notes.strip():
        print("Waiver: manifest notes present; failing anyway unless policy allows.")
    return 1


def _cmd_run_all(args: argparse.Namespace) -> int:
    code = _cmd_validate_manifest(args)
    if code != 0:
        return code
    return _cmd_reconcile(args)


def _cmd_build_manifest(args: argparse.Namespace) -> int:
    """Emit manifest skeleton (Phase 2: Bitbucket label query)."""
    cr_id = args.cr_id
    out = Path(args.output).resolve() if args.output else Path(f"{cr_id}-manifest.yaml")
    skeleton = f"""cr_id: {cr_id}
title: ""
baseline_ref: {args.baseline_ref or "release/TODO"}
expected_ref: integration/{cr_id.lower()}-all-prs
slice_ref: {args.slice_ref or f"release-{cr_id.lower()}"}
pull_requests: []
pr_branches: []
optional_commits: []
exclude_paths: []
notes: ""
"""
    out.write_text(skeleton, encoding="utf-8")
    print(f"Wrote skeleton manifest: {out}")
    print("Edit pull_requests / expected_ref before reconcile.")
    return 0


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="cr_slice_validation",
        description="CR decoupling slice reconciliation (see CR_SLICE_REVIEW_AUTOMATION_DESIGN.md)",
    )
    sub = p.add_subparsers(dest="command", required=True)

    vm = sub.add_parser("validate-manifest", help="Validate cr-manifest.yaml schema")
    vm.add_argument("--manifest", required=True, help="Path to cr-manifest.yaml")
    vm.set_defaults(func=_cmd_validate_manifest)

    rec = sub.add_parser("reconcile", help="Compare baseline..expected vs baseline..slice")
    rec.add_argument("--manifest", required=True)
    rec.add_argument("--repo", default=".", help="Git repository root")
    rec.add_argument("--slice-ref", default="", help="Override manifest slice_ref")
    rec.add_argument("--expected-ref", default="", help="Override manifest expected_ref")
    rec.add_argument("--output", default="", help="Report output path")
    rec.add_argument(
        "--format",
        choices=("json", "html"),
        default="json",
    )
    rec.add_argument("--line-level", action="store_true", help="Line-level gap detection")
    rec.add_argument(
        "--use-bitbucket",
        action="store_true",
        help="Resolve PR heads via BITBUCKET_* env when expected_ref omitted",
    )
    rec.add_argument(
        "--allow-waiver",
        action="store_true",
        help="Document only; still exits 1 on FAIL",
    )
    rec.set_defaults(func=_cmd_reconcile)

    ra = sub.add_parser("run-all", help="validate-manifest + reconcile")
    ra.add_argument("--manifest", required=True)
    ra.add_argument("--repo", default=".")
    ra.add_argument("--slice-ref", default="")
    ra.add_argument("--expected-ref", default="")
    ra.add_argument("--output", default="reconcile-report.json")
    ra.add_argument("--format", choices=("json", "html"), default="json")
    ra.add_argument("--line-level", action="store_true")
    ra.add_argument("--use-bitbucket", action="store_true")
    ra.add_argument("--allow-waiver", action="store_true")
    ra.set_defaults(func=_cmd_run_all)

    bm = sub.add_parser("build-manifest", help="Write manifest skeleton")
    bm.add_argument("--cr-id", required=True, help="e.g. CR-102")
    bm.add_argument("--baseline-ref", default="")
    bm.add_argument("--slice-ref", default="")
    bm.add_argument("--output", default="")
    bm.set_defaults(func=_cmd_build_manifest)

    return p


def main(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    parser = build_parser()
    args = parser.parse_args(argv)
    return int(args.func(args))
