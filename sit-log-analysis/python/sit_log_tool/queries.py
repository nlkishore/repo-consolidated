"""Local query implementations (Splunk SPL equivalents)."""

from __future__ import annotations

import csv
import json
import os
import re
from collections import Counter, defaultdict
from datetime import datetime
from pathlib import Path
from typing import Any

from .jsonl_io import load_events, write_json


def _status_int(event: dict[str, Any]) -> int | None:
    status = event.get("status")
    if status is None:
        return None
    try:
        return int(status)
    except (TypeError, ValueError):
        return None


def _hour_bucket(event: dict[str, Any]) -> str:
    raw = event.get("@timestamp") or event.get("timestamp") or ""
    if not raw:
        return "unknown"
    text = str(raw)
    if "T" in text:
        date_part, time_part = text.split("T", 1)
        hour = time_part.split(":")[0] if ":" in time_part else "00"
        return f"{date_part} {hour}:00"
    parts = text.split()
    if len(parts) >= 2:
        hour = parts[1].split(":")[0] if ":" in parts[1] else "00"
        return f"{parts[0]} {hour}:00"
    return text[:13] + ":00" if len(text) >= 13 else "unknown"


def api_errors_by_status(events: list[dict[str, Any]], out_dir: Path) -> None:
    groups: Counter[tuple[int, str]] = Counter()
    for event in events:
        status = _status_int(event)
        if status is None or status < 400:
            continue
        service = str(event.get("service") or "")
        groups[(status, service)] += 1

    rows = [
        {"status": s, "service": svc, "count": n}
        for (s, svc), n in groups.most_common()
    ]

    csv_path = out_dir / "errors-by-status.csv"
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["status", "service", "count"])
        for row in rows:
            writer.writerow([row["status"], row["service"], row["count"]])

    write_json(out_dir / "errors-by-status.json", rows)


def slow_requests(events: list[dict[str, Any]], out_dir: Path, min_ms: int) -> None:
    matched = []
    for event in events:
        latency = event.get("latencyMs")
        if latency is None:
            continue
        try:
            ms = int(latency)
        except (TypeError, ValueError):
            continue
        if ms >= min_ms:
            matched.append(event)

    matched.sort(key=lambda e: int(e.get("latencyMs") or 0), reverse=True)

    csv_path = out_dir / "slow-requests.csv"
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["latencyMs", "endpoint", "service", "traceId", "status"])
        for event in matched:
            writer.writerow(
                [
                    event.get("latencyMs"),
                    event.get("endpoint"),
                    event.get("service"),
                    event.get("traceId"),
                    event.get("status"),
                ]
            )

    write_json(out_dir / "slow-requests.json", matched)


def requests_by_trace_id(
    events: list[dict[str, Any]], out_dir: Path, trace_id: str
) -> None:
    matched = [e for e in events if str(e.get("traceId") or "") == trace_id]
    safe_name = re.sub(r"[^\w.-]", "_", trace_id)
    write_json(out_dir / f"trace-{safe_name}.json", matched)

    txt_path = out_dir / f"trace-{safe_name}.txt"
    lines = []
    for event in matched:
        ts = event.get("@timestamp") or event.get("_time") or "?"
        level = event.get("level") or "-"
        status = event.get("status") or "-"
        endpoint = event.get("endpoint") or ""
        message = event.get("message") or ""
        lines.append(f"{ts} {level} status={status} {endpoint} {message}".strip())
    txt_path.write_text("\n".join(lines) + ("\n" if lines else ""), encoding="utf-8")


def error_timeline(events: list[dict[str, Any]], out_dir: Path) -> None:
    buckets: Counter[str] = Counter()
    for event in events:
        status = _status_int(event)
        if status is None or status < 400:
            continue
        buckets[_hour_bucket(event)] += 1

    rows = [{"hour": h, "count": n} for h, n in sorted(buckets.items())]

    csv_path = out_dir / "error-timeline.csv"
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["hour", "count"])
        for row in rows:
            writer.writerow([row["hour"], row["count"]])

    write_json(out_dir / "error-timeline.json", rows)


def top_endpoints_by_volume(
    events: list[dict[str, Any]], out_dir: Path, top_n: int
) -> None:
    counts: Counter[str] = Counter()
    for event in events:
        endpoint = event.get("endpoint")
        if endpoint:
            counts[str(endpoint)] += 1

    rows = [{"endpoint": ep, "count": n} for ep, n in counts.most_common(top_n)]

    csv_path = out_dir / "top-endpoints.csv"
    with csv_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["endpoint", "count"])
        for row in rows:
            writer.writerow([row["endpoint"], row["count"]])

    write_json(out_dir / "top-endpoints.json", rows)


RUNNERS = {
    "api-errors-by-status": lambda events, out: api_errors_by_status(events, out),
    "slow-requests": lambda events, out: slow_requests(
        events, out, int(os.environ.get("LATENCY_MS_MIN", "1000"))
    ),
    "requests-by-trace-id": lambda events, out: requests_by_trace_id(
        events, out, os.environ["TRACE_ID"]
    ),
    "error-timeline": lambda events, out: error_timeline(events, out),
    "top-endpoints-by-volume": lambda events, out: top_endpoints_by_volume(
        events, out, int(os.environ.get("TOP_N", "20"))
    ),
}


def run_query(query_id: str, events_path: Path, out_dir: Path) -> None:
    query_id = query_id.strip().replace("\r", "")
    out_dir = Path(str(out_dir).replace("\r", ""))
    events_path = Path(str(events_path).replace("\r", ""))
    if query_id not in RUNNERS:
        known = ", ".join(sorted(RUNNERS))
        raise SystemExit(f"Unknown query '{query_id}'. Known: {known}")
    if query_id == "requests-by-trace-id" and not os.environ.get("TRACE_ID"):
        raise SystemExit("TRACE_ID environment variable is required")

    events = load_events(events_path)
    out_dir.mkdir(parents=True, exist_ok=True)
    RUNNERS[query_id](events, out_dir)
