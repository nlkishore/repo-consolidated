"""Unit tests for alert predicate."""

from completely_sold_alert.config import AppSettings, AlertSettings
from completely_sold_alert.services.evaluate import evaluate_rows, should_alert


def test_should_alert_at_threshold():
    settings = AppSettings(alert=AlertSettings(price_drop_threshold_pct=-5.0))
    row = {
        "Symbol": "AAPL",
        "Last_Sold_Price": 200.0,
        "Current_Market_Price": 190.0,
        "Change_Since_Last_Sold_Pct": -5.0,
    }
    assert should_alert(row, settings) is True


def test_should_not_alert_above_threshold():
    settings = AppSettings(alert=AlertSettings(price_drop_threshold_pct=-5.0))
    row = {
        "Symbol": "MSFT",
        "Last_Sold_Price": 400.0,
        "Current_Market_Price": 395.0,
        "Change_Since_Last_Sold_Pct": -1.25,
    }
    assert should_alert(row, settings) is False


def test_evaluate_fixture_sample():
    import json
    from pathlib import Path

    root = Path(__file__).resolve().parents[1]
    rows = json.loads((root / "fixtures" / "completely_sold_sample.json").read_text())
    settings = AppSettings(alert=AlertSettings(price_drop_threshold_pct=-5.0))
    candidates, _ = evaluate_rows(rows, settings)
    symbols = {r["Symbol"] for r in candidates}
    assert symbols == {"AAPL", "TSLA"}
