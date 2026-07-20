"""
Executable MCP tools — side-effecting or parameterized actions.

Add a new module under tools/, implement register(mcp), then import it here.
"""

from __future__ import annotations

from mcp.server.fastmcp import FastMCP

from tools import data_fetchers, dev_utilities


def register_all(mcp: FastMCP) -> None:
    """Attach every tool module to the shared FastMCP instance."""
    # Order is intentional: basics first, then data fetchers.
    dev_utilities.register(mcp)
    data_fetchers.register(mcp)
