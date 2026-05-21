"""JSON and HTML report writers for testbed runs."""

from __future__ import annotations

import json
from pathlib import Path

from jinja2 import Environment, FileSystemLoader

from testbed.db.validator import ValidationResult


def write_json(data: dict, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, default=str) + "\n", encoding="utf-8")


def write_html_summary(
    summary: dict,
    validation: ValidationResult,
    output_dir: Path,
) -> Path:
    output_dir.mkdir(parents=True, exist_ok=True)
    template_dir = Path(__file__).parent / "templates"
    env = Environment(
        loader=FileSystemLoader(str(template_dir)),
        autoescape=True,
    )
    tmpl = env.get_template("testbed-summary.html.j2")
    html = tmpl.render(summary=summary, validation=validation)
    out = output_dir / "testbed-summary.html"
    out.write_text(html, encoding="utf-8")
    return out
