"""JSON Schema for fileLists.txt YAML (design doc §5)."""

from __future__ import annotations

FILE_LISTS_DOCUMENT_SCHEMA: dict = {
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "required": ["version", "entries"],
    "additionalProperties": True,
    "properties": {
        "version": {"type": "integer", "minimum": 1},
        "baseline": {
            "type": "object",
            "additionalProperties": False,
            "properties": {
                "release_commit": {"type": "string", "minLength": 7},
                "full_build_id": {"type": "string", "minLength": 1},
            },
        },
        "entries": {
            "type": "array",
            "minItems": 1,
            "items": {
                "type": "object",
                "required": ["path"],
                "additionalProperties": True,
                "properties": {
                    "path": {"type": "string", "minLength": 1},
                    "reason": {"type": "string"},
                },
            },
        },
    },
}
