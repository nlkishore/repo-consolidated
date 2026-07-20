"""
Dev utilities — executable MCP tools for the local workspace.

Complexity ladder (read top → bottom when reviewing):
  Level 1  Disk / directory basics
  Level 2  Safe text file read & write
  Level 3  Light code / binary inspection (add more here later)
"""

from __future__ import annotations

import ast
import hashlib
import os
from pathlib import Path

import psutil
from mcp.server.fastmcp import FastMCP

WORKSPACE_ROOT = Path(__file__).resolve().parent.parent


def _resolve_safe(path: str) -> Path:
    """Map a relative path into the workspace; reject paths that escape it."""
    candidate = Path(path)
    if not candidate.is_absolute():
        candidate = WORKSPACE_ROOT / candidate
    resolved = candidate.resolve()
    try:
        resolved.relative_to(WORKSPACE_ROOT)
    except ValueError as exc:
        raise ValueError(
            f"Path '{path}' is outside the workspace root ({WORKSPACE_ROOT})."
        ) from exc
    return resolved


def register(mcp: FastMCP) -> None:
    # ------------------------------------------------------------------ #
    # Level 1 — basics
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def check_disk_space(path: str = os.path.abspath(os.sep)) -> str:
        """Report free / total disk space for a filesystem path."""
        try:
            usage = psutil.disk_usage(path)
            free_gb = usage.free / (1024**3)
            total_gb = usage.total / (1024**3)
            return (
                f"Path '{path}' has {free_gb:.2f} GB free "
                f"out of {total_gb:.2f} GB total."
            )
        except Exception as e:
            return f"Error checking path {path}: {e}"

    @mcp.tool()
    def list_workspace_dir(path: str = ".") -> str:
        """List files and folders under a workspace-relative path."""
        try:
            target = _resolve_safe(path)
            if not target.is_dir():
                return f"Not a directory: {target}"

            entries = sorted(
                target.iterdir(),
                key=lambda p: (not p.is_dir(), p.name.lower()),
            )
            if not entries:
                return f"{target} is empty."

            lines = []
            for entry in entries:
                kind = "dir " if entry.is_dir() else "file"
                size = entry.stat().st_size if entry.is_file() else "-"
                lines.append(f"{kind}\t{size}\t{entry.name}")
            return f"Listing of {target}:\n" + "\n".join(lines)
        except Exception as e:
            return f"Error listing '{path}': {e}"

    # ------------------------------------------------------------------ #
    # Level 2 — local file manipulation
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def read_workspace_file(path: str, max_chars: int = 8000) -> str:
        """Read a text file under the workspace (truncated when very long)."""
        try:
            target = _resolve_safe(path)
            text = target.read_text(encoding="utf-8", errors="replace")
            if len(text) > max_chars:
                return (
                    f"{text[:max_chars]}\n\n"
                    f"... truncated ({len(text)} chars total; "
                    f"showing first {max_chars})."
                )
            return text
        except Exception as e:
            return f"Error reading '{path}': {e}"

    @mcp.tool()
    def write_workspace_file(path: str, content: str, overwrite: bool = False) -> str:
        """Write a text file under the workspace. Requires overwrite=True to replace."""
        try:
            target = _resolve_safe(path)
            if target.exists() and not overwrite:
                return (
                    f"Refused: '{target}' already exists. "
                    "Pass overwrite=True to replace it."
                )
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(content, encoding="utf-8")
            return f"Wrote {len(content)} characters to {target}."
        except Exception as e:
            return f"Error writing '{path}': {e}"

    # ------------------------------------------------------------------ #
    # Level 3 — inspection (extend here next)
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def analyze_python_file(path: str) -> str:
        """Count lines and list classes / functions in a workspace Python file."""
        try:
            target = _resolve_safe(path)
            source = target.read_text(encoding="utf-8")
            tree = ast.parse(source, filename=str(target))

            classes = [n.name for n in ast.walk(tree) if isinstance(n, ast.ClassDef)]
            functions = [
                n.name for n in ast.walk(tree) if isinstance(n, ast.FunctionDef)
            ]
            lines = source.splitlines()

            return (
                f"File: {target}\n"
                f"Lines: {len(lines)}\n"
                f"Classes ({len(classes)}): {', '.join(classes) or '(none)'}\n"
                f"Functions ({len(functions)}): {', '.join(functions) or '(none)'}"
            )
        except Exception as e:
            return f"Error analyzing '{path}': {e}"

    @mcp.tool()
    def hex_audit_file(path: str, max_bytes: int = 64) -> str:
        """SHA-256 checksum plus a short hex dump of a workspace file."""
        try:
            target = _resolve_safe(path)
            data = target.read_bytes()
            digest = hashlib.sha256(data).hexdigest()
            slice_ = data[: max(0, max_bytes)]

            rows = []
            for i in range(0, len(slice_), 16):
                chunk = slice_[i : i + 16]
                hex_part = " ".join(f"{b:02x}" for b in chunk)
                ascii_part = "".join(chr(b) if 32 <= b < 127 else "." for b in chunk)
                rows.append(f"{i:08x}  {hex_part:<47}  {ascii_part}")

            dump = "\n".join(rows) if rows else "(empty)"
            return (
                f"File: {target}\n"
                f"Size: {len(data)} bytes\n"
                f"SHA-256: {digest}\n"
                f"Hex dump (first {max_bytes} bytes):\n{dump}"
            )
        except Exception as e:
            return f"Error hex-auditing '{path}': {e}"
