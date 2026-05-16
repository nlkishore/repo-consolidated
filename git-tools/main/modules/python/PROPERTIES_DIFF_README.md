# Properties diff (Python)

Compare Java `.properties` files: **production baseline** vs **UAT/candidate**, per file or per directory tree. Supports multi-instance deployment validation (e.g. Singapore `GEBCUSG*` / `GEBBASG*` hosts).

Stdlib only — no extra pip dependencies.

## Layout

| Path | Role |
|------|------|
| `properties_diff/` | Package (`compare-files`, `compare-trees`) |
| `properties_diff/examples/sg/` | Sample prod vs UAT trees |
| `docs/deployment/PROPERTIES_DEPLOYMENT_VALIDATION_RUNBOOK.md` | Operational runbook |

## Setup

From `git-tools/main/modules/python` (no install step required if Python 3.9+ is on PATH):

```bash
cd git-tools/main/modules/python
python -m properties_diff --help
```

## CLI

### Compare one file

```bash
python -m properties_diff compare-files \
  --baseline path/to/prod/application.properties \
  --candidate path/to/uat/application.properties \
  --relative-path conf/application.properties
```

### Compare directory trees (per instance)

```bash
python -m properties_diff compare-trees \
  --baseline-root exports/GEBCUSG01A/prod \
  --candidate-root exports/GEBCUSG01A/uat
```

### Options

| Flag | Description |
|------|-------------|
| `--format text\|json` | Human report or machine-readable JSON |
| `--include-removed` | List keys dropped from candidate |
| `--encoding utf-8` | File encoding (`iso-8859-1` for legacy exports) |
| `--redact REGEX` | Mask values matching pattern in output (repeatable) |
| `--no-fail-on-diff` | Exit 0 even when differences exist |
| `--glob "**/*.properties"` | Which files under roots to compare |

Exit code **1** (default) when there are new keys, changed values, or candidate-only property files.

## `run-tool.py` wrapper

```bash
python run-tool.py properties_diff compare-trees \
  --baseline-root properties_diff/examples/sg/GEBCUSG01A/prod \
  --candidate-root properties_diff/examples/sg/GEBCUSG01A/uat
```

## Sample run

```bash
python -m properties_diff compare-trees \
  --baseline-root properties_diff/examples/sg/GEBCUSG01A/prod \
  --candidate-root properties_diff/examples/sg/GEBCUSG01A/uat
```

JSON + redaction:

```bash
python -m properties_diff compare-files \
  --baseline properties_diff/examples/sg/GEBCUSG01A/prod/conf/application.properties \
  --candidate properties_diff/examples/sg/GEBCUSG01A/uat/conf/application.properties \
  --format json \
  --redact "db\." \
  --redact "password"
```

## Programmatic use

```python
from pathlib import Path
from properties_diff import compare_trees

result = compare_trees(
    Path("exports/GEBCUSG01A/prod"),
    Path("exports/GEBCUSG01A/uat"),
)
if result.has_rollout_risk:
    ...
```
