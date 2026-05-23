"""Load contract YAML files."""

from __future__ import annotations

from pathlib import Path

import yaml

from geb_testbed.contracts.models import Contract, ContractElement


def load_contract(path: Path) -> Contract:
    data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    elements = [
        ContractElement(
            id=e["id"],
            name=e.get("name", e["id"]),
            required=bool(e.get("required", True)),
            description=e.get("description", ""),
            path=e.get("path", ""),
            xpath=e.get("xpath", ""),
            type=e.get("type", "string"),
            max_length=e.get("maxLength"),
            pattern=e.get("pattern"),
            maps_from=e.get("mapsFrom"),
        )
        for e in data.get("elements", [])
    ]
    return Contract(
        api=data.get("api", ""),
        message=data.get("message", ""),
        version=str(data.get("version", "1.0")),
        direction=data.get("direction", "inbound"),
        format=data.get("format", "json"),
        root=data.get("root", ""),
        elements=elements,
    )


def resolve_contract(contracts_dir: Path, relative: str) -> Contract:
    path = contracts_dir / relative
    if not path.is_file():
        raise FileNotFoundError(f"Contract not found: {path}")
    return load_contract(path)
