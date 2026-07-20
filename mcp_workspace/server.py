"""
MCP workspace server — aggregates tools and resources.

Layout:
  server.py                 # this file (main entry)
  tools/                    # executable actions (extendable)
    dev_utilities.py        # disk, files, light code/hex inspection
    data_fetchers.py        # SQLite, CSV, market snapshot
  resources/                # read-only data streams (extendable)
    log_readers.py          # env, logs, project config
  docs/                     # architecture + per-method feature reference
"""

from __future__ import annotations

from mcp.server.fastmcp import FastMCP

from resources import register_all as register_resources
from tools import register_all as register_tools

mcp = FastMCP("My-First-Local-Server")

register_tools(mcp)
register_resources(mcp)

if __name__ == "__main__":
    mcp.run(transport="stdio")
