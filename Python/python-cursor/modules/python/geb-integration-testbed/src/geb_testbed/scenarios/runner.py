"""Run contract validation for one scenario."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from geb_testbed.contracts.loader import resolve_contract
from geb_testbed.scenarios.registry import SCENARIOS, ScenarioDef
from geb_testbed.validators.json_contract import (
    JsonValidationResult,
    load_json,
    validate_json_contract,
)
from geb_testbed.validators.xml_contract import (
    XmlValidationResult,
    load_xml,
    validate_xml_contract,
)


@dataclass
class ScenarioRunResult:
    scenario: str
    persona: str
    description: str
    ok: bool
    json_results: list[JsonValidationResult] = field(default_factory=list)
    xml_results: list[XmlValidationResult] = field(default_factory=list)


def run_scenario(
    scenario_name: str,
    contracts_dir: Path,
    fixtures_dir: Path,
    *,
    include_negative: bool = True,
) -> ScenarioRunResult:
    if scenario_name not in SCENARIOS:
        raise ValueError(f"Unknown scenario: {scenario_name}. Available: {list(SCENARIOS)}")
    scenario = SCENARIOS[scenario_name]
    json_results: list[JsonValidationResult] = []
    xml_results: list[XmlValidationResult] = []

    idb_contract = resolve_contract(contracts_dir, scenario.idb_contract)
    payload = load_json(fixtures_dir / scenario.fixture_json)
    json_results.append(validate_json_contract(payload, idb_contract))

    if include_negative and scenario.negative_fixture_json:
        neg = load_json(fixtures_dir / scenario.negative_fixture_json)
        neg_result = validate_json_contract(neg, idb_contract)
        json_results.append(neg_result)

    if scenario.eai_outbound_contract and scenario.fixture_xml_outbound:
        eai_contract = resolve_contract(contracts_dir, scenario.eai_outbound_contract)
        xml_root = load_xml(fixtures_dir / scenario.fixture_xml_outbound)
        xml_results.append(validate_xml_contract(xml_root, eai_contract))

    if scenario.eai_response_contract and scenario.fixture_xml_response:
        resp_contract = resolve_contract(contracts_dir, scenario.eai_response_contract)
        xml_root = load_xml(fixtures_dir / scenario.fixture_xml_response)
        xml_results.append(validate_xml_contract(xml_root, resp_contract))

    ok = bool(json_results) and json_results[0].ok and all(r.ok for r in xml_results)
    if include_negative and scenario.negative_fixture_json and len(json_results) > 1:
        if json_results[1].ok:
            ok = False  # negative fixture must fail contract validation

    return ScenarioRunResult(
        scenario=scenario.name,
        persona=scenario.persona,
        description=scenario.description,
        ok=ok,
        json_results=json_results,
        xml_results=xml_results,
    )


def run_all_scenarios(
    contracts_dir: Path,
    fixtures_dir: Path,
    scenario_names: list[str] | None = None,
) -> list[ScenarioRunResult]:
    names = scenario_names or list(SCENARIOS.keys())
    return [run_scenario(n, contracts_dir, fixtures_dir) for n in names]
