# CR Slice Review Automation — Technical Design

**Document purpose:** Automate reviewer assurance when one Change Request (CR) is decoupled from parallel work on the same repository. Reconcile the **slice branch** (production-bound) against the **union of all PRs** registered for that CR, relative to a shared **baseline**.

**Traceability:** [Prompts.txt](../collected_prompt_usecases/Prompts.txt) (lines 81–84), plan *CR Decoupling Review Automation*.

**Related:**

- [CR_SLICE_REVIEW_RUNBOOK.md](CR_SLICE_REVIEW_RUNBOOK.md) — reviewer steps
- [HOTFIX_BRANCH_VALIDATION_DESIGN.md](../../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md) — baseline manifest pattern
- [APPROACH-PYTHON-PR-CODE-REVIEW.md](../../../Python-Cursor/pr-code-review-automation/docs/APPROACH-PYTHON-PR-CODE-REVIEW.md) — Bitbucket + diff reconciliation (conceptual alignment)

---

## 1. Problem statement

| Situation | Risk |
|-----------|------|
| Multiple CRs, each with several PRs on `release` | Reviewer sees one consolidation PR after decouple; cannot mentally diff N PRs |
| Team branches baseline from prod and cherry-picks one CR | Missing hunks, cross-CR bleed, untracked commits |
| Manual review | Hours of diff comparison; no audit artifact |

**Goal:** Answer *“Does `slice` ≡ union(CR-X PRs) relative to `baseline`?”* with machine-readable PASS/FAIL and CI gate.

---

## 2. Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│ cr_slice_validation (Python CLI / CI)                            │
└───────────────────────────────┬─────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
┌───────────────┐     ┌─────────────────┐     ┌──────────────────┐
│ cr-manifest   │     │ git (local repo) │     │ Bitbucket REST   │
│ YAML          │     │ diff / log       │     │ (optional PR meta)│
└───────────────┘     └─────────────────┘     └──────────────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │ reconciler            │
                    │ file set + patch hash │
                    └───────────┬───────────┘
                                ▼
                    ┌───────────────────────┐
                    │ JSON / HTML report    │
                    └───────────────────────┘
```

---

## 3. CR manifest schema (`cr-manifest.yaml`)

| Field | Required | Description |
|-------|----------|-------------|
| `cr_id` | Yes | Change request id, e.g. `CR-102` |
| `title` | No | Human-readable title |
| `baseline_ref` | Yes | Git ref for production baseline (tag or branch) |
| `expected_ref` | No* | Branch/ref that already contains union of CR PRs (recommended) |
| `slice_ref` | No** | Decoupled branch under review (CLI `--slice-ref` overrides) |
| `pull_requests` | No | List of `{id: N}` for Bitbucket Server (metadata + attribution) |
| `pr_branches` | No | Branch names to union when `expected_ref` omitted |
| `optional_commits` | No | Extra SHAs included in expected set |
| `exclude_paths` | No | Glob patterns (fnmatch) omitted from comparison |
| `notes` | No | Waiver / conflict resolution notes for auditors |

\* If omitted, tool uses `pr_branches` or fetches PR `fromRef` via Bitbucket (requires env config).

\** Usually passed on CLI for CI: `--slice-ref $BITBUCKET_BRANCH`.

**Example:**

```yaml
cr_id: CR-102
title: Singapore payment fix
baseline_ref: release/2025.04.1
expected_ref: integration/cr-102-all-prs
slice_ref: release-cr102
pull_requests:
  - id: 451
  - id: 462
  - id: 471
exclude_paths:
  - "**/package-lock.json"
notes: ""
```

**Validation rules:**

- `cr_id` matches `^CR-[A-Za-z0-9._-]+$` (configurable)
- At least one of: `expected_ref`, non-empty `pr_branches`, or non-empty `pull_requests` (with Bitbucket credentials)
- `baseline_ref` and `slice_ref` must resolve via `git rev-parse` in the target repo

---

## 4. Reconciliation algorithm

### 4.1 Resolve SHAs

1. `baseline_sha = git rev-parse baseline_ref`
2. `slice_sha = git rev-parse slice_ref` (CLI or manifest)
3. `expected_sha`:
   - If `expected_ref` set → `git rev-parse expected_ref`
   - Else if `pr_branches` → synthetic merge-base union (see §4.3)
   - Else if `pull_requests` + Bitbucket → fetch each PR head; build union diff (§4.3)

### 4.2 File-level comparison

For refs `BASE`, `EXPECTED`, `ACTUAL`:

```
paths_expected = git diff --name-only BASE..EXPECTED  (minus exclude_paths)
paths_actual   = git diff --name-only BASE..ACTUAL
missing_files  = paths_expected - paths_actual
extra_files    = paths_actual - paths_expected
```

### 4.3 Content parity

For each path in `paths_expected ∩ paths_actual`:

```
patch_e = git diff BASE..EXPECTED -- path
patch_a = git diff BASE..ACTUAL   -- path
```

Normalize: CRLF → LF, strip trailing whitespace per line.

- If normalized patches equal → OK
- Else record in `file_mismatches[]` with short hash prefix

For paths only in expected or only in actual, no patch compare (already in missing/extra).

### 4.4 Commit attribution (slice only)

```
commits_actual = git log --format=%H BASE..ACTUAL
commits_from_manifest_prs = union of merge commits / PR heads (if known)
unattributed_commits = commits_actual not reachable from manifest PR heads
```

Phase 1: list all commits on slice not contained in `optional_commits` + PR head SHAs when Bitbucket/git refs available. Warn if non-empty.

### 4.5 Line-level (optional, `--line-level`)

Parse unified diffs with `diff_parser.parse_unified_diff`; flag line ranges in expected patch missing from actual patch.

---

## 5. CLI contract

Package: `git-tools/main/modules/python/cr_slice_validation/`

| Command | Description |
|---------|-------------|
| `validate-manifest <file>` | Schema check only |
| `reconcile --manifest FILE --repo PATH [--slice-ref REF] [--expected-ref REF] [--output PATH]` | Main comparison |
| `run-all` | `validate-manifest` + `reconcile`; exit 1 on FAIL |
| `build-manifest` | Phase 2 stub: emit manifest skeleton from `--cr-id` + Bitbucket label query |

**Exit codes:** `0` = PASS, `1` = FAIL (missing/extra/mismatch), `2` = usage/config error.

**Environment (optional Bitbucket):**

| Variable | Purpose |
|----------|---------|
| `BITBUCKET_SERVER_URL` | e.g. `https://bitbucket.example.com` |
| `BITBUCKET_TOKEN` | HTTP access token |
| `BITBUCKET_PROJECT_KEY` | Project key |
| `BITBUCKET_REPO_SLUG` | Repository slug |

---

## 6. Report schema (JSON)

```json
{
  "cr_id": "CR-102",
  "status": "PASS|FAIL",
  "baseline_ref": "release/2025.04.1",
  "baseline_sha": "abc123",
  "expected_ref": "integration/cr-102-all-prs",
  "expected_sha": "def456",
  "slice_ref": "release-cr102",
  "slice_sha": "789abc",
  "summary": {
    "files_expected": 24,
    "files_actual": 24,
    "missing": 0,
    "extra": 0,
    "mismatches": 0
  },
  "missing_files": [],
  "extra_files": [],
  "file_mismatches": [],
  "unattributed_commits": []
}
```

Reports contain **paths and hashes only** — no file bodies, no secrets.

---

## 7. CI integration contract

### 7.1 Bitbucket Pipelines

See [bitbucket-pipelines-cr-slice.example.yml](../../git-tools/ci/bitbucket-pipelines-cr-slice.example.yml).

Required variables in repository/deployment settings:

- `CR_MANIFEST_PATH` — e.g. `.cr/CR-102-manifest.yaml`
- `CR_SLICE_REF` — branch under test (default: `BITBUCKET_BRANCH`)

### 7.2 Jenkins (illustrative stage)

```groovy
stage('Validate CR slice') {
  steps {
    dir('git-tools/main/modules/python') {
      sh '''
        python -m cr_slice_validation run-all \
          --manifest "${CR_MANIFEST_PATH}" \
          --repo "${WORKSPACE}" \
          --slice-ref "${CR_SLICE_REF}"
      '''
    }
    archiveArtifacts artifacts: 'reconcile-report.json', allowEmptyArchive: false
  }
}
```

### 7.3 Merge policy

- Slice consolidation PR **requires** green `validate-cr-slice` check.
- Waiver: document in manifest `notes` + ticket; use `--allow-waiver` only in controlled environments.

---

## 8. Phased delivery status

| Phase | Scope | Status |
|-------|--------|--------|
| 1 | Manifest, git reconcile, JSON report, examples | Implemented (this repo) |
| 2 | `build-manifest`, HTML report, PR comment bot | Planned |
| 3 | HotFix fileLists cross-check, cross-CR overlap warnings | Planned |

---

## 9. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| Squash-merge loses PR boundaries | Manifest lists PR ids; `expected_ref` branch built by integration lead |
| Binary noise | `exclude_paths` globs |
| False green on semantic equivalence | Patch hash compare; optional `--line-level` |
| Two Python codebases | Self-contained `diff_parser` in package; optional Bitbucket via httpx |

---

## 10. Tool location

```text
git-tools/main/modules/python/cr_slice_validation/
docs/review/CR_SLICE_REVIEW_AUTOMATION_DESIGN.md
docs/review/CR_SLICE_REVIEW_RUNBOOK.md
git-tools/ci/bitbucket-pipelines-cr-slice.example.yml
```

Run via:

```bash
cd git-tools/main/modules/python
python -m cr_slice_validation run-all --manifest path/to/cr-manifest.yaml --repo /path/to/app-repo
```

Or: `python run-tool.py cr_slice run-all ...`
