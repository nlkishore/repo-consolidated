# CR Slice Validation

Reconcile a **decoupled slice branch** against the union of changes for one Change Request (CR), relative to a production **baseline**.

**Design:** [docs/review/CR_SLICE_REVIEW_AUTOMATION_DESIGN.md](../../../docs/review/CR_SLICE_REVIEW_AUTOMATION_DESIGN.md)  
**Runbook:** [docs/review/CR_SLICE_REVIEW_RUNBOOK.md](../../../docs/review/CR_SLICE_REVIEW_RUNBOOK.md)

## Install

```bash
cd git-tools/main/modules/python
pip install -r requirements-cr-slice-validation.txt
```

## Commands

```bash
python -m cr_slice_validation validate-manifest --manifest .cr/CR-102-manifest.yaml

python -m cr_slice_validation run-all \
  --manifest .cr/CR-102-manifest.yaml \
  --repo /path/to/app-repo \
  --slice-ref release-cr102 \
  --output reconcile-report.json

python run-tool.py cr_slice run-all --manifest .cr/CR-102-manifest.yaml --repo .
```

## Manifest

See [examples/cr-102-manifest.yaml](cr_slice_validation/examples/cr-102-manifest.yaml).

## Bitbucket (optional)

Set when resolving `pull_requests` without `expected_ref`:

- `BITBUCKET_SERVER_URL`
- `BITBUCKET_TOKEN`
- `BITBUCKET_PROJECT_KEY`
- `BITBUCKET_REPO_SLUG`

Use `--use-bitbucket` on `reconcile` / `run-all`.

## CI

[bitbucket-pipelines-cr-slice.example.yml](../../ci/bitbucket-pipelines-cr-slice.example.yml)
