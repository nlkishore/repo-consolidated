"""
Log / config readers — read-only MCP resources (data streams).

Complexity ladder (read top → bottom when reviewing):
  Level 1  System + environment snapshots
  Level 2  Workspace log tails
  Level 3  Project config files
"""

from __future__ import annotations

import os
import platform
from pathlib import Path

from mcp.server.fastmcp import FastMCP

WORKSPACE_ROOT = Path(__file__).resolve().parent.parent


def _tail_text_file(path: Path, max_lines: int = 80) -> str:
    """Return the last max_lines of a text file, with a short header."""
    if not path.exists():
        return f"(file not found: {path})"
    if not path.is_file():
        return f"(not a file: {path})"
    try:
        lines = path.read_text(encoding="utf-8", errors="replace").splitlines()
        tail = lines[-max_lines:] if len(lines) > max_lines else lines
        header = f"# {path} (last {len(tail)} of {len(lines)} lines)\n"
        return header + "\n".join(tail)
    except Exception as e:
        return f"(error reading {path}: {e})"


def register(mcp: FastMCP) -> None:
    # ------------------------------------------------------------------ #
    # Level 1 — environment snapshots
    # ------------------------------------------------------------------ #

    @mcp.resource("env://system_info")
    def get_system_info() -> str:
        """OS, Python version, cwd, and workspace root."""
        return (
            f"OS name: {os.name}\n"
            f"Platform: {platform.platform()}\n"
            f"Python: {platform.python_version()}\n"
            f"Working directory: {os.getcwd()}\n"
            f"Workspace root: {WORKSPACE_ROOT}"
        )

    @mcp.resource("env://environment")
    def get_environment_config() -> str:
        """Selected non-secret environment variables useful for local debugging."""
        allow = {
            "PATH",
            "HOME",
            "USERPROFILE",
            "USERNAME",
            "USER",
            "COMPUTERNAME",
            "HOSTNAME",
            "OS",
            "PYTHONPATH",
            "VIRTUAL_ENV",
            "TEMP",
            "TMP",
            "SHELL",
            "TERM",
        }
        lines = [f"{key}={os.environ[key]}" for key in sorted(allow) if key in os.environ]
        return "\n".join(lines) if lines else "(no matching environment variables found)"

    # ------------------------------------------------------------------ #
    # Level 2 — log streams
    # ------------------------------------------------------------------ #

    @mcp.resource("logs://workspace/recent")
    def get_workspace_recent_logs() -> str:
        """Tail common *.log files under the workspace (root, logs/, data/)."""
        candidates: list[Path] = []
        for pattern in ("*.log", "logs/*.log", ".logs/*.log", "data/*.log"):
            candidates.extend(WORKSPACE_ROOT.glob(pattern))

        seen: set[Path] = set()
        unique: list[Path] = []
        for path in candidates:
            resolved = path.resolve()
            if resolved not in seen:
                seen.add(resolved)
                unique.append(resolved)

        if not unique:
            return (
                f"No .log files found under {WORKSPACE_ROOT}. "
                "Drop logs into logs/ or the project root to expose them here."
            )

        chunks = [_tail_text_file(path) for path in unique[:10]]
        return "\n\n---\n\n".join(chunks)

    # ------------------------------------------------------------------ #
    # Level 3 — project config (extend here next)
    # ------------------------------------------------------------------ #

    @mcp.resource("config://mcp.json")
    def get_mcp_config() -> str:
        """Expose .cursor/mcp.json (read-only)."""
        config_path = WORKSPACE_ROOT / ".cursor" / "mcp.json"
        if not config_path.exists():
            return f"(missing: {config_path})"
        return config_path.read_text(encoding="utf-8")

    @mcp.resource("config://requirements")
    def get_requirements() -> str:
        """Expose requirements.txt (read-only)."""
        req_path = WORKSPACE_ROOT / "requirements.txt"
        if not req_path.exists():
            return f"(missing: {req_path})"
        return req_path.read_text(encoding="utf-8")
