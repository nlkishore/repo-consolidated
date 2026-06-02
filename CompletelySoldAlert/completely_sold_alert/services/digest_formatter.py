"""Single WhatsApp digest message formatter."""

from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime
from typing import Any
from zoneinfo import ZoneInfo

from completely_sold_alert.config import AppSettings


@dataclass
class DigestMeta:
    run_at: datetime
    total_completely_sold: int
    cooldown_suppressed: list[str]
    skipped_quote_count: int
    threshold_pct: float


def _fmt_money(value: Any) -> str:
    try:
        return f"{float(value):,.2f}"
    except (TypeError, ValueError):
        return "—"


def _fmt_pct(value: Any) -> str:
    try:
        return f"{float(value):+.1f}%"
    except (TypeError, ValueError):
        return "—"


def build_digest(
    candidates: list[dict[str, Any]],
    settings: AppSettings,
    meta: DigestMeta,
) -> str:
    tz = ZoneInfo(settings.schedule.timezone)
    run_date = meta.run_at.astimezone(tz).strftime("%Y-%m-%d %H:%M %Z")
    threshold = meta.threshold_pct
    max_sym = settings.notify.digest_max_symbols

    lines: list[str] = [
        "📉 *Completely Sold — Price Drop Alert*",
        f"📅 {run_date}  |  🔔 {len(candidates)} symbol(s)",
        f"Threshold: *{threshold:.1f}%* vs last sold price",
        "━━━━━━━━━━━━━━━━━━━━",
        "",
    ]

    shown = candidates[:max_sym]
    overflow = len(candidates) - len(shown)

    for row in shown:
        sym = str(row.get("Symbol") or "?")
        last_sold = row.get("Last_Sold_Price")
        current = row.get("Current_Market_Price")
        change = row.get("Change_Since_Last_Sold_Pct")
        sold_date = row.get("Last_Sold_Date") or row.get("First_Buy_Date") or "—"
        profit = row.get("Profit")
        profit_pct = row.get("Profit_Pct")

        lines.extend(
            [
                f"*{sym}*",
                f"  Sold:  ${_fmt_money(last_sold)}  →  Now: ${_fmt_money(current)}",
                f"  Change: *{_fmt_pct(change)}*  |  Sold on: {sold_date}",
                f"  Profit when sold: ${_fmt_money(profit)} ({_fmt_pct(profit_pct)})",
                "",
            ]
        )

    if overflow > 0:
        lines.append(f"_+{overflow} more symbol(s) not shown (digest_max_symbols={max_sym})_")
        lines.append("")

    lines.extend(
        [
            "━━━━━━━━━━━━━━━━━━━━",
            f"📊 Monitored: {meta.total_completely_sold} closed positions",
        ]
    )
    if meta.cooldown_suppressed:
        lines.append(
            f"⏭ Cooldown: {len(meta.cooldown_suppressed)} omitted "
            f"({', '.join(meta.cooldown_suppressed[:5])}"
            f"{'…' if len(meta.cooldown_suppressed) > 5 else ''})"
        )
    if meta.skipped_quote_count:
        lines.append(f"⚠ Skipped (no quote): {meta.skipped_quote_count}")
    lines.append("_Source: IBKR Completely_Sold · Yahoo Finance_")

    return "\n".join(lines)
