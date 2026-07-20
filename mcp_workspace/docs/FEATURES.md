# MCP workspace — Feature reference

Per-method documentation for every tool and resource registered by this server.
Architecture and “tools vs resources” guidance: see [ARCHITECTURE.md](ARCHITECTURE.md).

Convention in each module:

- **Level 1** — easiest to review; start here
- **Level 2** — still medium complexity; builds on Level 1 patterns
- **Level 3** — slightly richer; good place to add the *next* feature

Helpers that are **not** MCP-exposed are listed under “Internal helpers”.

---

## `tools/dev_utilities.py`

Theme: code analysis, hex audits, local file manipulation (workspace-scoped).

### Internal helpers

#### `_resolve_safe(path: str) -> Path`

| | |
|---|---|
| **What it does** | Turns a relative path into an absolute path under the project root. Rejects paths that resolve outside `WORKSPACE_ROOT`. |
| **Why it exists** | Shared safety gate for every file tool so callers cannot read/write arbitrary system paths. |
| **Not an MCP tool** | Called only by other functions in this module. |

---

### Level 1 — basics

#### `check_disk_space(path: str = <system root>) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Uses `psutil.disk_usage` to report free and total space in GB for a filesystem path. |
| **Parameters** | `path` — filesystem path to measure (default: OS root, e.g. `C:\` or `/`). |
| **Returns** | Human-readable free/total string, or an error message. |
| **Side effects** | None (read-only OS query). |
| **Notes** | Unlike other tools here, this is *not* limited to the workspace; it measures any mount path `psutil` can see. |

#### `list_workspace_dir(path: str = ".") -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Lists directories and files under a workspace-relative path. |
| **Parameters** | `path` — relative to workspace (or absolute but still inside workspace). Default `"."`. |
| **Returns** | Tab-separated lines: `kind`, size (or `-` for dirs), name. |
| **Side effects** | None. |
| **Errors** | Outside workspace, missing path, or not a directory → error string. |

---

### Level 2 — local file manipulation

#### `read_workspace_file(path: str, max_chars: int = 8000) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Reads a UTF-8 text file under the workspace. Truncates if longer than `max_chars`. |
| **Parameters** | `path` — workspace file; `max_chars` — maximum characters to return. |
| **Returns** | File text, optionally with a truncation footer. |
| **Side effects** | None. |

#### `write_workspace_file(path: str, content: str, overwrite: bool = False) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Writes `content` to a workspace path. Creates parent folders if needed. |
| **Parameters** | `path`, `content`, `overwrite` — must be `True` to replace an existing file. |
| **Returns** | Confirmation with character count, or a refusal if the file exists and `overwrite` is false. |
| **Side effects** | Creates/overwrites a file on disk. |

---

### Level 3 — inspection

#### `analyze_python_file(path: str) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Parses a `.py` file with the `ast` module and reports line count, class names, and function names. |
| **Parameters** | `path` — Python source under the workspace. |
| **Returns** | Short multi-line summary. |
| **Side effects** | None. |
| **Extend next** | Async defs, import counts, complexity metrics, etc. |

#### `hex_audit_file(path: str, max_bytes: int = 64) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Computes SHA-256 of the whole file and prints a hex+ASCII dump of the first `max_bytes`. |
| **Parameters** | `path`, `max_bytes` — dump length (default 64 for readable output). |
| **Returns** | Size, checksum, and hex dump block. |
| **Side effects** | None. |
| **Extend next** | Offset parameter, binary-type heuristics, compare two digests. |

---

## `tools/data_fetchers.py`

Theme: fetching market data or pulling from local databases / CSVs.

### Internal helpers

#### `_resolve_under_workspace(path: str) -> Path`

| | |
|---|---|
| **What it does** | Same workspace confinement as `_resolve_safe` in `dev_utilities`. |
| **Not an MCP tool** | Used by SQLite and CSV tools. |

---

### Level 1 — local SQLite

#### `ensure_sample_sqlite() -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Ensures `data/sample.db` exists with a `prices` table and a few demo rows. |
| **Parameters** | None. |
| **Returns** | Path to the DB and a sample `SELECT` hint. |
| **Side effects** | Creates `data/` and/or inserts seed rows if the table is empty. |
| **Why it exists** | Lets reviewers try `query_local_sqlite` without bringing their own database. |

#### `query_local_sqlite(db_path: str, sql: str, limit: int = 50) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Runs a read-only SQL query against a SQLite file under the workspace; returns a pandas text table. |
| **Parameters** | `db_path` — DB file; `sql` — must start with `SELECT`, `WITH`, or `PRAGMA`; `limit` — max rows shown (capped at 500). |
| **Returns** | Table as text, `(no rows)`, truncation note, or error. |
| **Side effects** | Opens DB in read-only URI mode (`mode=ro`). Does not modify data. |
| **Safety** | Non-SELECT statements are rejected in this medium-complexity version. |

---

### Level 2 — local tabular files

#### `summarize_csv(path: str, nrows: int = 20) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Loads a CSV with pandas and reports shape, column dtypes, and `head(nrows)`. |
| **Parameters** | `path` — workspace CSV; `nrows` — preview length (capped at 100). |
| **Returns** | Multi-line summary + preview table. |
| **Side effects** | None (read-only). |

---

### Level 3 — network fetch

#### `fetch_market_snapshot(symbols: str = "AAPL,MSFT", range_days: int = 5) -> str`

| | |
|---|---|
| **MCP kind** | Tool |
| **What it does** | Calls Yahoo Finance chart API for each comma-separated ticker and returns recent daily closes as a table. |
| **Parameters** | `symbols` — e.g. `"AAPL,MSFT"`; `range_days` — 1–90. |
| **Returns** | Combined pandas table (`symbol`, `date`, `close`); per-ticker `error` column on failure. |
| **Side effects** | Outbound HTTPS requests. |
| **Extend next** | Volumes, caching under `data/`, alternate providers. |

---

## `resources/log_readers.py`

Theme: exposing streaming system logs and environment / project configuration (read-only).

### Internal helpers

#### `_tail_text_file(path: Path, max_lines: int = 80) -> str`

| | |
|---|---|
| **What it does** | Reads a text file and returns the last `max_lines` with a header noting how many lines were shown. |
| **Not an MCP resource** | Used by `get_workspace_recent_logs`. |

---

### Level 1 — environment snapshots

#### `get_system_info()` — URI `env://system_info`

| | |
|---|---|
| **MCP kind** | Resource |
| **What it does** | Returns OS name, platform string, Python version, current working directory, and workspace root. |
| **Parameters** | None (URI only). |
| **Returns** | Multi-line plain text. |
| **Side effects** | None. |

#### `get_environment_config()` — URI `env://environment`

| | |
|---|---|
| **MCP kind** | Resource |
| **What it does** | Lists a **whitelist** of non-secret environment variables (`PATH`, `VIRTUAL_ENV`, `USERNAME`, etc.). |
| **Parameters** | None. |
| **Returns** | `KEY=value` lines, or a message if none matched. |
| **Side effects** | None. |
| **Safety** | Intentionally does **not** dump the full environment (avoids leaking tokens/secrets). |

---

### Level 2 — log streams

#### `get_workspace_recent_logs()` — URI `logs://workspace/recent`

| | |
|---|---|
| **MCP kind** | Resource |
| **What it does** | Finds `*.log` under the workspace root, `logs/`, `.logs/`, and `data/`; tails up to 10 files. |
| **Parameters** | None. |
| **Returns** | Concatenated tails separated by `---`, or guidance if no logs exist. |
| **Side effects** | None. |
| **How to try it** | Drop a `.log` file into `logs/` and re-read the resource. |

---

### Level 3 — project config

#### `get_mcp_config()` — URI `config://mcp.json`

| | |
|---|---|
| **MCP kind** | Resource |
| **What it does** | Returns the contents of `.cursor/mcp.json` so clients can inspect how this server is launched. |
| **Parameters** | None. |
| **Returns** | File text, or a missing-file message. |
| **Side effects** | None. |

#### `get_requirements()` — URI `config://requirements`

| | |
|---|---|
| **MCP kind** | Resource |
| **What it does** | Returns `requirements.txt` for dependency inspection. |
| **Parameters** | None. |
| **Returns** | File text, or a missing-file message. |
| **Side effects** | None. |
| **Extend next** | `config://pyproject`, pinned lockfile views, runtime package versions. |

---

## Package aggregators

### `tools/__init__.py` → `register_all(mcp)`

| | |
|---|---|
| **What it does** | Calls `dev_utilities.register(mcp)` then `data_fetchers.register(mcp)`. |
| **When to edit** | After adding a new module under `tools/`. |

### `resources/__init__.py` → `register_all(mcp)`

| | |
|---|---|
| **What it does** | Calls `log_readers.register(mcp)`. |
| **When to edit** | After adding a new module under `resources/`. |

### `server.py`

| | |
|---|---|
| **What it does** | Creates `FastMCP("My-First-Local-Server")`, registers tools and resources, runs stdio transport. |
| **Methods** | No feature methods; only aggregation + `mcp.run(...)`. |

---

## Quick “where should my new feature go?” checklist

1. Needs arguments or performs an action? → **`tools/<theme>.py`**
2. Fixed URI, read-only snapshot? → **`resources/<theme>.py`**
3. Start at **Level 1** in that file; document the method in this file; register via `__init__.py`.
