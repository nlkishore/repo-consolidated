"""Unified CLI: validate-file-lists | check-paired | validate-baseline | run-all."""

from __future__ import annotations

import argparse
import subprocess
import sys
from pathlib import Path
from typing import List, Optional

from hotfix_validation.baseline_checker import check_baseline_against_manifest
from hotfix_validation.config import PathRules
from hotfix_validation.file_lists_validator import FileListsValidator
from hotfix_validation.paired_commit import (
    PairedCommitChecker,
    list_changed_files_range,
    list_changed_files_rev,
    list_changed_files_staged,
)


def _cmd_validate_file_lists(args: argparse.Namespace) -> int:
    rules = PathRules(
        forbid_parent_segments=not args.allow_parent_paths,
        allowed_prefixes=tuple(args.path_prefix) if args.path_prefix else (),
    )
    validator = FileListsValidator(path_rules=rules)
    result = validator.validate_file(Path(args.file).resolve())
    if result.ok:
        print(f"OK: {result.entry_count} entr(y/ies)")
        for p in result.paths:
            print(f"  - {p}")
        return 0
    for msg in result.errors:
        print(msg, file=sys.stderr)
    return 1


def _cmd_check_paired(args: argparse.Namespace) -> int:
    repo = Path(args.repo).resolve()
    commit_message = args.commit_message or ""

    has_staged = bool(getattr(args, "staged", False))
    has_rev = bool(getattr(args, "rev", ""))
    has_range = bool(getattr(args, "range_base", "") or "") and bool(
        getattr(args, "range_head", "") or ""
    )
    mode_count = sum([has_staged, has_rev, has_range])
    if mode_count != 1:
        print(
            "Use exactly one of: --staged | --rev SHA | --range-base + --range-head",
            file=sys.stderr,
        )
        return 2

    try:
        if args.rev:
            changed = list_changed_files_rev(repo, args.rev)
            if not commit_message:
                commit_message = subprocess.run(
                    ["git", "show", "-s", "--format=%B", args.rev],
                    cwd=str(repo),
                    capture_output=True,
                    text=True,
                    check=True,
                ).stdout
        elif args.range_base and args.range_head:
            changed = list_changed_files_range(repo, args.range_base, args.range_head)
        else:
            changed = list_changed_files_staged(repo)
    except (RuntimeError, subprocess.CalledProcessError) as exc:
        print(str(exc), file=sys.stderr)
        return 1

    checker = PairedCommitChecker(
        build_script=args.build_script,
        file_list_names=args.file_list_names.split(",") if args.file_list_names else None,
        bypass_token=args.bypass_token or "[skip-hotfix-pair]",
    )
    r = checker.check_paths(changed, commit_message=commit_message)
    print(r.message)
    return 0 if r.ok else 1


def _cmd_validate_baseline(args: argparse.Namespace) -> int:
    result = check_baseline_against_manifest(
        Path(args.file_lists).resolve(),
        Path(args.manifest).resolve(),
        require_artifact=args.require_artifact,
    )
    if result.ok:
        print("OK: baseline matches release manifest.")
        return 0
    for e in result.errors:
        print(e, file=sys.stderr)
    return 1


def _cmd_run_all(args: argparse.Namespace) -> int:
    code = _cmd_validate_file_lists(
        argparse.Namespace(
            file=args.file_lists,
            allow_parent_paths=args.allow_parent_paths,
            path_prefix=args.path_prefix,
        )
    )
    if code != 0:
        return code

    paired_ns = argparse.Namespace(
        repo=args.repo,
        staged=args.staged,
        rev=args.rev,
        range_base=args.range_base,
        range_head=args.range_head,
        build_script=args.build_script,
        file_list_names=args.file_list_names,
        bypass_token=args.bypass_token,
        commit_message=args.commit_message,
    )
    code = _cmd_check_paired(paired_ns)
    if code != 0:
        return code

    if args.manifest:
        return _cmd_validate_baseline(
            argparse.Namespace(
                file_lists=args.file_lists,
                manifest=args.manifest,
                require_artifact=args.require_artifact,
            )
        )

    print("run-all: skipped baseline (no --manifest).")
    return 0


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="hotfix_validation",
        description="HotFix branch validators (see HOTFIX_BRANCH_VALIDATION_DESIGN.md)",
    )
    sub = p.add_subparsers(dest="command", required=True)

    vfl = sub.add_parser(
        "validate-file-lists",
        help="Validate fileLists.txt YAML and schema",
    )
    vfl.add_argument("file", help="Path to fileLists.txt")
    vfl.add_argument(
        "--allow-parent-paths",
        action="store_true",
        help="Allow '..' in path segments (default: forbid)",
    )
    vfl.add_argument(
        "--path-prefix",
        action="append",
        default=[],
        metavar="PREFIX",
        help="Require each entry path to start with PREFIX (repeatable)",
    )
    vfl.set_defaults(func=_cmd_validate_file_lists)

    pair = sub.add_parser(
        "check-paired",
        help="Ensure buildScripts.sh and fileLists change together",
    )
    pair.add_argument("--repo", default=".", help="Git repository root")
    pair.add_argument("--staged", action="store_true", help="git diff --cached")
    pair.add_argument("--rev", default="", metavar="SHA", help="Single commit (CI)")
    pair.add_argument(
        "--range-base",
        dest="range_base",
        default="",
        metavar="BASE",
        help="With --range-head: diff BASE...HEAD",
    )
    pair.add_argument(
        "--range-head", dest="range_head", default="", metavar="HEAD"
    )
    pair.add_argument("--build-script", default="buildScripts.sh")
    pair.add_argument(
        "--file-list-names",
        default="",
        help="Comma-separated basenames, e.g. fileLists.txt,fileLists.yaml",
    )
    pair.add_argument("--bypass-token", default="[skip-hotfix-pair]")
    pair.add_argument(
        "--commit-message",
        default="",
        help="Override message for bypass check (pre-commit usually empty)",
    )
    pair.set_defaults(func=_cmd_check_paired)

    base = sub.add_parser(
        "validate-baseline",
        help="Compare fileLists baseline to release-build-manifest.json",
    )
    base.add_argument("file_lists", help="Path to fileLists.txt")
    base.add_argument("manifest", help="Path to release-build-manifest.json")
    base.add_argument(
        "--require-artifact",
        action="store_true",
        help="Require manifest artifact_url",
    )
    base.set_defaults(func=_cmd_validate_baseline)

    ra = sub.add_parser(
        "run-all",
        help="validate-file-lists + check-paired + optional validate-baseline",
    )
    ra.add_argument("--file-lists", required=True)
    ra.add_argument("--repo", default=".")
    ra.add_argument("--manifest", default="", help="If set, run baseline check")
    ra.add_argument("--require-artifact", action="store_true")
    ra.add_argument("--allow-parent-paths", action="store_true")
    ra.add_argument("--path-prefix", action="append", default=[])
    ra.add_argument("--staged", action="store_true")
    ra.add_argument("--rev", default="")
    ra.add_argument("--range-base", default="")
    ra.add_argument("--range-head", default="")
    ra.add_argument("--build-script", default="buildScripts.sh")
    ra.add_argument("--file-list-names", default="")
    ra.add_argument("--bypass-token", default="[skip-hotfix-pair]")
    ra.add_argument("--commit-message", default="")
    ra.set_defaults(func=_cmd_run_all)

    return p


def main(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    parser = build_parser()
    args = parser.parse_args(argv)
    return int(args.func(args))


if __name__ == "__main__":
    raise SystemExit(main())
