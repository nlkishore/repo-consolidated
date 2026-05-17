"""JSON and HTML report writers."""

from __future__ import annotations

import json
from pathlib import Path

from cr_slice_validation.reconciler import ReconcileResult


def write_json(result: ReconcileResult, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        json.dumps(result.to_dict(), indent=2) + "\n",
        encoding="utf-8",
    )


def write_html(result: ReconcileResult, path: Path) -> None:
    d = result.to_dict()
    rows = []
    for f in d.get("missing_files") or []:
        rows.append(f"<tr class='fail'><td>missing</td><td>{_esc(f)}</td></tr>")
    for f in d.get("extra_files") or []:
        rows.append(f"<tr class='fail'><td>extra</td><td>{_esc(f)}</td></tr>")
    for m in d.get("file_mismatches") or []:
        rows.append(
            f"<tr class='fail'><td>mismatch</td><td>{_esc(m['path'])} "
            f"(expected {m['expected_hash']}, actual {m['actual_hash']})</td></tr>"
        )
    if not rows:
        rows.append("<tr><td colspan='2'>No file issues</td></tr>")

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <title>CR Slice Report — {_esc(d.get('cr_id', ''))}</title>
  <style>
    body {{ font-family: system-ui, sans-serif; margin: 2rem; }}
    .pass {{ color: #0a0; }}
    .fail {{ color: #a00; }}
    table {{ border-collapse: collapse; width: 100%; margin-top: 1rem; }}
    th, td {{ border: 1px solid #ccc; padding: 0.5rem; text-align: left; }}
    th {{ background: #f5f5f5; }}
  </style>
</head>
<body>
  <h1>CR Slice Reconciliation</h1>
  <p><strong>Status:</strong> <span class="{d.get('status', '').lower()}">{_esc(d.get('status', ''))}</span></p>
  <ul>
    <li>CR: {_esc(d.get('cr_id', ''))}</li>
    <li>Baseline: {_esc(d.get('baseline_ref', ''))} ({_esc(d.get('baseline_sha', '')[:12])}…)</li>
    <li>Expected: {_esc(d.get('expected_ref', ''))} ({_esc(d.get('expected_sha', '')[:12])}…)</li>
    <li>Slice: {_esc(d.get('slice_ref', ''))} ({_esc(d.get('slice_sha', '')[:12])}…)</li>
  </ul>
  <h2>Summary</h2>
  <pre>{_esc(json.dumps(d.get('summary', {}), indent=2))}</pre>
  <h2>Files</h2>
  <table>
    <thead><tr><th>Type</th><th>Detail</th></tr></thead>
    <tbody>
      {''.join(rows)}
    </tbody>
  </table>
</body>
</html>
"""
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(html, encoding="utf-8")


def _esc(text: str) -> str:
    return (
        str(text)
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace('"', "&quot;")
    )
