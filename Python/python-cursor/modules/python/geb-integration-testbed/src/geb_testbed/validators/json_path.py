"""Minimal JSONPath ($.a.b.c) resolver for contract validation."""

from __future__ import annotations

import re
from typing import Any


def resolve_path(data: Any, path: str) -> tuple[bool, Any]:
    """Return (found, value) for a $.dot.path expression."""
    if not path.startswith("$."):
        return False, None
    parts = path[2:].split(".")
    current: Any = data
    for part in parts:
        if not isinstance(current, dict) or part not in current:
            return False, None
        current = current[part]
    return True, current


def validate_type(value: Any, type_name: str) -> bool:
    if type_name == "string":
        return isinstance(value, str)
    if type_name == "integer":
        return isinstance(value, int) and not isinstance(value, bool)
    if type_name == "decimal":
        if isinstance(value, (int, float)) and not isinstance(value, bool):
            return True
        if isinstance(value, str):
            return bool(re.fullmatch(r"^\d+(\.\d{1,2})?$", value))
        return False
    if type_name == "boolean":
        return isinstance(value, bool)
    if type_name == "object":
        return isinstance(value, dict)
    return True


def validate_constraints(
    value: Any,
    *,
    max_length: int | None,
    pattern: str | None,
) -> list[str]:
    errors: list[str] = []
    if max_length is not None and isinstance(value, str) and len(value) > max_length:
        errors.append(f"length {len(value)} exceeds maxLength {max_length}")
    if pattern and isinstance(value, str) and not re.fullmatch(pattern, value):
        errors.append(f"value does not match pattern {pattern}")
    return errors
