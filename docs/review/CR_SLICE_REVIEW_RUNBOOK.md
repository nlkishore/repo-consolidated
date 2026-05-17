# CR Slice Review — Reviewer Runbook

Use this runbook when development **decouples one Change Request (CR)** from parallel CR work and builds a **slice branch** from a production baseline for expedited release.

**Design reference:** [CR_SLICE_REVIEW_AUTOMATION_DESIGN.md](CR_SLICE_REVIEW_AUTOMATION_DESIGN.md)

**Tool:** `git-tools/main/modules/python/cr_slice_validation/`

---

## When to use

- Multiple CRs each have their own PR sets on the same repository.
- Management requests shipping **one CR only** before others.
- Team created a **baseline branch** from production and a **slice branch** with cherry-picks/merges for that CR.
- You must confirm **nothing is missing** and **nothing from other CRs leaked** into the slice.

---

## Roles

| Role | Responsibility |
|------|----------------|
| **Dev lead** | Maintains `cr-manifest.yaml`, creates `expected_ref` integration branch |
| **Reviewer** | Runs reconciliation, reviews report, signs off or rejects |
| **CI** | Runs `run-all` on slice PR; blocks merge on FAIL |

---

## Prerequisites

1. Local clone of the application repository with `baseline_ref`, `expected_ref`, and `slice_ref` fetched.
2. `cr-manifest.yaml` committed under `.cr/` (or path agreed with team) listing all PRs for the CR.
3. Python 3.10+ on workstation or available in CI agent.

---

## Step 1 — Confirm CR scope with dev lead

Before running tooling, verify verbally or in the CR ticket:

- [ ] CR id (e.g. `CR-102`)
- [ ] List of **all** pull request ids included in the CR
- [ ] Production **baseline** tag/branch name
- [ ] Slice branch name ready for review

---

## Step 2 — Validate manifest

From `git-tools/main/modules/python`:

```bash
python -m cr_slice_validation validate-manifest --manifest /path/to/repo/.cr/CR-102-manifest.yaml
```

Fix any schema errors before continuing.

---

## Step 3 — Run reconciliation

```bash
python -m cr_slice_validation run-all \
  --manifest /path/to/repo/.cr/CR-102-manifest.yaml \
  --repo /path/to/app-repo \
  --slice-ref release-cr102 \
  --output /path/to/reconcile-report.json
```

Or via run-tool:

```bash
python run-tool.py cr_slice run-all \
  --manifest /path/to/repo/.cr/CR-102-manifest.yaml \
  --repo /path/to/app-repo \
  --slice-ref release-cr102
```

Optional HTML:

```bash
python -m cr_slice_validation reconcile ... --format html --output report.html
```

---

## Step 4 — Interpret the report

| Field | PASS criterion | Action if FAIL |
|-------|----------------|----------------|
| `status` | `PASS` | Do not merge until resolved |
| `missing_files` | Empty | Request dev restore dropped files/hunks from original PRs |
| `extra_files` | Empty or documented in manifest `notes` | Confirm intentional; reject if from another CR |
| `file_mismatches` | Empty | Diff specific paths vs original PR approvals |
| `unattributed_commits` | Empty or explained | Reject unexplained commits on slice |

Attach `reconcile-report.json` to the CR ticket and slice PR.

---

## Step 5 — Spot-check (human)

Automation does **not** replace functional QA. After PASS:

- [ ] Spot-check high-risk paths listed in the report (payment, auth, crypto, PII).
- [ ] Confirm QA sign-off for the CR still applies to the slice build.
- [ ] If slice feeds HotFix packaging, cross-check `fileLists.txt` per [HOTFIX_BRANCH_VALIDATION_DESIGN.md](../../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md).

---

## Step 6 — Sign-off

**Approve** slice PR when:

1. CI `validate-cr-slice` is green.
2. Report attached and `status: PASS`.
3. No open questions on `extra_files` or `unattributed_commits`.

**Waiver** (exceptional): Lead documents reason in manifest `notes` and ticket; CI uses `--allow-waiver` only per team policy.

---

## CI (slice PR)

Ensure the slice PR pipeline runs:

```bash
python -m cr_slice_validation run-all \
  --manifest "${CR_MANIFEST_PATH}" \
  --repo "${BITBUCKET_CLONE_DIR}" \
  --slice-ref "${BITBUCKET_BRANCH}"
```

See [bitbucket-pipelines-cr-slice.example.yml](../../git-tools/ci/bitbucket-pipelines-cr-slice.example.yml).

---

## Singapore / multi-instance note

If the decoupled CR includes **properties** changes, also run [PROPERTIES_DEPLOYMENT_VALIDATION_RUNBOOK.md](../deployment/PROPERTIES_DEPLOYMENT_VALIDATION_RUNBOOK.md) per instance before production.

---

## Quick reference — example manifest

```yaml
cr_id: CR-102
title: Payment validator fix
baseline_ref: release/2025.04.1
expected_ref: integration/cr-102-all-prs
slice_ref: release-cr102
pull_requests:
  - id: 451
  - id: 462
  - id: 471
exclude_paths:
  - "**/package-lock.json"
```

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `baseline_ref` not found | Tag not fetched | `git fetch --tags` |
| FAIL on file you expect | Wrong `expected_ref` | Rebuild integration branch from all PRs |
| Extra files on slice | Cherry-pick included other CR | Rebuild slice from clean baseline |
| Bitbucket errors | Missing token/env | Set `BITBUCKET_*` vars or use `expected_ref` only |
