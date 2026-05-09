# HotFix validation (Python)

Implements validators described in `git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md`.

## Layout

| Path | Role |
|------|------|
| `hotfix_validation/` | Package (`validate-file-lists`, `check-paired`, `validate-baseline`, `run-all`) |
| `hotfix_validation/examples/` | Sample `fileLists` YAML and release manifest JSON |
| `requirements-hotfix-validation.txt` | Dependencies |

## Setup

From this directory (`main/modules/python`):

```bash
pip install -r requirements-hotfix-validation.txt
```

## CLI

```bash
python -m hotfix_validation --help
python -m hotfix_validation validate-file-lists hotfix_validation/examples/sample_fileLists.txt
python -m hotfix_validation check-paired --staged --repo .
python -m hotfix_validation check-paired --rev <SHA> --repo .
python -m hotfix_validation validate-baseline hotfix_validation/examples/sample_fileLists.txt hotfix_validation/examples/sample_release_manifest.json
python -m hotfix_validation run-all --file-lists path/to/fileLists.txt --staged --repo . --manifest path/to/release-build-manifest.json
```

## `run-tool.py` wrapper

```bash
python run-tool.py hotfix_validate validate-file-lists hotfix_validation/examples/sample_fileLists.txt
```

## CI snippets

- Pre-commit (staged files): `python -m hotfix_validation check-paired --staged --repo ${{ github.workspace }}`
- Push (single commit): `python -m hotfix_validation check-paired --rev $CI_COMMIT_SHA --repo .`
- PR range: `python -m hotfix_validation check-paired --range-base origin/release --range-head HEAD --repo .`

Release pipeline should publish `release-build-manifest.json` (see design doc §6); pass its path to `validate-baseline` or `run-all --manifest`.
