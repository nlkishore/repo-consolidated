"""geb-testbed CLI."""

from __future__ import annotations

from pathlib import Path
from typing import Optional

import typer
from rich.console import Console
from rich.table import Table

from geb_testbed.config.loader import load_settings, project_root
from geb_testbed.reports.writer import write_reports
from geb_testbed.scenarios.registry import SCENARIOS
from geb_testbed.scenarios.runner import run_all_scenarios, run_scenario

app = typer.Typer(help="GEB integration testbed — IDB JSON and EAI XML contract validation")
console = Console()


def _paths(settings):
    root = project_root()
    return (
        root / settings.paths.contracts_dir,
        root / settings.paths.fixtures_dir,
        root / settings.paths.reports_dir,
    )


@app.command("list-scenarios")
def list_scenarios() -> None:
    """List available Maker / Checker / Single-user scenarios."""
    t = Table(title="Scenarios")
    t.add_column("Name")
    t.add_column("Persona")
    t.add_column("Description")
    for s in SCENARIOS.values():
        t.add_row(s.name, s.persona, s.description)
    console.print(t)


@app.command()
def validate(
    scenario: Optional[str] = typer.Option(None, help="Scenario name or omit for all"),
    config: str = typer.Option("config/settings.example.yaml", help="Settings YAML path"),
) -> None:
    """Validate fixture messages against IDB/EAI contracts."""
    settings = load_settings(project_root() / config)
    contracts_dir, fixtures_dir, reports_dir = _paths(settings)

    if scenario:
        results = [run_scenario(scenario, contracts_dir, fixtures_dir)]
    else:
        results = run_all_scenarios(contracts_dir, fixtures_dir, settings.scenarios)

    _print_results(results)
    json_p, html_p = write_reports(results, reports_dir)
    console.print(f"\nReports: {html_p}")

    if not all(r.ok for r in results):
        raise typer.Exit(1)


@app.command(name="run-all")
def run_all_cmd(
    config: str = typer.Option("config/settings.example.yaml"),
) -> None:
    """Validate all scenarios and write contract matrix report."""
    validate(scenario=None, config=config)


def _print_results(results) -> None:
    for r in results:
        style = "green" if r.ok else "red"
        console.print(f"\n[bold]{r.scenario}[/bold] ({r.persona}): [{style}]{'PASS' if r.ok else 'FAIL'}[/{style}]")
        for idx, jr in enumerate(r.json_results):
            for el in jr.elements:
                if el.status != "FAIL":
                    continue
                if idx > 0 and not r.ok:
                    continue  # expected negative fixture failures
                console.print(f"  [red]{el.contract_id}[/red] {el.name}: {el.errors}")


if __name__ == "__main__":
    app()
