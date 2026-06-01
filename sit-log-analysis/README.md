# SIT Log Analysis Toolkit

Download Service/API logs from SIT, normalize to JSONL, run Git Bash analytics, and maintain **Splunk SPL** sidecars for UAT/PROD.

| Doc | Path |
|-----|------|
| Plan | `docs/SIT_LOG_ANALYSIS_PLAN.md` |
| Implementation design | `docs/review/SIT_LOG_ANALYSIS_IMPLEMENTATION.md` |

## Quick start (offline demo)

Requires **Git Bash** (or CMD) and **Python 3** on PATH (stdlib only — **no jq** required).

```bash
cd sit-log-analysis
bash scripts/sit-log-analysis.sh pipeline --from-fixtures
```

Results under `LOCAL_ROOT` from config (default `/c/LogAnalysis/sit/<date>/payments-api/out/`).

To run a single trace lookup:

```bash
export TRACE_ID=trace-004
bash scripts/run-query.sh requests-by-trace-id --work-dir /c/LogAnalysis/sit/$(date +%F)/payments-api
```

## Production SIT workflow

1. Copy `config/sit.env.example` → `config/sit.env` and set `SIT_USER`, `SIT_JUMP`, `REMOTE_EXPORT`, `LOCAL_ROOT`.
2. Trigger remote collector (ops-approved script on server):

   ```bash
   bash scripts/trigger-remote-export.sh \
     --since 2026-05-30T00:00:00 --until 2026-05-30T23:59:59 \
     --service payments-api
   ```

3. Download archive:

   ```bash
   bash scripts/fetch-sit-logs.sh
   ```

4. Normalize and run all queries:

   ```bash
   bash scripts/normalize-logs.sh
   bash scripts/run-all-queries.sh
   ```

Or one command: `bash scripts/sit-log-analysis.sh pipeline`

## Query catalog

| ID | Outputs | SPL file |
|----|---------|----------|
| `api-errors-by-status` | CSV + JSON | `queries/api-errors-by-status.spl` |
| `slow-requests` | CSV + JSON (`LATENCY_MS_MIN`, default 1000) | `queries/slow-requests.spl` |
| `requests-by-trace-id` | JSON + TXT (`TRACE_ID` required) | `queries/requests-by-trace-id.spl` |
| `error-timeline` | CSV + JSON | `queries/error-timeline.spl` |
| `top-endpoints-by-volume` | CSV + JSON (`TOP_N`, default 20) | `queries/top-endpoints-by-volume.spl` |

Registry: `config/query-catalog.yaml`

## Python module

Queries and JSONL validation run via stdlib Python (no pip packages):

```bash
cd sit-log-analysis
python python/run_cli.py list-queries
python python/run_cli.py run-query api-errors-by-status \
  --events data/demo-run/normalized/events.jsonl \
  --out-dir data/demo-run/out/api-errors-by-status
```

## Layout

```
sit-log-analysis/
  config/           # sit.env (local), query-catalog.yaml
  scripts/          # fetch, normalize, run-query, orchestrator
  python/           # sit_log_tool package (replaces jq)
  queries/          # *.local.sh + *.spl pairs
  fixtures/sample/  # synthetic data for demo
```

Work directory per pull:

```
<LOCAL_ROOT>/<date>/<service>/
  raw/              # downloaded logs
  normalized/       # events.jsonl
  out/<query-id>/   # CSV, JSON, TXT results
```

## Windows

```bat
scripts\run-sit-log-analysis.bat pipeline --from-fixtures
```

## Security

- Do not commit `config/sit.env` or real SIT log exports.
- Use redacted fixtures only in git.
