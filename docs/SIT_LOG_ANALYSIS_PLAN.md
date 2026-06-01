# SIT Log Collection & Local Analysis — Plan & Design

**Document purpose:** Plan how to collect Service/API request–response logs from **SIT**, develop and validate **Splunk-style search criteria** locally, and produce repeatable analysis outputs—when SIT has **no Splunk**, **restricted filesystem access**, and only **sudo** for a dedicated user ID.

**Status:** Draft for review  
**Target:** SIT (System Integration Test) environment → analyst Windows workstation  
**Traceability:** [Prompts.txt](collected_prompt_usecases/Prompts.txt) (lines 139–148)

**Implementation (toolkit):**

- Tool: `repo-consolidated/sit-log-analysis/` — fetch, normalize, Git Bash queries, SPL sidecars
- Design: [review/SIT_LOG_ANALYSIS_IMPLEMENTATION.md](review/SIT_LOG_ANALYSIS_IMPLEMENTATION.md)
- Run: `bash scripts/sit-log-analysis.sh pipeline --from-fixtures` (demo) or configure `config/sit.env` for SIT `scp`

---

## 1. Problem statement (reviewed)

| Aspect | Current state |
|--------|----------------|
| **Goal** | Collect application logs from SIT and analyze them (latency, errors, correlation IDs, API payloads, business fields) using search patterns comparable to **Splunk SPL**. |
| **Gap** | **SIT does not have Splunk** (or equivalent centralized log platform). Logs exist on **application server filesystems** only. |
| **Access** | Interactive user cannot run arbitrary Unix tools on log files directly; **sudo** is available only for a **specific service user ID** (not the analyst’s daily account). |
| **Proposal (baseline)** | Copy/download logs to a **local Windows** machine; use **Git Bash** + **Python 3** (stdlib) and saved scripts to emulate Splunk-style filters and field extraction. |

### Refined success criteria

| # | Criterion |
|---|-----------|
| S1 | Reproducible log pull for a defined time window and service set (no ad-hoc `sudo cat` each time). |
| S2 | Splunk queries drafted in SIT can be **validated locally** before UAT/PROD (where Splunk may exist). |
| S3 | Outputs are **structured** (CSV/JSON) for defects, performance, and audit—not only terminal scrollback. |
| S4 | **No production PII** on personal machines beyond policy; redaction or synthetic test data where required. |
| S5 | Runbook documents **who** runs `sudo`, **what** is copied, and **retention** on Windows. |

---

## 2. Constraints and assumptions

### Confirmed constraints

- No Splunk (or ELK) indexer in SIT for ad-hoc search.
- Log paths may be owned by `appuser` / `jboss` / container UID; analyst lacks read without elevation.
- `sudo` is scoped (typically: specific commands or user switch)—not full root shell for developers.
- Corporate network may block direct SCP from laptop → server; jump host or approved file share may be required.

### Assumptions to validate in review

| Item | Question for ops/security |
|------|---------------------------|
| Log format | Single-line JSON, Log4j2 pattern, or multi-line XML/HTTP dumps? |
| Rotation | `log4j` daily roll, size-based, or pod stdout captured to file? |
| Correlation | Is `traceId` / `X-Request-Id` / `messageId` consistent across tiers? |
| Sudo model | `sudo -u logcollector /opt/scripts/collect-logs.sh` vs `sudo cat` per file? |
| Data classification | Are SIT logs still **internal confidential** (mask account numbers)? |
| Export channel | SCP, SFTP, shared NAS, or ticket-attached zip only? |

---

## 3. Recommended approach (phased)

### Phase 0 — Discovery (1–2 days)

1. Inventory **log locations** per tier (API gateway, app server, integration, DB audit if any).
2. Capture **one sample line** per logger (request, response, error) and document field names.
3. Confirm **sudo** recipe with platform team (approved script vs one-off commands).
4. List **Splunk queries** you intend to run in higher environments (saved searches, dashboards, alerts).

Deliverable: `log-inventory.yaml` (paths, patterns, retention, owner).

### Phase 1 — Controlled collection on SIT (baseline proposal, hardened)

**Do not** rely on manual `sudo tail` for every investigation. Prefer a **fixed collector** run under sudo:

```bash
# Example pattern (paths/commands must be approved by ops)
sudo -u <collector_user> /opt/log-export/collect-api-logs.sh \
  --since "2026-05-30T00:00:00" \
  --until "2026-05-30T23:59:59" \
  --service payments-api \
  --out /tmp/export/payments-api-20260530.tar.gz
```

Script responsibilities (implement with platform team):

| Step | Action |
|------|--------|
| 1 | Resolve log files by date glob (e.g. `application.log.2026-05-30*`). |
| 2 | Filter lines matching API/service markers (configurable regex). |
| 3 | Optional: strip or hash PAN/account fields before archive. |
| 4 | Write tarball to **staging directory** readable by analyst or scp user. |
| 5 | Log export metadata (host, window, file list, line counts) to `export.manifest.json`. |

Transfer to Windows:

| Method | When to use |
|--------|-------------|
| `scp` / `sftp` via jump host | Standard; scriptable in Git Bash |
| Shared drive drop | If SCP blocked |
| Internal artifact repo | Large multi-GB pulls |

Local layout (example):

```
C:\LogAnalysis\sit\
  2026-05-30\
    payments-api\
      raw\          # untouched tar extract
      normalized\   # JSONL one-event-per-line
      queries\      # .spl reference + .sh local runners
      out\          # CSV/JSON results
```

### Phase 2 — Normalize once, query many times

Before mimicking Splunk, **normalize** logs to **JSON Lines** (one JSON object per line) where possible:

| Source format | Normalization |
|---------------|---------------|
| JSON per line | Pass-through; validate with Python `json` |
| Log4j2 pattern | Use `grok` patterns (see §6) → JSONL |
| Multi-line stack traces | Stitch with `traceId` or timestamp window rules |
| XML request/response | Extract with `xmlstarlet` or Python `lxml` → JSONL |

Benefits: same **field names** as Splunk `field=value` searches; easier port of SPL to Python query runners.

### Phase 3 — Splunk query development loop

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  SIT export │────▶│  Normalize JSONL  │────▶│  Local query runner  │
│  (tar/zip)  │     │  (grok / Python)  │     │  Git Bash + Python   │
└─────────────┘     └──────────────────┘     └──────────┬──────────┘
                                                        │
                        ┌───────────────────────────────┼───────────────────────────────┐
                        ▼                               ▼                               ▼
                 ┌─────────────┐               ┌─────────────────┐               ┌──────────────┐
                 │  CSV/JSON   │               │  SPL sidecar    │               │  Splunk UAT  │
                 │  reports    │               │  (.spl + .md)   │               │  (validate)  │
                 └─────────────┘               └─────────────────┘               └──────────────┘
```

For each intended Splunk search, maintain a **sidecar file**:

- `queries/payments-errors.spl` — canonical SPL for UAT/PROD.
- `queries/payments-errors.local.sh` — local equivalent.
- `queries/payments-errors.expected.csv` — optional golden sample for regression.

### Phase 4 — Optional automation (if volume grows)

- Python CLI (`sit_log_query`) with Pydantic config, structured logging, redaction hooks—aligned with repo `cursor.md` principles.
- CI-style “log query tests” on checked-in **fixture snippets** (no real SIT data in git).

---

## 4. Options comparison

| Option | Description | Pros | Cons | Verdict |
|--------|-------------|------|------|---------|
| **A. Manual sudo + copy** | Analyst SSH, sudo read, copy files | Fastest first time | Not repeatable; audit gaps; error-prone | **Pilot only** |
| **B. Approved collector script + Windows Git Bash + Python** (this plan) | Scripted export; local Python/awk queries | Repeatable; works without Splunk; maps to SPL | Needs ops to install script; Windows tooling limits | **Recommended baseline** |
| **C. Splunk Universal Forwarder on SIT** | Agent ships logs to corporate Splunk | Best parity with PROD; full SPL in SIT | Infra approval, licensing, firewall | **Best long-term** if policy allows |
| **D. OpenSearch / ELK stack in SIT** | Stand up light stack in SIT K8s/VM | Rich UI, KQL/Lucene | Heavy ops cost; duplicate of Splunk | Only if enterprise standard is ELK not Splunk |
| **E. Central log share + read-only mount** | NFS/SMB mount of rolled logs | No per-pull copy | Still need search stack or download | Good **companion** to B or C |
| **F. Stream to analyst (tail -f over SSH)** | Live debugging | Good for single incident | No Splunk query design; fragile | **Incident only** |
| **G. Desktop Splunk Free / Splunk Trial** | Index locally on Windows | True SPL on laptop | License/compliance; index size | **Optional** for SPL authoring if legal approves **synthetic or redacted** exports only |

**Recommendation:** Pursue **B** immediately for unblock; open architecture decision record (ADR) for **C** (forwarder) or **G** (local Splunk) as enterprise path.

---

## 5. Architecture (baseline: B)

```
┌────────────────────────────────────────────────────────────────────────────┐
│ SIT application hosts                                                       │
│  application.log*  access.log*  integration*.log                           │
└───────────────────────────────┬────────────────────────────────────────────┘
                                │ sudo: approved collect-api-logs.sh
                                ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Staging: /tmp/export/*.tar.gz + manifest.json                               │
└───────────────────────────────┬────────────────────────────────────────────┘
                                │ scp / sftp / share
                                ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Windows workstation (Git Bash + optional Python 3.11+)                      │
│  scripts/fetch-sit-logs.sh → scripts/normalize-logs.sh → scripts/run-query.* │
└───────────────────────────────┬────────────────────────────────────────────┘
                                ▼
                     out/*.csv  out/*.json  query audit log
```

---

## 6. Mapping Splunk concepts to local tools

| Splunk concept | Local equivalent (Git Bash) | Notes |
|----------------|----------------------------|-------|
| `index=` / `source=` | Directory + filename glob | Document in manifest |
| `sourcetype` | File pattern → grok profile | One profile per log type |
| `search foo bar` | `grep -E` / `rg` | Prefer `rg` for speed on Windows |
| `field extraction` | `grok` (Logstash) or Python `json` for JSON | Store grok in repo |
| `stats count by status` | Python `Counter` / `csv` or `awk` | Implemented in `sit_log_tool` |
| `transaction` / `join` | `sort` + `awk` on correlation id | Python clearer for multi-key |
| `timechart` | Python matplotlib or export CSV → Excel | |
| `rex` | `grep -oP` (PCRE) | Git Bash supports `-P` |
| `eval` | `awk` expressions or Python | |
| `lookup` | `join` with CSV reference tables | Keep lookups in `data/` |
| Saved search | `queries/*.sh` + config YAML | Version-controlled |

### Example: error rate by HTTP status (conceptual)

**Splunk (UAT/PROD):**

```spl
index=app sourcetype=api_json status>=400
| stats count by status, service
| sort - count
```

**Local (JSONL + Python):**

```bash
python -m sit_log_tool run-query api-errors-by-status \
  --events normalized/events.jsonl \
  --out-dir out/api-errors-by-status
```

Maintain both in sidecar files so reviewers can diff intent.

---

## 7. Security, compliance, and retention

| Topic | Guideline |
|-------|-----------|
| **PII / account data** | Redact at collection script when possible; never commit raw SIT logs to git. |
| **Storage on laptop** | Encrypt disk (BitLocker); use dedicated `C:\LogAnalysis` with ACL; delete after ticket closed. |
| **Transfer** | Approved jump host only; no personal email/cloud upload. |
| **Sudo** | Least privilege: collector reads only log paths; cannot write app config. |
| **Audit** | Manifest: who exported, when, which hosts, SHA-256 of archive. |
| **Splunk in PROD** | Local analysis is **preparation**; final searches run in governed indexes. |

---

## 8. Deliverables checklist (for sign-off)

| Deliverable | Owner | Status |
|-------------|-------|--------|
| Log inventory (paths, formats, rotation) | App + ops | ☐ |
| Approved `collect-api-logs.sh` (or equivalent) | Platform | ☐ |
| Sudo / export runbook (1 page) | Ops | ☐ |
| Windows folder layout + `fetch-sit-logs.sh` | Analyst/dev | ☐ |
| Grok / Python normalization profiles | Dev | ☐ |
| Query catalog: `.spl` + `.local.sh` pairs | Dev/QA | ☐ |
| Sample redacted fixture for query tests | QA | ☐ |
| ADR: forwarder vs local-only long term | Architect | ☐ |

---

## 9. Risks and mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Multi-line logs break `grep` | Missing errors | Normalize to JSONL first; use log stitcher |
| Clock skew across hosts | Wrong `transaction` | Use correlation ID; NTP audit |
| Huge exports (>10 GB) | Laptop disk/time | Time-bound export; service filter; sample mode |
| SPL ↔ local drift | Wrong PROD searches | Sidecar pairs + golden fixtures |
| Sudo script change without notice | Broken pulls | Versioned script in config management |
| Git Bash performance | Slow on GB files | Use `rg`, Python streaming, or WSL2 |

---

## 10. Better options summary (executive)

1. **Short term (weeks):** Approved **collector script** + **JSONL normalization** + **Git Bash / Python** query pack with **SPL sidecars** — unblocks SIT without Splunk.
2. **Medium term (quarter):** **Splunk Universal Forwarder** (or approved agent) from SIT to non-prod index — best fidelity for SPL and alerts.
3. **Alternative medium term:** If Splunk licensing blocks SIT, **read-only log share** + **OpenSearch** in lab VPC (only if enterprise aligns).
4. **SPL authoring aid:** **Redacted** exports indexed in **Splunk Free on VM** (not laptop) if compliance allows — optional.
5. **Avoid as primary process:** Repeated manual `sudo cat` / full log copy without manifest, retention, or field normalization.

---

## 11. Suggested next steps (review meeting agenda)

1. Confirm log formats and provide 5–10 **redacted** sample lines per service.
2. Platform: implement or adopt **collector script** + staging path; document sudo line.
3. Security: approve Windows storage path and max retention days.
4. QA/Dev: pick **3 priority Splunk searches**; implement local pairs as proof of concept.
5. Architecture: decide ADR for **forwarder to SIT/non-prod Splunk** vs local-only.

---

## 12. Appendix — minimal script stubs (illustrative)

> Replace paths, users, and host names with values from your `log-inventory.yaml`. Submit collector script through normal change control.

**`fetch-sit-logs.sh` (Git Bash on Windows):**

```bash
#!/usr/bin/env bash
set -euo pipefail
SIT_JUMP="${SIT_JUMP:-jump.example.com}"
REMOTE_EXPORT="/tmp/export/latest.tar.gz"
LOCAL_DIR="${LOCAL_DIR:-/c/LogAnalysis/sit/$(date +%F)}"
mkdir -p "$LOCAL_DIR"
scp "${SIT_USER}@${SIT_JUMP}:${REMOTE_EXPORT}" "$LOCAL_DIR/"
tar -xzf "$LOCAL_DIR/$(basename "$REMOTE_EXPORT)" -C "$LOCAL_DIR/raw"
```

**`config/query-catalog.yaml` (example):**

```yaml
queries:
  - id: api-errors-by-status
    spl_file: queries/api-errors-by-status.spl
    local_runner: queries/api-errors-by-status.local.sh
    input_glob: "normalized/*api*.jsonl"
    description: Count >=400 responses by status and service
```

---

## Document history

| Version | Date | Author | Notes |
|---------|------|--------|-------|
| 0.1 | 2026-06-01 | Draft | Initial plan from Prompts.txt review |
