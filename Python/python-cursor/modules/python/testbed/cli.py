"""testbed CLI — seed / validate / reset / report / run-all."""

from __future__ import annotations

import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

import typer
from rich.console import Console
from rich.table import Table

from testbed.builders.scenario_builder import SCENARIO_BUILDERS
from testbed.config.loader import load_settings
from testbed.db.connection import db_cursor
from testbed.db.resetter import reset_tables
from testbed.db.seeder import resolve_entity_ids, resolve_user_ids, seed_scenario
from testbed.db.validator import validate
from testbed.reports.writer import write_html_summary, write_json

app = typer.Typer(help="Corporate banking testbed — GEB domain seed and validation")
console = Console()


def _get_settings(config: str):
    try:
        return load_settings(config)
    except FileNotFoundError as exc:
        console.print(f"[red]Config not found:[/red] {exc}")
        raise typer.Exit(1)


# ---------------------------------------------------------------------------
# seed
# ---------------------------------------------------------------------------

@app.command()
def seed(
    config: str = typer.Option("config/settings.yaml", help="Path to settings YAML"),
    scenario: Optional[str] = typer.Option(None, help="Scenario name or 'all'"),
    all_scenarios: bool = typer.Option(False, "--all", help="Seed all scenarios"),
    company_id: Optional[int] = typer.Option(None, help="Override company_id"),
    reset: bool = typer.Option(False, "--reset", help="Truncate tables before seeding"),
) -> None:
    """Seed one or all scenarios into the database."""
    settings = _get_settings(config)

    names: list[str]
    if all_scenarios or scenario == "all":
        names = settings.scenarios
    elif scenario:
        names = [scenario]
    else:
        console.print("[yellow]Specify --scenario NAME or --all[/yellow]")
        raise typer.Exit(1)

    invalid = [n for n in names if n not in SCENARIO_BUILDERS]
    if invalid:
        console.print(f"[red]Unknown scenarios:[/red] {invalid}. Available: {list(SCENARIO_BUILDERS)}")
        raise typer.Exit(1)

    with db_cursor(settings) as (cur, conn):
        if reset or settings.seed.reset_before_seed:
            console.print("[bold]Resetting tables…[/bold]")
            cleared = reset_tables(cur)
            conn.commit()
            console.print(f"  Cleared: {', '.join(cleared)}")

        for name in names:
            cid = company_id or (100 + list(SCENARIO_BUILDERS.keys()).index(name))
            builder = SCENARIO_BUILDERS[name]
            data = builder(
                company_id=cid,
                seed=settings.seed,
                security=settings.security,
            )
            console.print(f"[bold]Seeding scenario:[/bold] {name} (company_id={cid})")
            counts = seed_scenario(cur, data, settings.security)

            user_map = resolve_user_ids(cur, data.users)
            entity_map = resolve_entity_ids(cur, data.entities)

            conn.commit()
            _print_counts(name, counts)

    console.print("\n[bold green]Done.[/bold green]")


# ---------------------------------------------------------------------------
# validate
# ---------------------------------------------------------------------------

@app.command()
def validate_cmd(
    config: str = typer.Option("config/settings.yaml"),
) -> None:
    """Run post-seed integrity assertions."""
    settings = _get_settings(config)
    with db_cursor(settings) as (cur, _conn):
        result = validate(cur)

    t = Table(title="Validation Results")
    t.add_column("Check")
    t.add_column("Value")
    t.add_column("Status")
    for c in result.checks:
        style = "green" if c["status"] == "PASS" else ("yellow" if c["status"] == "WARN" else "red")
        t.add_row(c["check"], str(c["value"]), f"[{style}]{c['status']}[/{style}]")
    console.print(t)

    if result.errors:
        for err in result.errors:
            console.print(f"[red]ERROR:[/red] {err}")
        raise typer.Exit(1)
    console.print("[bold green]Validation PASS[/bold green]")


app.command(name="validate")(validate_cmd)


# ---------------------------------------------------------------------------
# reset
# ---------------------------------------------------------------------------

@app.command()
def reset_cmd(
    config: str = typer.Option("config/settings.yaml"),
    yes: bool = typer.Option(False, "--yes", help="Skip confirmation"),
) -> None:
    """Truncate all testbed tables and reset AUTO_INCREMENT."""
    if not yes:
        confirm = typer.confirm("This will delete ALL testbed data. Continue?")
        if not confirm:
            raise typer.Abort()
    settings = _get_settings(config)
    with db_cursor(settings) as (cur, conn):
        cleared = reset_tables(cur)
        conn.commit()
    for t_name in cleared:
        console.print(f"  [dim]{t_name}[/dim]")
    console.print("[bold green]Reset complete.[/bold green]")


app.command(name="reset")(reset_cmd)


# ---------------------------------------------------------------------------
# report
# ---------------------------------------------------------------------------

@app.command()
def report(
    config: str = typer.Option("config/settings.yaml"),
    fmt: str = typer.Option("html", "--format"),
) -> None:
    """Generate HTML or JSON summary from live DB."""
    settings = _get_settings(config)
    with db_cursor(settings) as (cur, _conn):
        result = validate(cur)
        summary = _build_summary(cur, settings.scenarios)

    out_dir = Path(settings.reports.output_dir)
    if fmt == "json":
        path = out_dir / "testbed-summary.json"
        write_json({"summary": summary, "validation": result.checks}, path)
        console.print(f"JSON report: {path}")
    else:
        path = write_html_summary(summary, result, out_dir)
        console.print(f"HTML report: {path}")


# ---------------------------------------------------------------------------
# run-all
# ---------------------------------------------------------------------------

@app.command(name="run-all")
def run_all(
    config: str = typer.Option("config/settings.yaml"),
    reset: bool = typer.Option(True, "--reset/--no-reset"),
) -> None:
    """reset + seed --all + validate + report."""
    settings = _get_settings(config)

    with db_cursor(settings) as (cur, conn):
        if reset:
            console.print("[bold]Resetting tables…[/bold]")
            reset_tables(cur)
            conn.commit()

        summary: dict = {
            "run_at": datetime.now(timezone.utc).isoformat(),
            "scenarios": [],
            "personas": [],
        }

        for name in settings.scenarios:
            if name not in SCENARIO_BUILDERS:
                console.print(f"[yellow]Unknown scenario {name!r}, skipping[/yellow]")
                continue
            cid = 100 + list(SCENARIO_BUILDERS.keys()).index(name)
            builder = SCENARIO_BUILDERS[name]
            data = builder(
                company_id=cid,
                seed=settings.seed,
                security=settings.security,
            )
            console.print(f"[bold]Seeding:[/bold] {name}")
            counts = seed_scenario(cur, data, settings.security)
            conn.commit()

            user_map = resolve_user_ids(cur, data.users)
            entity_map = resolve_entity_ids(cur, data.entities)

            scenario_summary = {
                "name": name,
                "companies": counts.get("entities", 0),
                "entities": counts.get("entities", 0),
                "users": counts.get("users", 0),
                "roles": counts.get("roles", 0),
                "permissions": counts.get("permissions", 0),
                "groups": counts.get("groups", 0),
                "status": "PASS",
            }
            summary["scenarios"].append(scenario_summary)

            for user in data.users:
                summary["personas"].append({
                    "login_id": user.login_id,
                    "scenario": name,
                    "roles": [r.rolename for r in data.roles],
                    "company": data.companies[0].abbv_name if data.companies else "",
                })

        result = validate(cur)

    out_dir = Path(settings.reports.output_dir)
    write_json({"summary": summary, "validation": result.checks}, out_dir / "testbed-summary.json")
    html_path = write_html_summary(summary, result, out_dir)

    if result.ok:
        console.print(f"\n[bold green]run-all PASS[/bold green] — reports: {out_dir}")
    else:
        console.print(f"\n[bold red]run-all FAIL[/bold red] — see {html_path}")
        for err in result.errors:
            console.print(f"  [red]{err}[/red]")
        raise typer.Exit(1)


# ---------------------------------------------------------------------------
# helpers
# ---------------------------------------------------------------------------

def _print_counts(scenario: str, counts: dict[str, int]) -> None:
    for k, v in counts.items():
        console.print(f"  {k}: {v}")


def _build_summary(cur, scenarios: list[str]) -> dict:
    summary: dict = {
        "run_at": datetime.now(timezone.utc).isoformat(),
        "scenarios": [],
        "personas": [],
    }
    tables = [
        "GTP_USER", "GTP_ROLE", "GTP_PERMISSION", "GTP_GROUP",
        "GTP_ENTITY", "GTP_USER_GROUP_ROLE",
    ]
    counts = {}
    for t in tables:
        cur.execute(f"SELECT COUNT(*) AS cnt FROM `{t}`")
        row = cur.fetchone()
        counts[t] = row["cnt"] if row else 0

    cur.execute("SELECT LOGIN_ID, COMPANY_ABBV_NAME FROM GTP_USER ORDER BY LOGIN_ID")
    users = cur.fetchall()
    for u in users:
        summary["personas"].append({
            "login_id": u["LOGIN_ID"],
            "scenario": "",
            "roles": [],
            "company": u.get("COMPANY_ABBV_NAME", ""),
        })

    for s in scenarios:
        summary["scenarios"].append({"name": s, "status": "SEEDED"})

    summary["table_counts"] = counts
    return summary
