from __future__ import annotations

import fnmatch
from pathlib import Path

from local_rag.config import PathsConfig


def _path_matches_glob(path: Path, pattern: str) -> bool:
    normalized = pattern.replace("\\", "/")
    parts = path.as_posix()
    if "**" in normalized:
        return fnmatch.fnmatch(parts, normalized)
    return fnmatch.fnmatch(path.name, normalized.split("/")[-1])


def should_skip(path: Path, cfg: PathsConfig) -> bool:
    for pat in cfg.exclude_globs:
        if _path_matches_glob(path, pat):
            return True
    return False


def iter_files(cfg: PathsConfig) -> list[Path]:
    max_bytes = cfg.max_file_mb * 1024 * 1024
    found: list[Path] = []

    for root in cfg.include_roots:
        if not root.is_dir():
            continue
        for path in root.rglob("*"):
            if not path.is_file():
                continue
            if path.suffix.lower() not in cfg.extensions:
                continue
            if should_skip(path, cfg):
                continue
            try:
                if path.stat().st_size > max_bytes:
                    continue
            except OSError:
                continue
            found.append(path.resolve())

    found.sort()
    return found
