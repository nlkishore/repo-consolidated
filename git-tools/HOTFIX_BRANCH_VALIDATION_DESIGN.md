# HotFix Branch Validation — Detailed Design

**Document purpose:** Translate the workflow described in `Prompts.txt` (lines 49–66) into enforceable, systematic validations before changes land on the HotFix build branch.

**Scope:** Bitbucket Server–centric SCM, `release` branch (full build binary), HotFix branch containing `buildScripts.sh` and `fileLists.txt`, incremental zip packaging for deployment.

---

## 1. Context summary

| Element | Description |
|--------|-------------|
| **Release branch** | Primary integration line; post-merge CI produces the **full build binary** (artifact). |
| **Developer flow** | Branch from `release` → develop → PR → merge to `release` → automated full build. |
| **HotFix branch** | Holds `buildScripts.sh` and `fileLists.txt`; scripts pick listed paths from the **latest full build** and produce an incremental zip for HotFix deployment. |
| **Pain point** | Incorrect or inconsistent edits to `fileLists.txt` (and related script drift); no guarantee that a matching release build exists before HotFix packaging. |
| **Goals** | (1) Validate `fileLists.txt` structure (YAML), (2) require evidence that release full build completed, (3) require `buildScripts.sh` and `fileLists.txt` to change together when either changes. |

---

## 2. Design principles

1. **Fail fast:** Catch errors at commit time (developer workstation) and again at push/merge time (server).
2. **Single source of truth:** Release build status comes from CI metadata or artifact registry, not manual checkbox.
3. **Explicit schema:** `fileLists.txt` is treated as YAML (or renamed to `fileLists.yaml` long-term) with a documented schema.
4. **Atomic hotfix metadata:** Script + list stay in sync via validation rules and optional Git hooks.

---

## 3. Target validations (requirements traceability)

| Requirement | Enforcement layer | Notes |
|-------------|-------------------|--------|
| `fileLists.txt` conforms to standard YAML structure | Local hook + CI + optional PR merge check | Parser + schema (see §5). |
| Full build on `release` completed before HotFix picks files | CI on HotFix branch + optional manual override with audit | Compare commit SHA / build ID against latest successful release pipeline artifact (see §6). |
| `buildScripts.sh` and `fileLists.txt` committed together | Pre-commit / pre-push hook + CI job | Reject commits that touch one without the other (with documented exceptions in §7). |

---

## 4. High-level architecture

```
┌─────────────────────────────────────────────────────────────────┐
│ Developer workstation                                            │
│  • pre-commit: YAML validate fileLists, paired-file rule       │
│  • pre-push:   optional full CI-equivalent script               │
└───────────────────────────────┬─────────────────────────────────┘
                                │ git push
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ Bitbucket Server                                                 │
│  • Branch permission: HotFix branch protected                    │
│  • Pull request: required reviewers + merge checks             │
└───────────────────────────────┬─────────────────────────────────┘
                                │ webhook / polling
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│ CI (e.g. Jenkins, Bamboo, GitLab CI mirror)                      │
│  Job: validate-hotfix-metadata                                   │
│   1. Parse & schema-validate fileLists                           │
│   2. Resolve latest release build for target baseline          │
│   3. Fail if release build missing / stale vs hotfix baseline    │
│   4. Verify paired commit for script + list                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. `fileLists.txt` — YAML structure and validation steps

### 5.1 Recommended file format

Treat content as **YAML** for machine validation (even if the filename remains `fileLists.txt` for legacy reasons).

**Example schema (illustrative — adjust keys to match existing `buildScripts.sh` expectations):**

```yaml
# fileLists.txt (YAML)
version: 1
baseline:
  release_commit: "<40-char-sha-or-short-sha>"   # optional if tracked elsewhere
  full_build_id: "<ci-build-number-or-url>"      # preferred linkage to CI
entries:
  - path: "WEB-INF/classes/com/example/Foo.class"
    reason: "BUG-12345"
  - path: "WEB-INF/lib/patched-library.jar"
    reason: "SECURITY-999"
```

### 5.2 Validation steps (automated)

1. **Syntax:** Run a YAML 1.1/1.2–compatible parser (`yamllint`, `ruamel.yaml`, PyYAML with safe_load).
2. **Schema:** Validate against JSON Schema or a small custom checker:
   - Required top-level keys: `version`, `entries` (minimum).
   - `entries` is a non-empty array (or allow empty only if policy forbids — recommend non-empty for HotFix).
   - Each entry has `path` (string, POSIX or normalized Windows path per repo convention).
   - Optional: forbid `..` path segments; enforce prefix rules (e.g. must start with `WEB-INF/`).
3. **Cross-check with script:** Optionally grep `buildScripts.sh` for referenced list filename and ensure paths exist in a **dry-run** extraction against a downloaded full-build artifact (advanced CI stage).

### 5.3 Deliverable: validator CLI

Add a small script (e.g. `git-tools/hotfix/validate_file_lists.py` — implementation phase) that:

- Accepts path to `fileLists.txt` and optional schema path.
- Exits non-zero with concise stderr on failure.
- Exits zero and prints parsed summary (entry count, paths) on success.

---

## 6. Mandatory “full build on release completed” validation

### 6.1 What “completed” means

- CI pipeline for `release` branch finished **successfully** for the commit that the HotFix is meant to patch against.
- Artifacts (full binary zip/ear) are **published** and **addressable** (URL, artifact repository ID, or checksum file).

### 6.2 Design options

| Approach | Pros | Cons |
|----------|------|------|
| **A. CI publishes `release-build-manifest.json`** alongside artifacts (commit SHA, build ID, timestamp, artifact checksum) | Single query from HotFix job | Requires CI template change on release pipeline |
| **B. HotFix branch stores only `baseline.full_build_id`** in YAML; CI resolves via REST API to Bamboo/Jenkins | Flexible | Depends on CI API availability and auth |
| **C. Git tag on release after successful build** (e.g. `build/release-20260509-001`) | Git-native | Tag discipline required |

**Recommended:** **A + optional tag:** Release pipeline writes manifest to artifact storage; HotFix validation downloads manifest by build ID or resolves “latest successful release build” and compares SHA to the HotFix YAML `baseline` section.

### 6.3 Validation steps (in CI)

1. Read `fileLists.txt` (YAML) `baseline.release_commit` and/or `baseline.full_build_id`.
2. Query CI or artifact API: “Does a successful full build exist for this baseline?”
3. If no match: **fail** with message:  
   `HotFix baseline has no successful release full build; merge to release and wait for CI, or correct baseline fields.`
4. Optional: **staleness check** — reject if release HEAD has moved and HotFix baseline is older than policy (e.g. must rebase HotFix branch on latest release tag).

---

## 7. Enforce `buildScripts.sh` and `fileLists.txt` committed together

### 7.1 Rule

**Default policy:** Any commit that modifies `buildScripts.sh` must also modify `fileLists.txt` in the same commit, and vice versa.

### 7.2 Implementation steps

1. **Pre-commit hook** (recommended tool: `pre-commit` framework or plain shell):
   - `git diff --cached --name-only`
   - If `buildScripts.sh` in list XOR `fileLists.txt` in list → exit 1 with instructions.
2. **CI job** (authoritative): Same logic on the pushed revision using `git show --name-only` or diff against merge base.
3. **Documented exceptions:**
   - Docs-only commits that touch neither file — allowed.
   - Emergency bypass: maintainers only via `[skip-hotfix-pair]` in commit message **only if** org policy allows (discouraged; prefer separate small commit that fixes both files).

### 7.3 Edge cases

- Rename `fileLists.txt` → update hook paths via repo config (`hotfix.paths.file_list`).
- Multiple list files: extend rule to “any file matching `fileLists*.txt`” or explicit list in config.

---

## 8. Bitbucket Server integration — ordered rollout

### Phase 1 — Repository-local (immediate value)

1. Add `pre-commit` config + validators under `git-tools/` (or submodule path).
2. Document in README: `pip install pre-commit && pre-commit install`.
3. Provide `validate-hotfix.sh` wrapping YAML + pair checks for developers without Python.

### Phase 2 — CI gate (blocking)

1. Add pipeline stage **on pushes to HotFix branch** and on **PRs targeting HotFix**.
2. Stage runs: YAML validate → baseline release-build check → paired-file check.
3. Publish HTML/text report as build artifact for auditors.

### Phase 3 — Server-side enforcement (optional)

1. **Branch permissions:** Restrict who can push to HotFix; require PR for all changes.
2. **Merge checks:** Require successful build of validator job before merge.
3. **Pre-receive hook** (if Bitbucket Server supports custom hooks in your version): Run lightweight pair check; heavy YAML + API checks remain in CI.

---

## 9. Operational checklist (for teams)

Before opening a PR to HotFix branch:

1. Confirm relevant changes are merged to `release` and **full build succeeded**.
2. Note **build ID / commit SHA** from release CI output.
3. Update `fileLists.txt` YAML with correct paths and baseline fields.
4. Update `buildScripts.sh` if packaging logic changes.
5. Commit **both** files in one commit when either changes.
6. Run local validator (`pre-commit run --all-files` or project script).
7. Open PR; wait for green CI.

---

## 10. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| False negatives if YAML schema too strict | Version `version:` field in YAML; evolve schema with migrations |
| CI API downtime blocks HotFix | Cache last-known-good manifest; read-only fallback with warning + manual approval role |
| Developers bypass hooks with `--no-verify` | CI remains mandatory on HotFix branch |
| Path mismatch Windows vs Linux in zip | Normalize paths in validator to repo convention |

---

## 11. Implementation backlog (concrete next steps)

1. **Freeze YAML schema** — agree on keys with owners of `buildScripts.sh`.
2. **Add `validate_file_lists` script** — YAML parse + schema + path rules.
3. **Add `check_paired_hotfix_files` script** — git staged-file logic.
4. **Wire pre-commit** — `.pre-commit-config.yaml` at repo root pointing to scripts.
5. **Extend release pipeline** — emit `release-build-manifest.json` (or equivalent).
6. **Add CI job** on HotFix branch — call validators + manifest API.
7. **Train team** — short session using §9 checklist.

---

## 12. References

- Source prompt: `docs/collected_prompt_usecases/Prompts.txt` (lines 49–66).
- This document location: `git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md`.

---

*Document version: 1.0 — design steps for systematic HotFix branch validation aligned with Bitbucket Server and release/full-build workflow.*
