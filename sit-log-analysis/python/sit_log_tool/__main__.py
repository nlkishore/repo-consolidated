"""CLI: python -m sit_log_tool <command> ..."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from .catalog import list_query_ids
from .jsonl_io import append_valid_jsonl
from .queries import run_query


def _tool_root() -> Path:
    return Path(__file__).resolve().parents[2]


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="SIT log analysis (Python, no jq)")
    sub = parser.add_subparsers(dest="command", required=True)

    p_list = sub.add_parser("list-queries", help="Print query ids from catalog")
    p_list.add_argument(
        "--catalog",
        type=Path,
        default=_tool_root() / "config" / "query-catalog.yaml",
    )

    p_run = sub.add_parser("run-query", help="Execute one query")
    p_run.add_argument("query_id")
    p_run.add_argument("--events", type=Path, required=True)
    p_run.add_argument("--out-dir", type=Path, required=True)

    p_val = sub.add_parser(
        "append-valid-jsonl", help="Append valid JSON lines from source to dest"
    )
    p_val.add_argument("source", type=Path)
    p_val.add_argument("dest", type=Path)

    args = parser.parse_args(argv)

    if args.command == "list-queries":
        for qid in list_query_ids(args.catalog):
            print(qid)
        return 0

    if args.command == "run-query":
        run_query(args.query_id, args.events, args.out_dir)
        print(f"Query {args.query_id} complete -> {args.out_dir}")
        return 0

    if args.command == "append-valid-jsonl":
        count = append_valid_jsonl(args.source, args.dest)
        print(f"Appended {count} lines to {args.dest}")
        return 0

    return 1


if __name__ == "__main__":
    sys.exit(main())
