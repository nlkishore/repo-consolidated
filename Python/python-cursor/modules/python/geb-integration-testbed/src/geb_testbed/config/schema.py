"""Configuration models."""

from __future__ import annotations

from pydantic import BaseModel, Field


class PathsSettings(BaseModel):
    contracts_dir: str = "contracts"
    fixtures_dir: str = "fixtures"
    reports_dir: str = "geb-testbed-reports"


class ScenarioSettings(BaseModel):
    name: str
    idb_contract: str
    eai_outbound_contract: str | None = None
    eai_response_contract: str | None = None
    fixture_json: str
    fixture_xml_outbound: str | None = None
    fixture_xml_response: str | None = None


class Settings(BaseModel):
    paths: PathsSettings = Field(default_factory=PathsSettings)
    scenarios: list[str] = Field(
        default_factory=lambda: [
            "maker_payments",
            "checker_payments",
            "single_user_payments",
        ]
    )
