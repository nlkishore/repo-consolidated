"""Validate JSON payloads against IDB contract tables."""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from geb_testbed.contracts.models import Contract
from geb_testbed.validators.json_path import (
    resolve_path,
    validate_constraints,
    validate_type,
)


@dataclass
class ElementResult:
    contract_id: str
    name: str
    path: str
    required: bool
    present: bool
    valid_type: bool
    status: str  # PASS | FAIL | WARN
    evidence: str = ""
    errors: list[str] = field(default_factory=list)


@dataclass
class JsonValidationResult:
    contract: str
    ok: bool
    elements: list[ElementResult] = field(default_factory=list)

    @property
    def fail_count(self) -> int:
        return sum(1 for e in self.elements if e.status == "FAIL")


def load_json(path: Path) -> Any:
    return json.loads(path.read_text(encoding="utf-8"))


def validate_json_contract(
    payload: Any,
    contract: Contract,
) -> JsonValidationResult:
    results: list[ElementResult] = []
    for el in contract.elements:
        if not el.path:
            continue
        found, value = resolve_path(payload, el.path)
        present = found and value is not None and value != ""
        errors: list[str] = []
        valid_type = True

        if el.required and not present:
            status = "FAIL"
            errors.append("required element missing")
        elif not present:
            status = "WARN"
        else:
            valid_type = validate_type(value, el.type)
            errors.extend(
                validate_constraints(value, max_length=el.max_length, pattern=el.pattern)
            )
            if not valid_type:
                errors.append(f"expected type {el.type}, got {type(value).__name__}")
            status = "PASS" if not errors else "FAIL"

        results.append(
            ElementResult(
                contract_id=el.id,
                name=el.name,
                path=el.path,
                required=el.required,
                present=present,
                valid_type=valid_type,
                status=status,
                evidence=str(value) if present else "",
                errors=errors,
            )
        )

    ok = all(e.status != "FAIL" for e in results)
    return JsonValidationResult(contract=contract.label, ok=ok, elements=results)
