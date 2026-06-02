"""Alert predicate and candidate filtering."""

from __future__ import annotations

from typing import Any

from completely_sold_alert.config import AppSettings


def should_alert(row: dict[str, Any], settings: AppSettings) -> bool:
    threshold = settings.alert.price_drop_threshold_pct
    change = row.get("Change_Since_Last_Sold_Pct")
    last_sold = row.get("Last_Sold_Price")
    current = row.get("Current_Market_Price")
    if change is None or last_sold is None or current is None:
        return False
    try:
        if float(last_sold) <= 0 or float(current) <= 0:
            return False
        return float(change) <= float(threshold)
    except (TypeError, ValueError):
        return False


def evaluate_rows(
    rows: list[dict[str, Any]], settings: AppSettings
) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    candidates: list[dict[str, Any]] = []
    skipped: list[dict[str, Any]] = []

    for row in rows:
        if should_alert(row, settings):
            candidates.append(row)
        elif row.get("Change_Since_Last_Sold_Pct") is None or row.get("Current_Market_Price") is None:
            skipped.append(row)

    candidates.sort(
        key=lambda r: float(r.get("Change_Since_Last_Sold_Pct") or 0),
    )
    return candidates, skipped
