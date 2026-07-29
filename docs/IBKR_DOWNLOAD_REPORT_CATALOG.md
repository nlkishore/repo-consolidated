# IBKR-Download Report Catalog — Account Statement & Realized Summary

**Purpose:** Inventory the data under `C:\Investment\IBKR-Download\` and catalog **what analysis reports can be generated**, with section→report mapping and notes for implementation.

**Status:** Catalog for review (documentation only; generator implementation optional follow-up)  
**Traceability:** [Prompts-investment.txt](collected_prompt_usecases/Prompts-investment.txt) (lines 35–38)  
**Folders reviewed:**
- `C:\Investment\IBKR-Download\AccountStatement\`
- `C:\Investment\IBKR-Download\RealizedSummary\`

**Related existing tools:**
- `C:\Investment\IBKR-Flex-BuySell\` / `trade_history` — Flex + Activity CSV buy/sell/dividends
- `C:\Investment\IBKR-Transaction\` — Activity TRANSACTIONS / DIVIDEND exports

---

## 1. Data inventory

### 1.1 AccountStatement (calendar / period statements)

| File (examples) | Approx period |
|-----------------|---------------|
| `U3831357_2020_2020.csv` … `U3831357_2025_2025.csv` | Full calendar years 2020–2025 |
| `U3831357_20260101_20260728.csv` | YTD / partial 2026 |

**Format:** IBKR multi-section CSV — each row starts with `SectionName, RowType, …` where `RowType` is typically `Header`, `Data`, `SubTotal`, `Total`, `Notes`.

### 1.2 RealizedSummary (realized / tax-style windows)

| File (examples) | Window pattern |
|-----------------|----------------|
| `U3831357_20200218_20210217.csv` … through `U3831357_20260212_20260728.csv` | Rolling ~1-year windows (filename dates) |

Same multi-section CSV style; overlaps AccountStatement for many cash/trade sections, plus **Forex P/L Details**. Stronger focus on **realized performance** over each window.

### 1.3 Overlap / when to use which

| Need | Prefer |
|------|--------|
| Full-year deposits, interest, SYEP, borrow fees, MTM | **AccountStatement** |
| Realized S/T–L/T P/L by symbol over a custom window | **RealizedSummary** |
| Symbol buy/sell + commissions (order-level) | Either (both have `Trades`) — consolidate carefully (dedupe across years/windows) |
| Dividends / withholding / corporate actions | Either (both have sections) |

**Caution:** Summing **all RealizedSummary files** without dedupe will **double-count** overlapping periods. Prefer AccountStatement year files for lifetime cash totals; use RealizedSummary for period-specific realized P/L.

---

## 2. Sections available (AccountStatement)

Observed in `U3831357_2025_2025.csv` (representative):

| Section | Typical columns (abbrev.) | Volume (2025 file) |
|---------|---------------------------|--------------------|
| Statement / Account Information | Field Name, Field Value | Meta |
| Net Asset Value | Asset Class, Prior/Current totals, Change | Snapshot |
| Change in NAV | Starting/ending NAV, P/L components | Summary |
| Mark-to-Market Performance Summary | Symbol, qty, prices, MTM P/L breakdown | Per symbol |
| Realized & Unrealized Performance Summary | Symbol, S/T & L/T realized/unrealized | Per symbol |
| Cash Report | Currency, Total, Securities, Futures | Cash |
| Open Positions | Symbol, Qty, Cost, Value, Unrealized P/L | Positions |
| Forex Balances | Currency, Qty, Cost, Unrealized | FX |
| Net Stock Position Summary | Symbol, Shares at IB / Borrowed / Lent / Net | Positions + lend |
| **Trades** | Symbol, Date/Time, Qty, T. Price, Proceeds, **Comm/Fee**, Basis, Realized P/L | Orders |
| Transaction Fees | Symbol, Date, Amount | Fee detail |
| **Corporate Actions** | Description, Qty, Proceeds, Value, Realized P/L | Corp events |
| **Deposits & Withdrawals** | Settle Date, Description, **Amount** | Cash movements |
| Fees | Date, Description, Amount | Broker fees |
| **Dividends** | Date, Description, Amount | Income |
| Withholding Tax | Date, Description, Amount | Tax |
| **Interest** | Date, Description, Amount | Credit/debit interest |
| Interest Accruals | Field Name / Value | Accrual snapshot |
| Change in Dividend Accruals | Symbol, Ex/Pay Date, Gross/Net | Accrual detail |
| Borrow Fee Details | Symbol, Fee Rate, Borrow Fee | Short locate / borrow |
| SYEP Securities Lent Activity | Symbol, Date, Qty, Collateral | Stock yield enhancement |
| SYEP Securities Lent Interest Details | Symbol, Interest Paid to Customer | SYEP income |
| Financial Instrument Information | Symbol, Conid, Security ID | Reference |
| Codes / Notes | Code meanings, legal notes | Reference |

RealizedSummary adds / emphasizes: **Forex P/L Details**; may omit some AccountStatement-only blocks (e.g. SYEP, Borrow Fee, MTM) depending on statement type.

---

## 3. Report catalog (what you can generate)

Reports below are **feasible from the files already present**. Priority reflects the prompt examples plus high-value analytics.

### A. Cash & funding (AccountStatement)

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R01** | **Total deposits & withdrawals** | `Deposits & Withdrawals` | Sum Amount where Amount &gt; 0 (deposits); sum where Amount &lt; 0 (withdrawals); net funding; by currency; by year; line-item ledger |
| **R02** | Funding timeline | same | Monthly/yearly cash in/out chart data |
| **R03** | Cash report snapshot | `Cash Report` | Ending cash by currency (per statement end) |

**Quick check (all AccountStatement files, rough rollup):** ~40 deposit lines ≈ **298,020**; ~2 withdrawal lines ≈ **-4,000** (currency as in file, often account base / stated currency).

### B. Trading activity by symbol

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R10** | **Symbol-wise buys & sells** | `Trades` (`DataDiscriminator` = Order/Trade) | Symbol; buy qty/count; sell qty/count; avg buy/sell price; proceeds |
| **R11** | **Commission paid by symbol** | `Trades`.`Comm/Fee` | Sum \|Comm/Fee\| per Symbol; total commissions; by year |
| **R12** | Commission & transaction fees | `Trades` + `Transaction Fees` | Broker commission vs exchange/regulatory fees |
| **R13** | Trade blotter | `Trades` | Chronological order list (Date/Time, Side from qty sign, Price, Proceeds, Comm) |
| **R14** | Realized P/L from closed trades | `Trades`.`Realized P/L` (+ ClosedLot rows if present) | Per trade / per symbol realized |

**Side rule:** IBKR quantity &gt; 0 ≈ buy; quantity &lt; 0 ≈ sell (confirm per file).

### C. Corporate actions & income

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R20** | **Corporate actions by symbol** | `Corporate Actions` | Event description, qty, proceeds, realized P/L, date |
| **R21** | Dividends received | `Dividends` | Total dividends; by symbol (parse ticker from Description); by year |
| **R22** | Withholding tax | `Withholding Tax` | Tax paid; by symbol/year; effective rate vs dividends |
| **R23** | Dividend accruals | `Change in Dividend Accruals` | Ex-date / pay-date / gross / net by symbol |
| **R24** | Net dividend income | R21 − R22 | After-tax dividend income |

**Quick check (AccountStatement rollup):** dividends sum ≈ **5,642** (file currency).

### D. Interest paid / earned (IBKR)

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R30** | **Interest paid to IBKR** | `Interest` | Rows with negative Amount and/or debit/borrow descriptions; total interest expense |
| **R31** | Interest earned / credit interest | `Interest` | Positive Amount / credit interest descriptions |
| **R32** | Interest accruals snapshot | `Interest Accruals` | Accrued balances at statement end |
| **R33** | Net interest cost | R30 + R31 | Net financing cost |

**Quick check (AccountStatement rollup):** interest credit-ish ≈ **+4,186**; debit/paid-ish ≈ **-23,406** (classify carefully by Description).

### E. Performance & positions

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R40** | Symbol realized & unrealized P/L | `Realized & Unrealized Performance Summary` | S/T vs L/T realized; unrealized; total by symbol |
| **R41** | Mark-to-market P/L by symbol | `Mark-to-Market Performance Summary` | MTM position / transaction / commissions / other |
| **R42** | Open positions | `Open Positions` | Qty, cost basis, market value, unrealized P/L |
| **R43** | Change in NAV bridge | `Change in NAV` + `Net Asset Value` | Starting → ending NAV attribution |
| **R44** | Window realized summary | RealizedSummary same sections | Same as R40 for each RealizedSummary file window |

### F. Lending / borrow / SYEP (AccountStatement-heavy)

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R50** | Stock lend activity | SYEP Securities Lent Activity | Symbols lent, qty, collateral |
| **R51** | SYEP interest income | SYEP Securities Lent Interest Details | Interest paid to customer by symbol |
| **R52** | Borrow fees | `Borrow Fee Details` | Fee by symbol / rate |
| **R53** | Net stock position (lend/borrow) | `Net Stock Position Summary` | Shares at IB vs lent/borrowed |

### G. Forex

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R60** | Forex balances | `Forex Balances` | Per-currency exposure |
| **R61** | Forex realized P/L | RealizedSummary `Forex P/L Details` | FX conversion P/L in base (e.g. SGD) |

### H. Fees (non-commission)

| Report ID | Report name | Source section(s) | Metrics / columns |
|-----------|-------------|-------------------|-------------------|
| **R70** | Broker / other fees | `Fees` | By description (market data, etc.) |
| **R71** | All-in cost of trading | R11 + R12 + R70 + R30 | Commissions + fees + interest |

### I. Consolidated “dashboard” packs

| Report ID | Pack | Combines |
|-----------|------|----------|
| **R80** | **Account funding & cost dashboard** | R01, R30, R31, R11, R70 |
| **R81** | **Symbol trading & P&L pack** | R10, R11, R20, R21, R40, R42 |
| **R82** | **Income pack** | R21, R22, R51, R31 |
| **R83** | Lifetime Excel workbook | All priority reports across AccountStatement years (deduped) |

---

## 4. Mapping prompt examples → report IDs

| Prompt example | Report ID(s) |
|----------------|--------------|
| Total Deposits and Withdrawals | **R01**, R02 |
| Stock symbol wise Buy, Sell | **R10**, R13 |
| Corporate Actions | **R20** |
| Commission paid to trade symbol wise | **R11** |
| Interest paid to IBKR | **R30**, R33 |

---

## 5. Suggested Excel / CSV output layout (generator)

If implemented (e.g. `IBKR-Download` reporter):

| Sheet | Content |
|-------|---------|
| Report_Info | Files ingested, date coverage, base currency notes |
| Deposits_Withdrawals | Ledger + totals (R01) |
| Trades_All | Normalized orders from all AS years |
| By_Symbol_Trades | Buy/Sell qty & counts (R10) |
| Commission_By_Symbol | R11 |
| Corporate_Actions | R20 |
| Dividends | R21 |
| Withholding_Tax | R22 |
| Interest | R30/R31 detail |
| Interest_Summary | Totals paid vs earned |
| Symbol_Performance | From Realized & Unrealized Performance Summary (latest or yearly) |
| Open_Positions | Latest statement |
| SYEP_Borrow | R50–R52 |
| RealizedSummary_Windows | Index of RS files + per-window realized totals (no naive sum) |

---

## 6. Implementation notes

1. **Parser:** Section-aware CSV reader (`Section`, `Header`/`Data`); ignore `SubTotal`/`Total` for line aggregates or use Totals only as cross-checks.  
2. **Dedupe:** AccountStatement year files are mostly non-overlapping by calendar year; still guard if YTD file overlaps last year file. RealizedSummary windows **overlap** — never sum all RS files blindly.  
3. **Symbol extraction:** Dividends/interest descriptions often embed ticker (`GOOGL(US02079K3059) Cash Dividend…`) — regex extract for by-symbol income.  
4. **Currency:** Mixed USD lines vs account base (e.g. SGD) — report currency column always; optional FX convert later using Forex sections.  
5. **Relation to `trade_history`:** Flex/Activity pipeline already builds Symbol_PnL from trades+dividends. AccountStatement adds **deposits, interest, SYEP, borrow fees, MTM, official IBKR performance summary** not fully in Flex trade dumps. Best long-term: **ingest AccountStatement as another source** into analysis workbooks alongside `trade_history`.

---

## 7. Recommended build order (if coding next)

| Phase | Deliverable |
|-------|-------------|
| P0 | This catalog (done) |
| P1 | Parser + Excel: R01, R10, R11, R20, R21, **R22 Withholding Tax**, R30 — **implemented** |
| P2 | R40/R42 from latest statement; R50–R52 SYEP/borrow |
| P3 | RealizedSummary window reports + Forex P/L; merge with `trade_history` store |

**P1 tool:** `C:\Investment\IBKR-Download\`  
**Run:** `python -m ibkr_download_reports` → `reports/IBKR_AccountStatement_P1.xlsx`  
**Sheets include:** `Withholding_Tax`, `Withholding_By_Symbol`, `Dividends_Net_Of_Tax`

---

## 8. Review checklist

- [ ] Confirm base currency interpretation for Amount columns  
- [ ] Confirm Trade side from Quantity sign  
- [ ] Approve P1 report set (R01, R10, R11, R20, R30, R21)  
- [ ] Decide: new tool under `IBKR-Download/` vs extend `IBKR-Flex-BuySell`  
