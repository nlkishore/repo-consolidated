"""Git subprocess helpers."""

from __future__ import annotations

import hashlib
import subprocess
from pathlib import Path


class GitError(RuntimeError):
    pass


def _run(repo: Path, *args: str) -> str:
    cmd = ["git", *args]
    try:
        proc = subprocess.run(
            cmd,
            cwd=str(repo),
            capture_output=True,
            text=True,
            check=True,
        )
    except subprocess.CalledProcessError as exc:
        err = (exc.stderr or exc.stdout or "").strip()
        raise GitError(f"git {' '.join(args)} failed: {err}") from exc
    return proc.stdout


def rev_parse(repo: Path, ref: str) -> str:
    return _run(repo, "rev-parse", ref).strip()


def diff_name_only(repo: Path, base: str, head: str) -> list[str]:
    out = _run(repo, "diff", "--name-only", f"{base}..{head}")
    return [p.strip().replace("\\", "/") for p in out.splitlines() if p.strip()]


def diff_file(repo: Path, base: str, head: str, path: str) -> str:
    return _run(repo, "diff", f"{base}..{head}", "--", path)


def log_commits(repo: Path, base: str, head: str) -> list[str]:
    out = _run(repo, "log", "--format=%H", f"{base}..{head}")
    return [line.strip() for line in out.splitlines() if line.strip()]


def is_ancestor(repo: Path, ancestor: str, commit: str) -> bool:
    proc = subprocess.run(
        ["git", "merge-base", "--is-ancestor", ancestor, commit],
        cwd=str(repo),
        capture_output=True,
    )
    return proc.returncode == 0


def normalize_patch(patch: str) -> str:
    lines = []
    for line in patch.replace("\r\n", "\n").splitlines():
        lines.append(line.rstrip())
    return "\n".join(lines).strip()


def patch_hash(patch: str) -> str:
    normalized = normalize_patch(patch)
    return hashlib.sha256(normalized.encode("utf-8")).hexdigest()[:16]
