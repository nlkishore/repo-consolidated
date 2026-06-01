"""Read and write JSON Lines event files."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Iterator


def iter_events(path: Path) -> Iterator[dict[str, Any]]:
    with path.open(encoding="utf-8", errors="replace") as handle:
        for line_no, line in enumerate(handle, start=1):
            text = line.strip()
            if not text:
                continue
            try:
                row = json.loads(text)
            except json.JSONDecodeError as exc:
                raise ValueError(f"{path}:{line_no}: invalid JSON: {exc}") from exc
            if not isinstance(row, dict):
                raise ValueError(f"{path}:{line_no}: expected JSON object")
            yield row


def load_events(path: Path) -> list[dict[str, Any]]:
    return list(iter_events(path))


def append_valid_jsonl(source: Path, dest: Path) -> int:
    """Copy valid JSON lines from source into dest; return count written."""
    count = 0
    dest.parent.mkdir(parents=True, exist_ok=True)
    with source.open(encoding="utf-8", errors="replace") as inp, dest.open(
        "a", encoding="utf-8"
    ) as out:
        for line in inp:
            text = line.strip()
            if not text:
                continue
            json.loads(text)
            out.write(text + "\n")
            count += 1
    return count


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
