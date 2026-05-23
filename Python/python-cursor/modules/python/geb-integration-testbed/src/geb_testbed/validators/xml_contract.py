"""Validate XML against EAI contract element tables (XPath)."""

from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from lxml import etree

from geb_testbed.contracts.models import Contract
from geb_testbed.validators.json_contract import ElementResult


@dataclass
class XmlValidationResult:
    contract: str
    ok: bool
    elements: list[ElementResult] = field(default_factory=list)


def load_xml(path: Path) -> etree._Element:
    return etree.parse(str(path)).getroot()


def validate_xml_contract(
    root: etree._Element,
    contract: Contract,
) -> XmlValidationResult:
    results: list[ElementResult] = []
    for el in contract.elements:
        if not el.xpath:
            continue
        nodes = root.xpath(el.xpath, namespaces=root.nsmap if hasattr(root, "nsmap") else None)
        present = len(nodes) > 0 and (nodes[0].text or "").strip() != ""
        value = nodes[0].text.strip() if present and nodes[0].text else ""

        if el.required and not present:
            status, errors = "FAIL", ["required element missing"]
        elif not present:
            status, errors = "WARN", []
        else:
            status, errors = "PASS", []

        results.append(
            ElementResult(
                contract_id=el.id,
                name=el.name,
                path=el.xpath,
                required=el.required,
                present=present,
                valid_type=True,
                status=status,
                evidence=value,
                errors=errors,
            )
        )

    ok = all(e.status != "FAIL" for e in results)
    return XmlValidationResult(contract=contract.label, ok=ok, elements=results)
