# MCP workspace — Architecture Guide
#
# Read this first. Then open docs/FEATURES.md for per-method details.
# Feature modules are written at *medium* complexity so reviewers can
# follow Level 1 → Level 2 → Level 3 and add more context progressively.

## 1. Big picture

```
Cursor / MCP client
        │  stdio
        ▼
   server.py                 # creates FastMCP, registers everything
        │
        ├── tools/           # ACTIONS  — agent calls with arguments
        │     ├── __init__.py          # register_all()
        │     ├── dev_utilities.py     # workspace / code helpers
        │     └── data_fetchers.py     # DB / CSV / market pulls
        │
        └── resources/       # DATA     — client reads by URI (no side effects)
              ├── __init__.py          # register_all()
              └── log_readers.py       # env, logs, config snapshots
```

`server.py` is the only process entry point that matters. It:

1. Builds one `FastMCP` instance.
2. Calls `tools.register_all(mcp)`.
3. Calls `resources.register_all(mcp)`.
4. Runs over stdio so Cursor can talk to it (see `.cursor/mcp.json`).

`my_mcp_server.py` is a thin backward-compatible wrapper that imports the same `mcp` object.

---

## 2. Tools vs resources (architectural significance)

| | **Tools** (`tools/`) | **Resources** (`resources/`) |
|---|---|---|
| MCP concept | Callable **actions** | Addressable **data** |
| Decorator | `@mcp.tool()` | `@mcp.resource("scheme://…")` |
| Who decides when | Model / agent chooses to invoke | Client / agent **reads** a URI when it needs context |
| Inputs | Explicit parameters | Usually none (URI identifies the stream) |
| Side effects | Allowed (write file, create DB, HTTP fetch) | Should be **read-only** |
| Typical return | Result of doing something | Snapshot of current state |
| Mental model | “Do X” | “Show me Y” |

### Why split them?

- **Safety boundary** — reviewers and clients can treat resources as safe to browse; tools need more caution (writes, network, SQL).
- **Discovery** — resources show up as stable URIs (`env://system_info`); tools show up as named functions with schemas.
- **Caching / refresh** — resources fit “poll this view”; tools fit “run this operation once”.
- **Growth** — new capabilities land in the folder that matches the contract, so the server stays navigable.

### Decision rule when adding a feature

Put it under **`tools/`** when:

- It needs arguments the caller chooses (`path`, `sql`, `symbols`).
- It **changes** something (write file, create sample DB) or performs a **one-shot fetch/compute**.
- The agent should decide *whether* and *with what inputs* to run it.

Put it under **`resources/`** when:

- It is a **stable view** of the world (env, recent logs, config file contents).
- There are no meaningful parameters beyond the URI.
- Calling it repeatedly should be harmless (read-only).
- You want the client to attach or browse it like a document, not “execute” it.

**Borderline cases**

| Idea | Prefer | Why |
|---|---|---|
| “Read this specific log path” | Tool | Needs a `path` argument |
| “Recent workspace logs” | Resource | Fixed URI, no args |
| “Summarize this CSV” | Tool | Path + nrows are inputs |
| “requirements.txt contents” | Resource | Fixed project file |
| “Fetch AAPL prices” | Tool | Symbols / range are inputs; network side effect |
| “Current system info” | Resource | Snapshot, no args |

---

## 3. What lives in each folder today

### `tools/` — executable actions

| File | Role | Complexity levels |
|---|---|---|
| `dev_utilities.py` | Code analysis, hex audits, local file manipulation | L1 disk/dir → L2 read/write → L3 analyze/hex |
| `data_fetchers.py` | Market / local DB / CSV pulls | L1 SQLite → L2 CSV → L3 market HTTP |

Shared idea: every path helper keeps work **inside `WORKSPACE_ROOT`** so tools cannot wander the whole disk.

### `resources/` — read-only streams

| File | Role | Complexity levels |
|---|---|---|
| `log_readers.py` | System logs, environment, project config | L1 env snapshots → L2 log tails → L3 config files |

Resources never write files and never take free-form paths that escape the intended URI.

---

## 4. How registration works (extendability)

Each feature module exports:

```python
def register(mcp: FastMCP) -> None:
    @mcp.tool()          # or @mcp.resource("…")
    def my_feature(...):
        ...
```

The package `__init__.py` imports modules and calls `register(mcp)`:

```python
# tools/__init__.py
def register_all(mcp):
    dev_utilities.register(mcp)
    data_fetchers.register(mcp)
```

**To add a new feature file**

1. Create `tools/my_feature.py` or `resources/my_feature.py`.
2. Implement `register(mcp)` with medium-complexity Level 1 methods first.
3. Import and call it from the matching `__init__.py` → `register_all`.
4. Document methods in `docs/FEATURES.md`.
5. Restart the MCP server / Cursor so the new tools/resources appear.

You do **not** need to change `server.py` for each new module if `__init__.py` already aggregates them.

---

## 5. Progressive complexity (reviewer-friendly)

Modules are intentionally **medium** complexity—not stubs, not frameworks.

Inside each file, look for section comments:

```text
# Level 1 — basics
# Level 2 — …
# Level 3 — … (extend here next)
```

Suggested growth path for reviewers / contributors:

1. Understand Level 1 methods and the `register()` pattern.
2. Trace how `server.py` → `__init__.py` → `register()` wires them.
3. Add a Level 2 method in the same file (same style, same safety helpers).
4. Only then split a new module when a theme diverges (e.g. `tools/git_helpers.py`).

Avoid dumping advanced options, deep nesting, and rare edge cases into Level 1. Prefer clear happy-path code with a single `try/except` returning an error string.

---

## 6. Runtime wiring

`.cursor/mcp.json` points Cursor at the venv Python and `server.py`:

```json
{
  "mcpServers": {
    "my-first-local-server": {
      "command": "...\\.venv\\Scripts\\python.exe",
      "args": ["...\\server.py"]
    }
  }
}
```

Dependencies: `requirements.txt` (`mcp`, `psutil`, `pandas`).

---

## 7. Related docs

- **[FEATURES.md](FEATURES.md)** — every tool and resource method: purpose, parameters, returns, level.
- Source of truth for behavior: the Python modules under `tools/` and `resources/`.
