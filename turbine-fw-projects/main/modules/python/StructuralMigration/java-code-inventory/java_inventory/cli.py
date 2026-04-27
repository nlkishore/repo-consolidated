from __future__ import annotations

import argparse
from pathlib import Path

from java_inventory.scanner import scan_java_project, scan_torque_schemas, write_csv


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Scan Java project and export imports/method signatures CSV reports."
    )
    p.add_argument("--root", required=True, help="Root folder to recursively scan for *.java files")
    p.add_argument(
        "--output",
        default="./output",
        help="Output folder for CSV files (default: ./output)",
    )
    p.add_argument(
        "--encoding",
        default="utf-8",
        help="Source file encoding (default: utf-8)",
    )
    return p


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()

    root = Path(args.root).expanduser().resolve()
    output = Path(args.output).expanduser().resolve()

    if not root.exists() or not root.is_dir():
        parser.error(f"--root must be an existing directory: {root}")

    result = scan_java_project(root=root, encoding=args.encoding)

    write_csv(
        output / "imports.csv",
        result["imports"],
        ["folder_name", "java_file_name", "import_statement", "is_static", "is_wildcard"],
    )
    write_csv(
        output / "method_signatures.csv",
        result["method_signatures"],
        [
            "folder_name",
            "java_file_name",
            "class_name",
            "method_name",
            "return_type",
            "parameters",
            "modifiers",
            "throws",
            "signature_text",
            "line_number",
        ],
    )
    write_csv(
        output / "classes.csv",
        result["classes"],
        [
            "folder_name",
            "java_file_name",
            "package_name",
            "type_kind",
            "type_name",
            "modifiers",
            "extends",
            "implements",
        ],
    )
    write_csv(output / "packages.csv", result["packages"], ["package_name", "file_count"])
    write_csv(output / "parse_issues.csv", result["parse_issues"], ["file_path", "error_type", "message"])
    schema_rows = scan_torque_schemas(root=root, encoding=args.encoding)
    write_csv(
        output / "schema_classification.csv",
        schema_rows,
        [
            "file_path",
            "schema_type",
            "root_tag",
            "uses_dtd",
            "uses_torque_xsd",
            "has_interface_attr",
            "has_base_class_attr",
            "has_peer_interface_attr",
            "recommended_migration_mode",
            "notes",
        ],
    )

    print(f"Scan completed.\nRoot: {root}\nOutput: {output}")
    for name, rows in result.items():
        print(f"- {name}: {len(rows)}")
    print(f"- schema_classification: {len(schema_rows)}")
    return 0
