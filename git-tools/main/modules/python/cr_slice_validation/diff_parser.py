"""Parse unified git diff into per-file added line ranges (aligned with pr_review.diff_parser)."""

from __future__ import annotations

import re
from collections import defaultdict

_HUNK_RE = re.compile(r"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@")


def parse_unified_diff(diff_text: str) -> dict[str, list[tuple[int, int]]]:
    """Path -> inclusive [start, end] ranges on the new file (added lines only)."""
    file_ranges: dict[str, list[tuple[int, int]]] = defaultdict(list)
    current_file: str | None = None
    new_line = 0
    run_start: int | None = None
    last_added = 0

    def flush_run() -> None:
        nonlocal run_start, last_added, current_file
        if current_file and run_start is not None and last_added >= run_start:
            file_ranges[current_file].append((run_start, last_added))
        run_start = None

    for line in diff_text.splitlines():
        if line.startswith("diff --git "):
            flush_run()
            current_file = _parse_diff_git_line(line)
            continue
        if line.startswith("+++ ") or line.startswith("--- "):
            continue
        m = _HUNK_RE.match(line)
        if m:
            flush_run()
            new_line = int(m.group(1))
            continue
        if not current_file:
            continue
        if line.startswith("\\"):
            continue
        if not line:
            continue
        tag = line[0]
        rest = line[1:]
        if tag == "+":
            if not rest.startswith("+"):
                if run_start is None:
                    run_start = new_line
                last_added = new_line
                new_line += 1
            continue
        if tag == " ":
            flush_run()
            new_line += 1
            continue
    flush_run()
    return dict(file_ranges)


def _parse_diff_git_line(line: str) -> str:
    parts = line.split()
    if len(parts) >= 4 and parts[2].startswith("b/"):
        return parts[2][2:]
    if len(parts) >= 4:
        return parts[3].lstrip("b/")
    return parts[-1].lstrip("b/")


def flatten_ranges(ranges: list[tuple[int, int]]) -> set[int]:
    lines: set[int] = set()
    for a, b in sorted(ranges):
        lines.update(range(a, b + 1))
    return lines
