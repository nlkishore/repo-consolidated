# IBKR Trade History — Baseline + Incremental & Symbol P&L — Design

**Purpose:** Technical design for baseline/incremental download of **buys, sells, and corporate actions**, plus **symbol-wise bought/sold** and **P&L-by-symbol** analysis outputs.

**Parent plan:** [../IBKR_TRADE_HISTORY_BASELINE_PLAN.md](../IBKR_TRADE_HISTORY_BASELINE_PLAN.md)  
**Traceability:** [Prompts-investment.txt](../collected_prompt_usecases/Prompts-investment.txt) (lines 23–26)  
**Status:** P1 + P2 implemented — `C:\Investment\IBKR-Flex-BuySell\trade_history\` (`python -m trade_history`)  
**Code home:** `C:\Investment\IBKR-Flex-BuySell\`  

---

## 1. Context and actors

| Actor | Role |
|-------|------|
| Investor / analyst | Runs `baseline` once, then `refresh` as needed; opens Excel for analysis |
| IBKR Flex Web Service | Source of trade / corporate-action statements |
| Existing CompletelySoldAlert | Continues to read Completely_Sold sheet (unchanged contract) |

---

## 2. Architecture

```text
┌─────────────────────────────────────────────────────────┐
│ CLI: python -m trade_history baseline | refresh | status │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│ Orchestrator                                             │
│  - load config + state.json                              │
│  - resolve date range (full vs watermark−overlap→today)  │
│  - invoke downloaders                                    │
│  - merge/dedupe into store                               │
│  - rebuild analysis workbook                             │
│  - advance watermark                                     │
└───────┬─────────────────────────────┬───────────────────┘
        │                             │
┌───────▼──────────┐         ┌────────▼────────────┐
│ TradeDownloader  │         │ CorporateDownloader │
│ (reuse flex_* )  │         │ (new / extended)    │
└───────┬──────────┘         └────────┬────────────┘
        │                             │
        └─────────────┬───────────────┘
                      ▼
              data/store/ (trades.parquet|csv,
                           corporate_actions.csv)
                      ▼
              reports/IBKR_TradeHistory.xlsx
```

### Layers

| Layer | Responsibility |
|-------|----------------|
| CLI | argparse; exit codes; no business logic |
| Orchestrator | Mode, watermark, merge, export |
| Adapters | Flex API, Activity CSV, Yahoo marks (existing `market_prices`) |
| Analytics | Symbol_PnL, By_Symbol aggregations |
| Config | `config.ini` / env — tokens, query IDs, account_open_date |

---

## 3. Data model

### 3.1 Trade row (normalized)

| Field | Notes |
|-------|-------|
| Date | Trade date |
| Symbol | Uppercase |
| Side | BUY / SELL |
| Quantity | Absolute shares |
| Price | Execution |
| Proceeds / Net Amount | Signed cash if available |
| Commission | Optional |
| Source | `flex` \| `activity` |
| SourceFile | Cache path |
| RowKey | Dedupe key (date, symbol, side, qty, price, net) — reuse `flex_ytd.trade_row_key` |

### 3.2 Corporate action row (normalized)

| Field | Notes |
|-------|-------|
| Date | Ex-date or pay date (document which; prefer Ex-Date + Pay-Date columns) |
| Symbol | |
| ActionType | DIVIDEND, SPLIT, MERGER, SPINOFF, RIGHTS, OTHER |
| Quantity / Ratio | Split ratio or share delta when present |
| Amount | Cash (dividends) |
| Description | Raw IBKR text |
| Source / SourceFile / RowKey | Same pattern as trades |

### 3.3 Symbol_PnL row

| Column | Formula / meaning (v1 average-cost) |
|--------|-------------------------------------|
| Symbol | |
| Buy_Qty / Sell_Qty | Sum of buys / sells |
| Buy_Cost / Sell_Proceeds | Cash totals (incl. commission if available) |
| Open_Qty | Buy_Qty − Sell_Qty |
| Realized_PnL | For closed lots under avg-cost: proceeds − cost allocated to sold qty |
| Dividends | Sum of DIVIDEND Amount (P2) |
| Mark_Price | Yahoo last (open positions) |
| Unrealized_PnL | Open_Qty × Mark − remaining cost basis (if Open_Qty ≠ 0) |
| Total_PnL | Realized_PnL + Unrealized_PnL + Dividends |
| First_Buy_Date / Last_Sell_Date | |

Align closed-symbol Realized_PnL with existing `Completely_Sold.Profit` where net qty = 0 (regression test).

---

## 4. Baseline / refresh algorithm

### 4.1 `baseline`

1. Require `account_open_date` (or `from_date` / `start_year`).  
2. Delete or archive previous `data/store/` only if `--force-rebaseline`.  
3. Download Flex windows from open date → today (existing yearly splitter).  
4. Optionally fill gaps from `IBKR-Transaction\Latest\`.  
5. Write full trade store (+ corporate store when available).  
6. Build Excel analysis.  
7. Write `data/state.json` with watermark = max trade/action date (or today UTC date).

### 4.2 `refresh`

1. Fail if no `state.json` (instruct user to run `baseline`).  
2. `from = watermark_date − refresh_overlap_days`.  
3. Download only windows overlapping `[from, today]`.  
4. Merge into store with RowKey dedupe (keep newest source preference: activity over stale flex if configured).  
5. Rebuild Excel from **entire** store (not delta-only).  
6. Advance watermark to max(date) in store.

### 4.3 Idempotency

Re-running `refresh` the same day must not duplicate rows (RowKey uniqueness).

---

## 5. Corporate actions — how to obtain (P2)

### IBKR portal setup (user action)

Activity Flex Query should include sections such as:

- Trades (or Trades + Transaction History)  
- **Corporate Actions**  
- **Cash Transactions** (filter Dividends) if dividends not under Corporate Actions  

Document exact Flex Query checklist in README during implementation.

### Parsing strategy

| Source | Approach |
|--------|----------|
| Flex CSV multi-section | Detect section headers; route to `parse_corporate_actions` |
| Activity Statement CSV | Map known IBKR “Corporate Actions” / “Dividends” blocks (similar to `parse_activity_trades`) |
| Fallback | Manual drop of corporate CSV into `downloads/corporate/` |

If corporate sections missing, P1 still ships; P2 logs warning: “Corporate Actions empty — update Flex Query”.

---

## 6. CLI contract

```text
cd C:\Investment\IBKR-Flex-BuySell
python -m trade_history baseline
python -m trade_history refresh
python -m trade_history status
python -m trade_history baseline --force-rebaseline
python -m trade_history export --out reports\IBKR_TradeHistory.xlsx
```

**Exit codes:** `0` ok; `2` missing baseline/config; `3` Flex/API failure; `4` I/O.

**Config additions (`config.ini`):**

```ini
[history]
account_open_date = 2020-01-15
refresh_overlap_days = 7
store_dir = data/store
state_path = data/state.json
report_path = reports/IBKR_TradeHistory.xlsx
# optional second Flex query for corporate-only
corporate_query_id =
```

Secrets stay under existing `[flex] token` / `query_id`.

---

## 7. Excel workbook layout

| Sheet | Content |
|-------|---------|
| Report_Info | mode, watermark, account_open_date, source files, counts |
| Trades_All | Normalized trades |
| Buys / Sells | Filters |
| Corporate_Actions | P2; empty stub in P1 with note |
| By_Symbol_Trades | Pivot-friendly: Symbol, Side, Qty, Amount, TradeCount |
| Symbol_PnL | Primary analysis sheet |
| Completely_Sold | Keep generating (compat with CompletelySoldAlert) |
| Still_Holding | Keep generating |

Also keep writing legacy `reports/IBKR_BuySell_Since_2020.xlsx` **or** make TradeHistory the canonical path and point CompletelySoldAlert config at the new Completely_Sold sheet — decide at implementation (prefer **single workbook** + update alert config example).

---

## 8. Mapping to existing modules

| Existing | Role in new design |
|----------|--------------------|
| `flex_client.download_flex_report` | Unchanged |
| `flex_buysell_report` window loop | Extract shared `WindowDownloader` used by orchestrator |
| `flex_parse.merge_trade_frames` | Store merge |
| `flex_ytd.sync_ytd` | Optional path inside `refresh` for current year |
| `build_completely_sold_summary` | Feed Symbol_PnL closed rows / keep sheet |
| `market_prices.enrich_*` | Unrealized marks on Symbol_PnL |

New files (illustrative):

```text
IBKR-Flex-BuySell/
  trade_history/
    __main__.py
    cli.py
    orchestrator.py
    state.py
    store.py
    symbol_pnl.py
    corporate_parse.py   # P2
```

---

## 9. Security & observability

- No tokens in logs or Excel Report_Info.  
- Structured log lines: `mode=refresh windows=2 trades_added=15 watermark=2026-07-30`.  
- Read-only toward IBKR; local writes under `data/` and `reports/`.

---

## 10. Testing

| Test | Assert |
|------|--------|
| Fixture trades → Symbol_PnL | Closed symbol Realized_PnL matches hand calc |
| baseline then refresh with overlap | No duplicate RowKeys |
| refresh without state | Exit 2 |
| Completely_Sold vs Symbol_PnL (net=0) | Profit ≈ Realized_PnL |
| Corporate fixture (P2) | Dividends roll into Symbol_PnL.Dividends |

---

## 11. Implementation phases

| Phase | Work |
|-------|------|
| P1 | `trade_history` package; state; baseline/refresh for **trades**; Symbol_PnL + By_Symbol; Excel export; tests |
| P2 | Corporate Flex/Activity parse; Corporate_Actions sheet; dividends in Symbol_PnL |
| P3 | FIFO option; Parquet; optional sync to repo-consolidated |

---

## 12. Review checklist

- [ ] Account open date / start range approved  
- [ ] Avg-cost P&L for v1 accepted  
- [ ] Extend `IBKR-Flex-BuySell` (not new top-level project) accepted  
- [ ] Flex Query will be updated for corporate actions (P2)  
- [ ] Proceed to P1 after sign-off  
