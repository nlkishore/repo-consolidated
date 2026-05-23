"""Contract validation tests using bundled fixtures."""

from pathlib import Path

from geb_testbed.scenarios.runner import run_all_scenarios, run_scenario

ROOT = Path(__file__).resolve().parents[1]
CONTRACTS = ROOT / "contracts"
FIXTURES = ROOT / "fixtures"


def test_maker_payments_passes():
    result = run_scenario("maker_payments", CONTRACTS, FIXTURES)
    assert result.ok
    assert result.json_results[0].ok


def test_checker_payments_passes():
    result = run_scenario("checker_payments", CONTRACTS, FIXTURES)
    assert result.ok
    assert len(result.xml_results) == 2


def test_single_user_payments_passes():
    result = run_scenario("single_user_payments", CONTRACTS, FIXTURES)
    assert result.ok


def test_run_all_scenarios():
    results = run_all_scenarios(CONTRACTS, FIXTURES)
    assert len(results) == 3
    assert all(r.ok for r in results)
