"""Scenario definitions: which contracts and fixtures apply per flow."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class ScenarioDef:
    name: str
    description: str
    persona: str  # maker | checker | single_user
    idb_contract: str
    eai_outbound_contract: str | None
    eai_response_contract: str | None
    fixture_json: str
    fixture_xml_outbound: str | None = None
    fixture_xml_response: str | None = None
    negative_fixture_json: str | None = None


SCENARIOS: dict[str, ScenarioDef] = {
    "maker_payments": ScenarioDef(
        name="maker_payments",
        description="Maker submits payment JSON; GEB persists to DB (no EAI).",
        persona="maker",
        idb_contract="idb/payments_submit_v1.yaml",
        eai_outbound_contract=None,
        eai_response_contract=None,
        fixture_json="json/maker_payment_valid.json",
        negative_fixture_json="json/maker_payment_missing_message_id.json",
    ),
    "checker_payments": ScenarioDef(
        name="checker_payments",
        description="Checker sends reference; GEB loads DB and sends XML to EAI.",
        persona="checker",
        idb_contract="idb/payments_approve_v1.yaml",
        eai_outbound_contract="eai/payments_outbound_v1.yaml",
        eai_response_contract="eai/payments_response_v1.yaml",
        fixture_json="json/checker_approve_valid.json",
        fixture_xml_outbound="xml/eai_payment_outbound.xml",
        fixture_xml_response="xml/eai_payment_response_ack.xml",
    ),
    "single_user_payments": ScenarioDef(
        name="single_user_payments",
        description="Single user: JSON in, XML to EAI in one step.",
        persona="single_user",
        idb_contract="idb/payments_submit_v1.yaml",
        eai_outbound_contract="eai/payments_outbound_v1.yaml",
        eai_response_contract="eai/payments_response_v1.yaml",
        fixture_json="json/single_user_payment_valid.json",
        fixture_xml_outbound="xml/eai_payment_outbound.xml",
        fixture_xml_response="xml/eai_payment_response_ack.xml",
    ),
}
