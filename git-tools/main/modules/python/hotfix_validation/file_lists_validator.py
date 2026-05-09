"""Validate fileLists.txt as YAML against schema and path rules."""

from __future__ import annotations

import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, List, Optional

import yaml

try:
    import jsonschema
except ImportError as exc:  # pragma: no cover - surfaced at runtime
    jsonschema = None  # type: ignore
    _IMPORT_ERROR = exc
else:
    _IMPORT_ERROR = None

from hotfix_validation.config import PathRules, DEFAULT_PATH_RULES
from hotfix_validation.schema import FILE_LISTS_DOCUMENT_SCHEMA


@dataclass
class ValidationResult:
    ok: bool
    errors: List[str] = field(default_factory=list)
    entry_count: int = 0
    paths: List[str] = field(default_factory=list)


def _normalize_path(path: str, rules: PathRules) -> str:
    p = path.strip().replace("\\", "/") if rules.normalize_separators else path.strip()
    while "//" in p:
        p = p.replace("//", "/")
    return p


def _check_path_segment_rules(normalized: str, rules: PathRules) -> Optional[str]:
    segments = normalized.split("/")
    if rules.forbid_parent_segments and ".." in segments:
        return f"path must not contain '..' segment: {normalized!r}"
    if rules.allowed_prefixes:
        if not any(normalized.startswith(pref) for pref in rules.allowed_prefixes):
            allowed = ", ".join(repr(p) for p in rules.allowed_prefixes)
            return f"path must start with one of [{allowed}]: {normalized!r}"
    return None


class FileListsValidator:
    """Loads YAML fileLists, validates schema and optional path rules."""

    def __init__(
        self,
        path_rules: Optional[PathRules] = None,
        schema: Optional[dict] = None,
    ):
        self.path_rules = path_rules or DEFAULT_PATH_RULES
        self.schema = schema or FILE_LISTS_DOCUMENT_SCHEMA

    def validate_file(self, file_path: Path) -> ValidationResult:
        errors: List[str] = []
        if not file_path.is_file():
            return ValidationResult(ok=False, errors=[f"not a file: {file_path}"])

        try:
            raw = file_path.read_text(encoding="utf-8")
        except OSError as exc:
            return ValidationResult(ok=False, errors=[f"read failed: {exc}"])

        try:
            data = yaml.safe_load(raw)
        except yaml.YAMLError as exc:
            return ValidationResult(ok=False, errors=[f"YAML syntax error: {exc}"])

        if data is None:
            return ValidationResult(ok=False, errors=["YAML document is empty"])

        if not isinstance(data, dict):
            return ValidationResult(
                ok=False, errors=[f"root must be a mapping, got {type(data).__name__}"]
            )

        schema_errors = self._validate_schema(data)
        errors.extend(schema_errors)

        paths: List[str] = []
        entries = data.get("entries") if isinstance(data.get("entries"), list) else []
        for i, item in enumerate(entries):
            if not isinstance(item, dict):
                errors.append(f"entries[{i}] must be an object")
                continue
            p = item.get("path")
            if not isinstance(p, str):
                errors.append(f"entries[{i}].path must be a non-empty string")
                continue
            norm = _normalize_path(p, self.path_rules)
            seg_err = _check_path_segment_rules(norm, self.path_rules)
            if seg_err:
                errors.append(seg_err)
            else:
                paths.append(norm)

        ok = len(errors) == 0
        return ValidationResult(
            ok=ok,
            errors=errors,
            entry_count=len(paths),
            paths=paths,
        )

    def _validate_schema(self, data: dict[str, Any]) -> List[str]:
        if _IMPORT_ERROR is not None or jsonschema is None:
            return [
                "jsonschema is required for schema validation. "
                "Install: pip install -r requirements-hotfix-validation.txt"
            ]
        try:
            jsonschema.validate(instance=data, schema=self.schema)
        except jsonschema.ValidationError as exc:
            return [f"schema: {exc.message} at {list(exc.path)}"]

        return []


def main_argv(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    if len(argv) < 1:
        print("usage: validate-file-lists <path-to-fileLists.txt>", file=sys.stderr)
        return 2

    path = Path(argv[0]).resolve()
    result = FileListsValidator().validate_file(path)
    if result.ok:
        print(f"OK: {result.entry_count} entr(y/ies)")
        for p in result.paths:
            print(f"  - {p}")
        return 0

    for msg in result.errors:
        print(msg, file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main_argv())
