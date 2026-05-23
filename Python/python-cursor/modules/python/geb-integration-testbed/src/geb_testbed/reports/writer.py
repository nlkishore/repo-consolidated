"""Write contract matrix reports (JSON + HTML)."""

from __future__ import annotations

import json
from datetime import datetime, timezone
from pathlib import Path

from jinja2 import Environment, FileSystemLoader, select_autoescape

from geb_testbed.scenarios.runner import ScenarioRunResult


def _serialize_results(results: list[ScenarioRunResult]) -> dict:
    out = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "overall_ok": all(r.ok for r in results),
        "scenarios": [],
    }
    for r in results:
        rows = []
        for jr in r.json_results:
            for el in jr.elements:
                rows.append(
                    {
                        "direction": "Inbound JSON",
                        "contract": jr.contract,
                        "contract_id": el.contract_id,
                        "element": el.name,
                        "required": el.required,
                        "present": el.present,
                        "valid_type": el.valid_type,
                        "status": el.status,
                        "evidence": el.evidence,
                        "errors": el.errors,
                    }
                )
        for xr in r.xml_results:
            for el in xr.elements:
                rows.append(
                    {
                        "direction": "XML",
                        "contract": xr.contract,
                        "contract_id": el.contract_id,
                        "element": el.name,
                        "required": el.required,
                        "present": el.present,
                        "valid_type": el.valid_type,
                        "status": el.status,
                        "evidence": el.evidence,
                        "errors": el.errors,
                    }
                )
        out["scenarios"].append(
            {
                "name": r.scenario,
                "persona": r.persona,
                "description": r.description,
                "ok": r.ok,
                "matrix": rows,
            }
        )
    return out


def write_reports(results: list[ScenarioRunResult], out_dir: Path) -> tuple[Path, Path]:
    out_dir.mkdir(parents=True, exist_ok=True)
    data = _serialize_results(results)
    json_path = out_dir / "geb-contract-matrix.json"
    json_path.write_text(json.dumps(data, indent=2), encoding="utf-8")

    tpl_dir = Path(__file__).parent / "templates"
    env = Environment(
        loader=FileSystemLoader(str(tpl_dir)),
        autoescape=select_autoescape(["html", "xml"]),
    )
    html = env.get_template("contract-matrix.html.j2").render(**data)
    html_path = out_dir / "geb-contract-matrix.html"
    html_path.write_text(html, encoding="utf-8")
    return json_path, html_path
