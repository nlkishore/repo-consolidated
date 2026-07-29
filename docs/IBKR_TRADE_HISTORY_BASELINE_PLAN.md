# IBKR Trade History — Baseline + Incremental Download & Symbol P&L — Plan

**Purpose:** Plan a program that downloads **all buys, sells, and corporate actions** since account open; **baselines** on first run; **refreshes incrementally** thereafter; and produces **analysis-ready** reports (symbol-wise bought/sold + **P&L by symbol**).

**Status:** P1 + P2 implemented in `C:\Investment\IBKR-Flex-BuySell\trade_history\`  
**Traceability:** [Prompts-investment.txt](collected_prompt_usecases/Prompts-investment.txt) (lines 23–26)  
**Primary reuse:** `C:\Investment\IBKR-Flex-BuySell\` (already downloads Buy/Sell with yearly windows + cache)  
**Related:** Completely Sold alert / Excel sheets; `IBKR-Transaction\` Activity CSVs  

**Design (detailed):** [review/IBKR_TRADE_HISTORY_BASELINE_DESIGN.md](review/IBKR_TRADE_HISTORY_BASELINE_DESIGN.md)

**Run:**
```text
cd C:\Investment\IBKR-Flex-BuySell
python -m trade_history baseline --offline   # or without --offline for Flex download
python -m trade_history refresh
python -m trade_history status
```

---

## 1. Problem statement

| Need | Description |
|------|-------------|
| Full history | Buy + Sell + **corporate actions** (dividends, splits, mergers, spin-offs, etc.) since **account opened** |
| First run | Build a durable **baseline** store (not only one Excel file) |
| Later runs | **Incremental** refresh — fetch only new date windows / new activity; merge + dedupe |
| Analysis | Symbol-wise **bought vs sold**, and **P&L summary by symbol** (closed + open positions) |

### What already exists (do not rebuild from scratch)

| Capability | Location | Coverage |
|------------|----------|----------|
| Flex Web Service download (≤366-day windows) | `IBKR-Flex-BuySell/flex_client.py` | Buy/Sell trades |
| Cached window CSVs + skip/retry | `downloads/flex_{queryId}_{from}_{to}.csv` | Incremental by window |
| YTD sync from Activity CSV | `flex_ytd.py` / `--sync-ytd` | Current calendar year gaps |
| Excel: All / Buys / Sells / Completely_Sold / Still_Holding | `flex_buysell_report.py` | Trade P&L for **fully closed** and open net qty |
| Manual baseline compare (YTD) | `downloads/manual_baseline_2026_ytd.csv` | Validation only |

### Gaps (this prompt)

| Gap | Detail |
|-----|--------|
| **G1 Corporate actions** | Current Flex pipeline focuses on **trades**; no first-class **Corporate Actions** / dividend cash sheets |
| **G2 Explicit baseline command** | Cache exists, but no named **`baseline`** artifact + state file (“account history locked through date D”) |
| **G3 Explicit `refresh`** | Need a single CLI: first time → baseline; later → incremental from `last_watermark` |
| **G4 Symbol P&L for all symbols** | `Completely_Sold` = closed only; need **Symbol_PnL** for every traded symbol (realized + unrealized + dividends) |
| **G5 Account-open start** | Config uses `start_year` / `from_date` (default 2020); should support **account_open_date** |

---

## 2. Goals and non-goals

### Goals

| # | Goal |
|---|------|
| G1 | One CLI: `baseline` (full since account open) and `refresh` (incremental from watermark) |
| G2 | Persist baseline + append-only / mergeable store under `data/` (CSV/Parquet + Excel views) |
| G3 | Download **Trades** and **Corporate Actions** (via extended Flex Query or Activity CSV sections) |
| G4 | Emit analysis workbooks: trades, corporate actions, **By_Symbol** buys/sells, **Symbol_PnL** |
| G5 | Reuse existing Flex client, window cache, Activity merge, and parse utilities |
| G6 | Config/env for token, query IDs, account open date — no secrets in code |
| G7 | Structured logs: windows fetched, rows added, watermark advanced |

### Non-goals (v1)

| # | Non-goal |
|---|----------|
| N1 | Live streaming / TWS API (Flex + Activity CSV only) |
| N2 | Tax lot matching / IRS Form 8949 (document as future) |
| N3 | Replacing CompletelySoldAlert (consumes Excel; unchanged) |
| N4 | Multi-account portfolio UI |

---

## 3. Recommended approach

### Extend `IBKR-Flex-BuySell` (recommended)

```text
First run:
  trade_history baseline
      → Flex windows: account_open → today (trades + corporate actions)
      → Merge Activity CSV gaps
      → Write data/baseline/ + state.json (watermark=today)
      → Write reports/IBKR_TradeHistory.xlsx (analysis sheets)

Later:
  trade_history refresh
      → Fetch only (watermark − overlap_days) → today
      → Dedupe merge into store
      → Advance watermark
      → Rebuild analysis Excel from full store
```

| Pros | Cons |
|------|------|
| Reuses battle-tested download/cache/parse | Flex Query must be updated in IBKR portal for corporate actions |
| Fits existing Investment folder | Corporate-action parsing is new work |
| Completely_Sold / alerts keep working | — |

### Alternative (not recommended for v1)

Greenfield project duplicating Flex download — higher cost, same IBKR limits.

---

## 4. Baseline vs incremental (contract)

| Mode | When | Behavior |
|------|------|----------|
| **baseline** | First time, or `--force-rebaseline` | Download full range; replace `data/baseline/`; set watermark |
| **refresh** | Anytime after baseline exists | Download from `watermark − N days` through today; merge; never drop historical rows without explicit rebaseline |

**Watermark file (illustrative):** `data/state.json`

```json
{
  "account_open_date": "2020-01-15",
  "baseline_completed_at": "2026-07-30T08:00:00Z",
  "watermark_date": "2026-07-29",
  "trade_query_id": "...",
  "corporate_query_id": "...",
  "row_counts": { "trades": 12000, "corporate_actions": 400 }
}
```

**Overlap:** default `refresh_overlap_days: 7` to catch late Flex settlements / corrections.

---

## 5. Analysis outputs (required)

| Artifact | Content |
|----------|---------|
| `Trades_All` | All buy/sell since account open |
| `Buys` / `Sells` | Side filters |
| `Corporate_Actions` | Dividends, splits, mergers, etc. |
| `By_Symbol_Trades` | Grouped or filtered view: buys/sells per symbol |
| **`Symbol_PnL`** | Per symbol: buy qty/cost, sell qty/proceeds, **realized P&L**, open qty, **unrealized** (mark-to-market), **dividends received**, net P&L |
| `Report_Info` | Watermark, sources, run mode, timestamps |

---

## 6. Deliverables (phased)

| Phase | Deliverable | Outcome |
|-------|-------------|---------|
| P0 | Plan + design (this + design doc) | Review gate |
| P1 | `baseline` / `refresh` CLI on top of existing Flex trades; `state.json`; Symbol_PnL (trades-only) | Usable without corporate actions |
| P2 | Corporate Actions Flex/Activity parse + sheet + dividend column on Symbol_PnL | Full prompt coverage |
| P3 | Optional Parquet store + notebook-friendly export | Heavier analysis |

---

## 7. Success criteria

- First `baseline` produces Excel with trades since `account_open_date` and non-empty `Symbol_PnL`.  
- Second `refresh` adds only new rows (row count ≥ previous); watermark advances.  
- Symbol with buys+sells shows bought/sold totals and realized P&L.  
- After P2, dividend/corporate action rows appear and affect Symbol_PnL cash component.  
- Secrets remain in `config.ini` / env (gitignored).

---

## 8. Open questions for review

1. **Account open date** — exact date for config? (default keep 2020-01-01 if unknown)  
2. **One Flex Query vs two** — trades + corporate actions in one Activity Flex Query, or separate query IDs?  
   **Recommendation:** one Activity Flex Query with Trades + Corporate Actions + Cash Transactions (Dividends); fallback: second query ID.  
3. **P&L method** — average cost vs FIFO for partial sells?  
   **Recommendation:** v1 **average cost** for realized (matches Completely_Sold simplicity); document FIFO as P3.  
4. Project packaging — extend `IBKR-Flex-BuySell` vs new folder `IBKR-TradeHistory/`?  
   **Recommendation:** extend `IBKR-Flex-BuySell` with `python -m trade_history baseline|refresh`.

---

## 9. Next step

Review this plan, then approve [IBKR_TRADE_HISTORY_BASELINE_DESIGN.md](review/IBKR_TRADE_HISTORY_BASELINE_DESIGN.md) before P1 implementation.
