"""
Read-only MCP resources — URI-addressable data the client can inspect.

Add a new module under resources/, implement register(mcp), then import it here.
"""

from __future__ import annotations

from mcp.server.fastmcp import FastMCP

from resources import log_readers


def register_all(mcp: FastMCP) -> None:
    """Attach every resource module to the shared FastMCP instance."""
    log_readers.register(mcp)
