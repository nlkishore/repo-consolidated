# SIT Log Analysis — Implementation Design

**Purpose:** Executable toolkit to download SIT logs, normalize to JSONL, run Git Bash analytics, and maintain **SPL sidecars** for UAT/PROD Splunk.

**Parent plan:** [../SIT_LOG_ANALYSIS_PLAN.md](../SIT_LOG_ANALYSIS_PLAN.md)  
**Tool root:** `repo-consolidated/sit-log-analysis/`  
**Status:** Implemented (baseline)

---

## 1. Components

| Component | Path | Role |
|-----------|------|------|
| Config | `config/sit.env.example`, `config/log-inventory.example.yaml`, `config/query-catalog.yaml` | Hosts, paths, query registry (no secrets in repo) |
| Fetch | `scripts/fetch-sit-logs.sh` | `scp`/`sftp` archive from SIT staging; `--from-fixtures` for offline demo |
| Remote export | `scripts/trigger-remote-export.sh` | SSH wrapper to run ops-approved collector on SIT |
| Normalize | `scripts/normalize-logs.sh` | JSONL + Log4j2 pattern → unified `normalized/events.jsonl` |
| Query runner | `scripts/run-query.sh`, `scripts/run-all-queries.sh` | Run one or all catalogued queries |
| Orchestrator | `scripts/sit-log-analysis.sh` | `fetch` \| `normalize` \| `query` \| `run-all` \| `pipeline` |
| Queries | `queries/*.local.sh` + `queries/*.spl` | Local bash + canonical SPL |
| Fixtures | `fixtures/sample/` | Redacted synthetic logs (safe in git) |

---

## 2. Data flow

```
SIT host (sudo collector) → /tmp/export/*.tar.gz
        │ scp (fetch-sit-logs.sh)
        ▼
WORK_DIR/raw/
        │ normalize-logs.sh
        ▼
WORK_DIR/normalized/events.jsonl
        │ run-query.sh / run-all-queries.sh
        ▼
WORK_DIR/out/<query-id>/*.{csv,json,txt}
        │
        └── queries/*.spl  (copy to Splunk UAT when validated)
```

**Default work directory:** `%LOCAL_ROOT%` from `config/sit.env` (e.g. `C:/LogAnalysis/sit/2026-05-30/payments-api`).

---

## 3. Prerequisites (Windows Git Bash)

| Tool | Purpose |
|------|---------|
| Git Bash | Bash, awk, tar, orchestration scripts |
| **Python 3** | JSONL validation and all queries (`python/sit_log_tool`, stdlib only) |
| `ssh` / `scp` | Remote fetch (OpenSSH client) |
| Optional `rg` | Faster grep on large files |

**Note:** `jq` is **not** required (many corporate environments block it). Python replaces jq entirely.

Copy `config/sit.env.example` → `config/sit.env` (gitignored) and set `SIT_USER`, `SIT_JUMP`, `REMOTE_EXPORT`, `LOCAL_ROOT`.

---

## 4. Query catalog (baseline)

| Query ID | Local output | Splunk use case |
|----------|--------------|-----------------|
| `api-errors-by-status` | CSV: status, service, count | Error breakdown dashboard |
| `slow-requests` | CSV: endpoint, latencyMs, traceId | Performance SLA breach |
| `requests-by-trace-id` | JSON lines for one trace | Incident drill-down |
| `error-timeline` | CSV: hour, count | Timechart / alert baseline |
| `top-endpoints-by-volume` | CSV: endpoint, count | Traffic hotspots |

---

## 5. Security

- `config/sit.env` is **not** committed; only `.example`.
- `data/` work directories are gitignored.
- Fixtures use synthetic IDs only.
- Redact PAN/account in `trigger-remote-export.sh` collector (ops-owned on server).

---

## 6. Extension points

1. Add grok patterns in `config/log-inventory.yaml` per sourcetype.
2. Add query: new `queries/foo.local.sh` + `queries/foo.spl` + entry in `query-catalog.yaml`.
3. Phase 4: optional Python `sit_log_query` CLI sharing same JSONL contract.

---

## Document history

| Version | Date | Notes |
|---------|------|-------|
| 1.0 | 2026-06-01 | Initial implementation design |
