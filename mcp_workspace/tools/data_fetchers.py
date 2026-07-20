"""
Data fetchers — executable MCP tools that pull or summarize data.

Complexity ladder (read top → bottom when reviewing):
  Level 1  Local sample database setup + read-only SQL
  Level 2  CSV summary under the workspace
  Level 3  External market snapshot (network call)
"""

from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

import pandas as pd
from mcp.server.fastmcp import FastMCP

WORKSPACE_ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = WORKSPACE_ROOT / "data"


def _resolve_under_workspace(path: str) -> Path:
    """Resolve a path and keep it inside the workspace root."""
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
    # Level 1 — local SQLite
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def ensure_sample_sqlite() -> str:
        """Create data/sample.db with a small prices table if missing."""
        try:
            DATA_DIR.mkdir(parents=True, exist_ok=True)
            db_path = DATA_DIR / "sample.db"

            with sqlite3.connect(db_path) as conn:
                conn.execute(
                    """
                    CREATE TABLE IF NOT EXISTS prices (
                        symbol TEXT NOT NULL,
                        trade_date TEXT NOT NULL,
                        close REAL NOT NULL
                    )
                    """
                )
                count = conn.execute("SELECT COUNT(*) FROM prices").fetchone()[0]
                if count == 0:
                    conn.executemany(
                        "INSERT INTO prices (symbol, trade_date, close) VALUES (?, ?, ?)",
                        [
                            ("AAPL", "2026-07-15", 214.5),
                            ("AAPL", "2026-07-16", 215.1),
                            ("MSFT", "2026-07-15", 455.2),
                            ("MSFT", "2026-07-16", 456.0),
                        ],
                    )
                    conn.commit()

            return f"Sample database ready at {db_path}. Try: SELECT * FROM prices;"
        except Exception as e:
            return f"Error creating sample database: {e}"

    @mcp.tool()
    def query_local_sqlite(db_path: str, sql: str, limit: int = 50) -> str:
        """Run a read-only SELECT / WITH / PRAGMA against a workspace SQLite DB."""
        first = sql.strip().split(None, 1)[0].upper() if sql.strip() else ""
        if first not in {"SELECT", "WITH", "PRAGMA"}:
            return "Only SELECT, WITH, or PRAGMA statements are allowed."

        try:
            target = _resolve_under_workspace(db_path)
            if not target.exists():
                return f"Database not found: {target}"

            limit = max(1, min(limit, 500))
            uri = f"file:{target.as_posix()}?mode=ro"
            with sqlite3.connect(uri, uri=True) as conn:
                df = pd.read_sql_query(sql, conn)

            if df.empty:
                return "(no rows)"
            if len(df) > limit:
                return (
                    f"{df.head(limit).to_string(index=False)}\n\n"
                    f"... truncated to {limit} of {len(df)} rows."
                )
            return df.to_string(index=False)
        except Exception as e:
            return f"Error querying '{db_path}': {e}"

    # ------------------------------------------------------------------ #
    # Level 2 — local tabular files
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def summarize_csv(path: str, nrows: int = 20) -> str:
        """Load a workspace CSV and return shape, dtypes, and a short preview."""
        try:
            target = _resolve_under_workspace(path)
            df = pd.read_csv(target)
            preview = df.head(max(1, min(nrows, 100)))
            dtypes = "\n".join(f"  {col}: {dtype}" for col, dtype in df.dtypes.items())
            return (
                f"File: {target}\n"
                f"Shape: {df.shape[0]} rows x {df.shape[1]} columns\n"
                f"Dtypes:\n{dtypes}\n\n"
                f"Preview:\n{preview.to_string(index=False)}"
            )
        except Exception as e:
            return f"Error summarizing CSV '{path}': {e}"

    # ------------------------------------------------------------------ #
    # Level 3 — network fetch (extend here next)
    # ------------------------------------------------------------------ #

    @mcp.tool()
    def fetch_market_snapshot(symbols: str = "AAPL,MSFT", range_days: int = 5) -> str:
        """Fetch recent daily closes for tickers via Yahoo Finance chart API."""
        tickers = [s.strip().upper() for s in symbols.split(",") if s.strip()]
        if not tickers:
            return "No symbols provided."

        range_days = max(1, min(range_days, 90))
        frames: list[pd.DataFrame] = []

        for ticker in tickers:
            url = (
                "https://query1.finance.yahoo.com/v8/finance/chart/"
                f"{ticker}?range={range_days}d&interval=1d"
            )
            try:
                req = Request(url, headers={"User-Agent": "mcp-workspace/1.0"})
                with urlopen(req, timeout=15) as resp:
                    payload = json.loads(resp.read().decode("utf-8"))

                result = payload["chart"]["result"][0]
                timestamps = result.get("timestamp") or []
                quote = (result.get("indicators") or {}).get("quote", [{}])[0]
                closes = quote.get("close") or []

                rows = []
                for ts, close in zip(timestamps, closes):
                    if close is None:
                        continue
                    rows.append(
                        {
                            "symbol": ticker,
                            "date": pd.to_datetime(ts, unit="s").date().isoformat(),
                            "close": round(float(close), 4),
                        }
                    )
                frames.append(pd.DataFrame(rows) if rows else pd.DataFrame(
                    [{"symbol": ticker, "date": None, "close": None}]
                ))
            except (HTTPError, URLError, KeyError, IndexError, TypeError, ValueError) as e:
                frames.append(
                    pd.DataFrame(
                        [{"symbol": ticker, "date": None, "close": None, "error": str(e)}]
                    )
                )

        return pd.concat(frames, ignore_index=True).to_string(index=False)
