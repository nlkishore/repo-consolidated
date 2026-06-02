# Completely Sold Price Alert — Plan

**Purpose:** Plan a new Investment feature that monitors **fully closed positions** (Completely_Sold), sends **WhatsApp** alerts when **current market price falls 5% or more below last sold price**, and **refreshes** the sold list when data is older than **24 hours** — using **LangGraph** for orchestration and decisioning.

**Status:** Review decisions incorporated — ready for implementation  
**Project location:** `C:\Investment\CompletelySoldAlert\` (standalone; LangChain + LangGraph isolated from other Investment scripts)
**Traceability:** [Prompts.txt](collected_prompt_usecases/Prompts.txt) (lines 155–162)  
**Design (detailed):** [review/COMPLETELY_SOLD_PRICE_ALERT_DESIGN.md](review/COMPLETELY_SOLD_PRICE_ALERT_DESIGN.md)

---

## 1. Problem statement (reviewed)

| Existing capability | Location | What it does today |
|---------------------|----------|-------------------|
| **Completely sold list** | `C:\Investment\IBKR-Flex-BuySell\` | Flex/Activity merge → Excel `Completely_Sold` sheet: `Last_Sold_Price`, `Current_Market_Price`, `Change_Since_Last_Sold_Pct` |
| **WhatsApp notification** | `C:\Investment\AlertApp\`, root `stock_whatsapp_monitor.py`, `AutomatedTrading\` | Green API / CallMeBot / Twilio — separate watchlists, not tied to Completely_Sold |

| Gap | Description |
|-----|-------------|
| **G1** | No automated alert when a **sold** symbol’s price drops **≥ 5%** vs **last sold price** (re-entry / “sold too early” signal). |
| **G2** | No scheduled **staleness check** — sold list and market prices may be stale; need **re-fetch after 24h** before alerting. |
| **G3** | No unified **decision workflow** (refresh → evaluate → dedupe → notify) — manual Excel review only. |

### Alert rule (clarified)

Use the same metric as IBKR-Flex-BuySell:

```text
Change_Since_Last_Sold_Pct = (Current_Market_Price / Last_Sold_Price - 1) × 100
```

**Notify when:** `Change_Since_Last_Sold_Pct <= alert.price_drop_threshold_pct`  
(i.e. current price is at or below the configured % drop vs last sold price.)

**Threshold source:** `config/settings.yaml` → `alert.price_drop_threshold_pct` (default `-5.0`).  
No hardcoded percentage in code; change the config file to tune sensitivity (e.g. `-3.0`, `-7.5`).

---

## 2. Goals and non-goals

### Goals

| # | Goal |
|---|------|
| G1 | Orchestrate: freshness check → optional refresh → load Completely_Sold → evaluate → WhatsApp |
| G2 | Re-run **IBKR Flex buy/sell report** (or equivalent) when last successful export is **> 24 hours** |
| G3 | LangGraph **conditional edges** for decisioning (stale data, no symbols, cooldown, send/skip) |
| G4 | **One WhatsApp digest** per run — readable layout (sections, aligned columns, WhatsApp-safe formatting) |
| G5 | Run **only on US market days** (NYSE calendar — skip weekends and exchange holidays) |
| G6 | **Separate project** `CompletelySoldAlert\` with its own venv and pinned LangChain/LangGraph deps |
| G7 | Reuse Green API pattern from `AlertApp`; config/env for secrets; structured logs |

### Non-goals (v1)

- Replacing `flex_buysell_report.py` logic (wrap/subprocess only)
- IBKR live streaming quotes (Yahoo `yfinance` via existing `market_prices.py` is enough for v1)
- LLM-generated trading advice (optional message formatting only; deterministic alerts by default)
- Mobile app or web UI

---

## 3. Recommended approach

### Option A — LangGraph orchestrator + existing Python tools (recommended)

```
Scheduler (Task Scheduler / cron)
    → completely_sold_alert_runner.py
        → LangGraph: freshness → refresh? → load → evaluate → notify?
        → subprocess: flex_buysell_report.py (if stale)
        → read: Completely_Sold sheet / cached JSON
        → WhatsApp: Green API adapter (shared module)
```

| Pros | Cons |
|------|------|
| Clear decision flow; easy to extend (cooldown, batch digest) | Adds `langgraph` / `langchain-core` dependencies |
| Fits prompt requirement | Slightly more moving parts than a single script |

### Option B — Single Python script (no LangGraph)

| Pros | Cons |
|------|------|
| Minimal deps; fast to ship | Branching logic harder to maintain as rules grow |

### Option C — n8n / Power Automate external workflow

| Pros | Cons |
|------|------|
| Low code | Another platform; harder to version with Investment repo |

**Recommendation:** **Option A** for review alignment with LangChain/LangGraph; keep **deterministic** price math outside the LLM.

---

## 4. Phased delivery

### Phase 0 — Review ✓ (decisions locked)

| # | Decision |
|---|----------|
| 1 | **Price drop %** from `config/settings.yaml` (`alert.price_drop_threshold_pct`) |
| 2 | **Single digest** WhatsApp message with improved readability |
| 3 | **Market days only** (NYSE — no Sat/Sun/holidays) |
| 4 | **Separate project** `C:\Investment\CompletelySoldAlert\` |

### Phase 1 — Core pipeline (MVP)

- Standalone project scaffold + dedicated `.venv` + `requirements.txt` (LangGraph, LangChain-core)
- LangGraph nodes: `check_market_day` → `check_freshness` → `refresh_data` → `load` → `evaluate` → `format_digest` → `send_digest`
- `settings.yaml` for threshold, calendar, WhatsApp, paths
- Subprocess to `IBKR-Flex-BuySell/flex_buysell_report.py` when data &gt; 24h
- CLI: `python -m completely_sold_alert run`

### Phase 2 — Production ops

- Windows Task Scheduler: daily post-market ET on **weekdays** (graph still gates holidays)
- Per-symbol cooldown in digest (suppress repeats within 24h)
- Update `INVESTMENT-PROGRAMS-REFERENCE.md`

### Phase 3 — Optional LLM layer

- LangChain prompt to summarize alert batch (still rule-based trigger)
- Guardrails: no buy/sell recommendations unless explicitly enabled

---

## 5. Data and integration map

| Artifact | Source | Used for |
|----------|--------|----------|
| `reports/IBKR_BuySell_Since_2020.xlsx` | `flex_buysell_report.py` | `Completely_Sold` sheet |
| `Change_Since_Last_Sold_Pct` | `market_prices.enrich_completely_sold_with_market_prices` | Alert predicate |
| `config.ini` | `IBKR-Flex-BuySell\` | Flex token, query ID, report path |
| WhatsApp credentials | `AlertApp` pattern or new `config.ini` section | Send message |
| Freshness | `Report_Info` sheet or `last_export.json` | 24h gate |

**Staleness rule:** If `now - last_successful_export > 24 hours` → run refresh **before** evaluation.

---

## 6. LangGraph role (summary)

| Step | Graph decision |
|------|----------------|
| Market day | not NYSE session day → `END` (log `skipped_non_market_day`) |
| Freshness | `stale` → `refresh_data` → `load`; `fresh` → `load` |
| Load | empty sheet → `END` (log warning) |
| Evaluate | `Change_Since_Last_Sold_Pct <= settings.threshold` → candidates |
| Format digest | build **one** readable WhatsApp body |
| Send | single Green API message; update cooldown store |
| Errors | retry refresh once; then fail with logged state |

Full node/state diagram: see design doc §4.

---

## 7. Configuration (`config/settings.yaml`)

| Key | Example | Description |
|-----|---------|-------------|
| `alert.price_drop_threshold_pct` | `-5.0` | **Primary tuning knob** — notify when change ≤ this % |
| `data.max_age_hours` | `24` | Re-run Flex report if older |
| `data.report_path` | `../IBKR-Flex-BuySell/reports/...xlsx` | Completely_Sold workbook |
| `data.flex_project_dir` | `../IBKR-Flex-BuySell` | Subprocess cwd for refresh |
| `schedule.run_market_days_only` | `true` | **Approved** — skip non-trading days |
| `schedule.market_calendar` | `NYSE` | Exchange for holiday calendar |
| `schedule.timezone` | `America/New_York` | Market-day evaluation TZ |
| `whatsapp.provider` | `green_api` | Green API (AlertApp pattern) |
| `whatsapp.target_phone` | env / yaml | No `+` prefix for Green API |
| `notify.cooldown_hours` | `24` | Per-symbol repeat suppression in digest |
| `notify.mode` | `digest` | **Fixed** — single message only (v1) |

---

## 8. Security and compliance

- No API keys in code; use `config.ini` (gitignored) or environment variables.
- WhatsApp messages contain tickers and prices only — no account numbers.
- Logs: structured JSON lines; no Flex tokens in log output.
- LLM (if enabled): optional local Ollama; no PII in prompts.

---

## 9. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| Flex download fails overnight | Retry once; skip notify if refresh fails (alert in log) |
| Yahoo rate limits / delisted symbols | Skip row; include in summary “N symbols skipped” |
| False alerts on bad `Last_Sold_Price` | Require `Sell_Qty_Total == Buy_Qty_Total`; min trade count |
| Alert fatigue | Per-symbol cooldown + optional daily digest |
| LangGraph version drift | Pin versions in `requirements.txt` |

---

## 10. Separate project rationale

| Principle | How |
|-----------|-----|
| **Isolation** | `CompletelySoldAlert\` is not embedded in `IBKR-Flex-BuySell\` or `AlertApp\` |
| **Own dependencies** | Dedicated `requirements.txt`: `langgraph`, `langchain-core`, `pydantic`, etc. |
| **Own venv** | `CompletelySoldAlert\.venv` — avoids version clashes with Investment root packages |
| **Integration only via adapters** | Subprocess to Flex report; read Excel; Green API send — no shared code merge |
| **Clear ownership** | LangChain/LangGraph orchestration lives only in this project |

---

## 11. Deliverables

| Deliverable | Status |
|-------------|--------|
| Plan document (this file) | ☑ Updated |
| Design document | ☑ Updated |
| Review decisions (config %, digest, market days, separate project) | ☑ Approved |
| `CompletelySoldAlert/` toolkit in repo | ☑ |
| Task Scheduler / runbook | ☐ With implementation |

---

## Document history

| Version | Date | Notes |
|---------|------|-------|
| 0.1 | 2026-06-01 | Initial plan from Prompts.txt |
| 0.2 | 2026-06-01 | Review: config threshold, digest-only, market days, separate project |
