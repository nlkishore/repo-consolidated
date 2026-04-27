# Java Code Inventory Scanner

This utility scans any Java codebase and exports CSV reports for:

1. **Imports inventory**  
   `folder_name, java_file_name, import_statement, is_static, is_wildcard`
2. **Method signatures inventory**  
   `folder_name, java_file_name, class_name, method_name, return_type, parameters, modifiers, throws, signature_text, line_number`

## Extra recommended reports (also generated)

3. **Class inventory** (`classes.csv`)  
   package, class/interface/enum, modifiers, extends/implements
4. **Package inventory** (`packages.csv`)  
   package name, file count
5. **Parse issues** (`parse_issues.csv`)  
   files that failed full AST parse (scanner still captures imports with fallback)
6. **Schema classification** (`schema_classification.csv`)  
   detects Torque schema style (`torque3_dtd`, `torque5_xsd_*`) and whether interface/baseClass/peerInterface attributes are present

## Why these extra reports?

- Class and package inventories help plan migration waves by module.
- Parse issue report highlights files needing manual cleanup before automation.
- Combined with method signatures, this helps generate safer OpenRewrite recipes.

## Setup

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Run

```bash
python run_inventory.py --root "C:\path\to\any\java\root" --output ".\output"
```

### Arguments

- `--root` (required): root folder to scan (configurable; works from anywhere)
- `--output` (optional): output folder for CSVs (default: `./output`)
- `--encoding` (optional): file encoding (default: `utf-8`)

## Output files

- `imports.csv`
- `method_signatures.csv`
- `classes.csv`
- `packages.csv`
- `parse_issues.csv`
- `schema_classification.csv`

## Notes

- Parser uses `javalang` AST for signatures and structure.
- Import extraction has a regex fallback so import CSV remains useful even if parsing fails.
