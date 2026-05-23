"""Contract dataclasses loaded from YAML."""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class ContractElement:
    id: str
    name: str
    required: bool = True
    description: str = ""
    path: str = ""          # JSONPath-style for IDB
    xpath: str = ""         # XPath for EAI XML
    type: str = "string"
    max_length: int | None = None
    pattern: str | None = None
    maps_from: str | None = None


@dataclass
class Contract:
    api: str = ""
    message: str = ""
    version: str = "1.0"
    direction: str = "inbound"
    format: str = "json"
    root: str = ""
    elements: list[ContractElement] = field(default_factory=list)

    @property
    def label(self) -> str:
        return self.api or self.message or "contract"
