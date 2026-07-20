"""Backward-compatible entry point. Prefer `server.py`. """

from server import mcp

if __name__ == "__main__":
    mcp.run(transport="stdio")
