from __future__ import annotations

import csv
import re
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import javalang

IMPORT_RE = re.compile(r"^\s*import\s+(static\s+)?([a-zA-Z0-9_.*$]+)\s*;", re.MULTILINE)


@dataclass
class ParseIssue:
    file_path: str
    error_type: str
    message: str


def _type_to_text(type_node: Any) -> str:
    if type_node is None:
        return "void"

    name = getattr(type_node, "name", str(type_node))
    args = getattr(type_node, "arguments", None)
    dims = getattr(type_node, "dimensions", None)

    generic = ""
    if args:
        parts: list[str] = []
        for arg in args:
            t = getattr(arg, "type", arg)
            parts.append(_type_to_text(t))
        generic = "<" + ", ".join(parts) + ">"

    array_suffix = ""
    if dims:
        array_suffix = "[]" * len(dims)

    return f"{name}{generic}{array_suffix}"


def _param_to_text(param: Any) -> str:
    p_type = _type_to_text(getattr(param, "type", None))
    varargs = "..." if getattr(param, "varargs", False) else ""
    pname = getattr(param, "name", "arg")
    return f"{p_type}{varargs} {pname}".strip()


def _safe_rel_parent(root: Path, file_path: Path) -> str:
    rel_parent = file_path.parent.relative_to(root)
    text = str(rel_parent).replace("\\", "/")
    return text if text else "."


def _extract_import_rows(source: str, folder_name: str, java_file_name: str) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for m in IMPORT_RE.finditer(source):
        is_static = bool(m.group(1))
        import_stmt = m.group(2)
        rows.append(
            {
                "folder_name": folder_name,
                "java_file_name": java_file_name,
                "import_statement": import_stmt,
                "is_static": str(is_static).lower(),
                "is_wildcard": str(import_stmt.endswith(".*")).lower(),
            }
        )
    return rows


def _build_signature(
    modifiers: list[str],
    return_type: str,
    method_name: str,
    params: str,
    throws: str,
) -> str:
    prefix = (" ".join(sorted(modifiers)) + " ").strip()
    if prefix:
        prefix += " "
    sig = f"{prefix}{return_type} {method_name}({params})"
    if throws:
        sig += f" throws {throws}"
    return sig


def scan_java_project(root: Path, encoding: str = "utf-8") -> dict[str, list[dict[str, str]]]:
    imports: list[dict[str, str]] = []
    methods: list[dict[str, str]] = []
    classes: list[dict[str, str]] = []
    parse_issues: list[ParseIssue] = []
    package_counter: Counter[str] = Counter()

    for file_path in root.rglob("*.java"):
        java_file_name = file_path.name
        folder_name = _safe_rel_parent(root, file_path)

        try:
            source = file_path.read_text(encoding=encoding, errors="replace")
        except Exception as e:  # pragma: no cover
            parse_issues.append(ParseIssue(str(file_path), type(e).__name__, str(e)))
            continue

        imports.extend(_extract_import_rows(source, folder_name, java_file_name))

        package_name = ""
        try:
            tree = javalang.parse.parse(source)
            if tree.package and tree.package.name:
                package_name = tree.package.name
                package_counter[package_name] += 1

            for _, type_decl in tree.filter(javalang.tree.TypeDeclaration):
                class_name = getattr(type_decl, "name", "")
                kind = "class"
                if isinstance(type_decl, javalang.tree.InterfaceDeclaration):
                    kind = "interface"
                elif isinstance(type_decl, javalang.tree.EnumDeclaration):
                    kind = "enum"

                extends_name = ""
                ext = getattr(type_decl, "extends", None)
                if ext is not None:
                    extends_name = getattr(ext, "name", "")

                impl = getattr(type_decl, "implements", None) or []
                impl_text = ", ".join(getattr(i, "name", "") for i in impl if getattr(i, "name", ""))

                classes.append(
                    {
                        "folder_name": folder_name,
                        "java_file_name": java_file_name,
                        "package_name": package_name,
                        "type_kind": kind,
                        "type_name": class_name,
                        "modifiers": " ".join(sorted(getattr(type_decl, "modifiers", []) or [])),
                        "extends": extends_name,
                        "implements": impl_text,
                    }
                )

                for body_decl in getattr(type_decl, "body", []):
                    if isinstance(body_decl, javalang.tree.MethodDeclaration):
                        method_name = body_decl.name
                        return_type = _type_to_text(body_decl.return_type)
                        modifiers = list(getattr(body_decl, "modifiers", []) or [])
                        params_list = [_param_to_text(p) for p in getattr(body_decl, "parameters", [])]
                        params = ", ".join(params_list)
                        throws_list = [_type_to_text(t) for t in getattr(body_decl, "throws", []) or []]
                        throws_text = ", ".join(throws_list)
                        signature = _build_signature(modifiers, return_type, method_name, params, throws_text)
                        line_no = str(getattr(getattr(body_decl, "position", None), "line", "") or "")

                        methods.append(
                            {
                                "folder_name": folder_name,
                                "java_file_name": java_file_name,
                                "class_name": class_name,
                                "method_name": method_name,
                                "return_type": return_type,
                                "parameters": params,
                                "modifiers": " ".join(sorted(modifiers)),
                                "throws": throws_text,
                                "signature_text": signature,
                                "line_number": line_no,
                            }
                        )

                    if isinstance(body_decl, javalang.tree.ConstructorDeclaration):
                        ctor_name = body_decl.name
                        modifiers = list(getattr(body_decl, "modifiers", []) or [])
                        params_list = [_param_to_text(p) for p in getattr(body_decl, "parameters", [])]
                        params = ", ".join(params_list)
                        throws_list = [_type_to_text(t) for t in getattr(body_decl, "throws", []) or []]
                        throws_text = ", ".join(throws_list)
                        signature = _build_signature(modifiers, ctor_name, ctor_name, params, throws_text)
                        line_no = str(getattr(getattr(body_decl, "position", None), "line", "") or "")

                        methods.append(
                            {
                                "folder_name": folder_name,
                                "java_file_name": java_file_name,
                                "class_name": class_name,
                                "method_name": ctor_name,
                                "return_type": "constructor",
                                "parameters": params,
                                "modifiers": " ".join(sorted(modifiers)),
                                "throws": throws_text,
                                "signature_text": signature,
                                "line_number": line_no,
                            }
                        )

        except Exception as e:
            parse_issues.append(ParseIssue(str(file_path), type(e).__name__, str(e)))

    packages = [
        {"package_name": pkg, "file_count": str(cnt)}
        for pkg, cnt in sorted(package_counter.items(), key=lambda x: x[0])
    ]

    issue_rows = [
        {"file_path": p.file_path, "error_type": p.error_type, "message": p.message}
        for p in parse_issues
    ]

    return {
        "imports": imports,
        "method_signatures": methods,
        "classes": classes,
        "packages": packages,
        "parse_issues": issue_rows,
    }


def write_csv(path: Path, rows: list[dict[str, str]], fieldnames: list[str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow({k: row.get(k, "") for k in fieldnames})


def scan_torque_schemas(root: Path, encoding: str = "utf-8") -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    xml_files = list(root.rglob("*.xml"))
    for p in xml_files:
        rel = str(p.relative_to(root)).replace("\\", "/")
        name_low = p.name.lower()
        rel_low = rel.lower()
        if "schema" not in name_low and "/torque-schema/" not in rel_low and "/schema/" not in rel_low:
            continue
        try:
            text = p.read_text(encoding=encoding, errors="replace")
        except Exception as e:  # pragma: no cover
            rows.append(
                {
                    "file_path": rel,
                    "schema_type": "unreadable",
                    "root_tag": "",
                    "uses_dtd": "false",
                    "uses_torque_xsd": "false",
                    "has_interface_attr": "false",
                    "has_base_class_attr": "false",
                    "has_peer_interface_attr": "false",
                    "recommended_migration_mode": "manual_review",
                    "notes": f"read_error:{type(e).__name__}",
                }
            )
            continue

        uses_dtd = "<!doctype database" in text.lower()
        uses_torque_xsd = "db.apache.org/torque/" in text.lower() and "xsd" in text.lower()
        has_interface_attr = bool(re.search(r"\binterface\s*=", text))
        has_base_class_attr = bool(re.search(r"\bbaseClass\s*=", text))
        has_peer_interface_attr = bool(re.search(r"\bpeerInterface\s*=", text))
        root_tag_match = re.search(r"<\s*(database|schema)\b", text, flags=re.IGNORECASE)
        root_tag = root_tag_match.group(1).lower() if root_tag_match else ""

        if uses_dtd:
            schema_type = "torque3_dtd"
            mode = "legacy_mapbuilder_or_manual_bridge"
            notes = "Legacy DTD schema; interface/baseClass attributes are normally unavailable."
        elif uses_torque_xsd and (has_interface_attr or has_base_class_attr or has_peer_interface_attr):
            schema_type = "torque5_xsd_interface_capable"
            mode = "schema_driven_interface_generation"
            notes = "Schema supports interface/baseClass/peerInterface mapping."
        elif uses_torque_xsd:
            schema_type = "torque5_xsd_basic"
            mode = "schema_codegen_without_interface_contracts"
            notes = "XSD schema present; interface mapping attributes not found."
        else:
            schema_type = "generic_xml_or_unknown"
            mode = "manual_review"
            notes = "Could not confidently classify as Torque DTD/XSD schema."

        rows.append(
            {
                "file_path": rel,
                "schema_type": schema_type,
                "root_tag": root_tag,
                "uses_dtd": str(uses_dtd).lower(),
                "uses_torque_xsd": str(uses_torque_xsd).lower(),
                "has_interface_attr": str(has_interface_attr).lower(),
                "has_base_class_attr": str(has_base_class_attr).lower(),
                "has_peer_interface_attr": str(has_peer_interface_attr).lower(),
                "recommended_migration_mode": mode,
                "notes": notes,
            }
        )

    return sorted(rows, key=lambda x: x["file_path"])
