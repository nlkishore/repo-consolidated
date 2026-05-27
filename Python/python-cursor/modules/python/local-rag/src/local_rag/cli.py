from __future__ import annotations

import argparse
import json
import sys

import structlog

from local_rag.config import SQLITE_PATH, load_paths_config
from local_rag.indexer import run_index
from local_rag.metadata_db import MetadataDB
from local_rag.rag import query


def _configure_logging() -> None:
    structlog.configure(
        processors=[
            structlog.processors.add_log_level,
            structlog.dev.ConsoleRenderer(),
        ]
    )


def cmd_index(args: argparse.Namespace) -> int:
    stats = run_index(force=args.force)
    print(json.dumps(stats, indent=2))
    return 0


def cmd_status(_: argparse.Namespace) -> int:
    paths = load_paths_config()
    db = MetadataDB(SQLITE_PATH)
    try:
        stats = db.stats()
    finally:
        db.close()

    print("Include roots:")
    for root in paths.include_roots:
        print(f"  - {root}")
    print(f"\nIndexed files: {stats['files']}")
    print(f"Chunks (SQLite): {stats['chunks']}")
    return 0


def cmd_query(args: argparse.Namespace) -> int:
    result = query(args.question, top_k=args.top_k)
    print(result["answer"])
    print("\n--- Sources ---")
    for src in result["sources"]:
        print(f"  {src}")
    return 0


def main() -> None:
    _configure_logging()
    parser = argparse.ArgumentParser(
        prog="local-rag",
        description="Index and query local documents (Ollama + Chroma).",
    )
    sub = parser.add_subparsers(dest="command", required=True)

    p_index = sub.add_parser("index", help="Index configured folders")
    p_index.add_argument("--force", action="store_true", help="Re-index all files")
    p_index.set_defaults(func=cmd_index)

    p_status = sub.add_parser("status", help="Show index roots and stats")
    p_status.set_defaults(func=cmd_status)

    p_query = sub.add_parser("query", help="Ask a question over the index")
    p_query.add_argument("question", help="Natural language question")
    p_query.add_argument("--top-k", type=int, default=None, help="Number of chunks to retrieve")
    p_query.set_defaults(func=cmd_query)

    args = parser.parse_args()
    try:
        sys.exit(args.func(args))
    except (RuntimeError, FileNotFoundError, ValueError) as exc:
        print(f"Error: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
