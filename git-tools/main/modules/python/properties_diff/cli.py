"""CLI: compare-files | compare-trees for production vs UAT .properties."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import List, Optional

from properties_diff.compare import (
    compare_files,
    compare_trees,
    compile_redact_patterns,
    dump_json,
    file_diff_to_dict,
    format_file_diff_text,
    format_tree_diff_text,
    tree_diff_to_dict,
)


def _exit_code(has_risk: bool, *, fail_on_diff: bool) -> int:
    if not fail_on_diff:
        return 0
    return 1 if has_risk else 0


def _cmd_compare_files(args: argparse.Namespace) -> int:
    baseline = Path(args.baseline).resolve()
    candidate = Path(args.candidate).resolve()
    if not baseline.is_file():
        print(f"Baseline file not found: {baseline}", file=sys.stderr)
        return 2
    if not candidate.is_file():
        print(f"Candidate file not found: {candidate}", file=sys.stderr)
        return 2

    redact = compile_redact_patterns(args.redact or [])
    diff = compare_files(
        baseline,
        candidate,
        encoding=args.encoding,
        include_removed=args.include_removed,
        relative_path=args.relative_path or candidate.name,
    )

    if args.format == "json":
        print(
            dump_json(
                file_diff_to_dict(
                    diff,
                    include_removed=args.include_removed,
                    redact_patterns=redact,
                )
            ),
            end="",
        )
    else:
        print(
            format_file_diff_text(
                diff,
                include_removed=args.include_removed,
                redact_patterns=redact,
            )
        )

    return _exit_code(diff.has_rollout_risk, fail_on_diff=args.fail_on_diff)


def _cmd_compare_trees(args: argparse.Namespace) -> int:
    baseline_root = Path(args.baseline_root).resolve()
    candidate_root = Path(args.candidate_root).resolve()
    if not baseline_root.is_dir():
        print(f"Baseline root not found: {baseline_root}", file=sys.stderr)
        return 2
    if not candidate_root.is_dir():
        print(f"Candidate root not found: {candidate_root}", file=sys.stderr)
        return 2

    redact = compile_redact_patterns(args.redact or [])
    result = compare_trees(
        baseline_root,
        candidate_root,
        glob_pattern=args.glob,
        encoding=args.encoding,
        include_removed=args.include_removed,
    )

    if args.format == "json":
        print(
            dump_json(
                tree_diff_to_dict(
                    result,
                    include_removed=args.include_removed,
                    redact_patterns=redact,
                )
            ),
            end="",
        )
    else:
        print(
            format_tree_diff_text(
                result,
                include_removed=args.include_removed,
                redact_patterns=redact,
            )
        )

    return _exit_code(result.has_rollout_risk, fail_on_diff=args.fail_on_diff)


def _add_common_compare_flags(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--encoding",
        default="utf-8",
        help="File encoding (default: utf-8; use iso-8859-1 for legacy JDK exports)",
    )
    parser.add_argument(
        "--format",
        choices=("text", "json"),
        default="text",
        help="Output format (default: text)",
    )
    parser.add_argument(
        "--include-removed",
        action="store_true",
        help="Report keys present in baseline but missing in candidate",
    )
    parser.add_argument(
        "--redact",
        action="append",
        default=[],
        metavar="REGEX",
        help="Redact values matching REGEX in output (repeatable)",
    )
    parser.add_argument(
        "--fail-on-diff",
        action="store_true",
        default=True,
        help="Exit 1 when new/changed keys or candidate-only files exist (default)",
    )
    parser.add_argument(
        "--no-fail-on-diff",
        action="store_false",
        dest="fail_on_diff",
        help="Always exit 0 regardless of differences",
    )


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        prog="properties_diff",
        description="Compare Java .properties: production baseline vs UAT/candidate",
    )
    sub = p.add_subparsers(dest="command", required=True)

    cf = sub.add_parser(
        "compare-files",
        help="Diff a single baseline vs candidate .properties file",
    )
    cf.add_argument("--baseline", required=True, help="Production baseline file")
    cf.add_argument("--candidate", required=True, help="UAT or candidate file")
    cf.add_argument(
        "--relative-path",
        default="",
        help="Logical path label in reports (default: candidate filename)",
    )
    _add_common_compare_flags(cf)
    cf.set_defaults(func=_cmd_compare_files)

    ct = sub.add_parser(
        "compare-trees",
        help="Diff all matching .properties under two directory roots",
    )
    ct.add_argument(
        "--baseline-root",
        required=True,
        help="Production config tree root for one instance",
    )
    ct.add_argument(
        "--candidate-root",
        required=True,
        help="UAT/candidate config tree root for the same instance",
    )
    ct.add_argument(
        "--glob",
        default="**/*.properties",
        help='Relative path glob (default: "**/*.properties")',
    )
    _add_common_compare_flags(ct)
    ct.set_defaults(func=_cmd_compare_trees)

    return p


def main(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    parser = build_parser()
    args = parser.parse_args(argv)
    return int(args.func(args))
