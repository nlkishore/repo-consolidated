# HotFix Team Repo — Test Cases & Sandbox Strategy

**Purpose:** Describe how teams can use **isolated repositories or sandbox branches** for HotFix branch workflow and tooling validation **without disrupting** day-to-day work on `release`, feature branches, or shared integration lines.

**Audience:** Release engineers, branch admins, developers validating `buildScripts.sh`, `fileLists.txt`, and `hotfix_validation` tooling.

**Related:** `HOTFIX_BRANCH_VALIDATION_DESIGN.md`, `main/modules/python/hotfix_validation/`, `HOTFIX_PYTHON_README.md`.

---

## 1. Goals

| Goal | Outcome |
|------|---------|
| Isolate HotFix experiments | Mistakes in YAML or paired commits do not block the main repo’s daily merges. |
| Train safely | Teams rehearse PR → validation → incremental zip without touching production binaries workflow. |
| Validate automation | Run `validate-file-lists`, `check-paired`, `validate-baseline` against fixtures before enabling CI gates on the canonical repo. |

---

## 2. Recommended patterns (choose one per organization policy)

### Pattern A — Dedicated sandbox repository (lowest risk to daily work)

Create a **mirror or fork** used only for HotFix rehearsals.

**Naming examples:** `<product>-hotfix-sandbox`, `repo-consolidated-hotfix-lab`.

**Characteristics:**

- Same default branch layout intent as production repo (`release`, hotfix branch name agreed with release team).
- No production CI publishing real artifacts; optional mock manifest JSON only.
- Main team continues working in the canonical repo unchanged.

**When to use:** Strong separation needed; Bitbucket/GitHub permissions easier on a second repo.

---

### Pattern B — Sandbox branches inside the same repo (medium isolation)

Create **long-lived sandbox branches** never merged to `release` unless explicitly promoted.

**Examples:**

- `hotfix-lab/team-alpha` — team-specific rehearsal
- `hotfix-lab/validation-only` — automation testing only

**Rules:**

- Branch protection: `hotfix-lab/*` may require **only** HotFix validators in CI, not full product build.
- `release` and normal `hotfix/*` stay reserved for real delivery; daily work uses `feature/*` or `dev/*` from `release` as today.

**When to use:** One Bitbucket project, want one clone; need clear naming and permissions.

---

### Pattern C — Personal / team forks (good for pull-request practice)

Each developer or squad uses a **fork**; only “promotion” PRs go to the org canonical repo.

**When to use:** Open-source–style flow; strong code review before any change hits the main remote.

---

## 3. How to create a team sandbox repo (Pattern A) — procedure

Use this as a **test case pre-condition** for lab environments.

1. **Create empty project** in Bitbucket Server (or GitHub) with name e.g. `myapp-hotfix-sandbox`.
2. **Add remote** (do not replace `origin` of the main repo on developer machines for daily work):
   - `git remote add sandbox <sandbox-url>`
3. **Initial push of structure only** (optional):
   - Copy `buildScripts.sh`, `fileLists.txt` (template), and `hotfix_validation` examples from `repo-consolidated` or your product repo.
4. **Create branches:**
   - `release` — track or reset to a **known good tag/SHA** from production pipeline (read-only reference).
   - `hotfix-lab` (or `hotfix/build`) — where teams commit HotFix metadata for practice.
5. **Wire CI lightly:**
   - Job runs only: `pip install -r requirements-hotfix-validation.txt`, `python -m hotfix_validation run-all ...`
   - No full EAR/WAR build unless explicitly requested.

**Impact on daily work:** None on the main repository if developers keep using canonical `origin` for normal features.

---

## 4. Test case template (fill per sprint)

| Field | Value |
|-------|--------|
| **TC-ID** | HOTFIX-LAB-NNN |
| **Sandbox pattern** | A / B / C |
| **Repo / branch under test** | |
| **Baseline manifest** | Path or URL to sample `release-build-manifest.json` |
| **Tester / date** | |

---

## 5. Test cases

### TC-01 — Sandbox repo exists and is independent of daily `release` work

| Item | Detail |
|------|--------|
| **Objective** | Confirm a team can push to the sandbox without affecting the canonical `release` branch. |
| **Preconditions** | Pattern A: `myapp-hotfix-sandbox` created; developer has write access; canonical repo URL unchanged as primary `origin`. |
| **Steps** | 1. Clone sandbox: `git clone <sandbox-url>`. 2. Create branch `hotfix-lab/try-01` from `release` (or empty). 3. Add dummy `fileLists.txt` (valid YAML) and touch `buildScripts.sh` in same commit. 4. Push to sandbox. 5. On canonical repo, verify `release` and open PRs are unchanged. |
| **Expected** | Push succeeds; canonical repo history and branches unaffected. |
| **Pass criteria** | No CI triggered on canonical repo; no merge to canonical without separate PR. |

---

### TC-02 — `fileLists.txt` YAML validation in sandbox (no block on mainline)

| Item | Detail |
|------|--------|
| **Objective** | Run `validate-file-lists` on invalid then valid YAML in sandbox; main team not blocked. |
| **Preconditions** | `hotfix_validation` installed per `HOTFIX_PYTHON_README.md`. |
| **Steps** | 1. Commit intentionally invalid YAML (missing `entries`) on `hotfix-lab` — push. 2. Run `python -m hotfix_validation validate-file-lists fileLists.txt`; expect exit ≠ 0. 3. Fix file to match schema; re-run; expect exit 0. |
| **Expected** | Failures contained to sandbox branch; developers on canonical repo unaffected. |
| **Pass criteria** | Validator errors are clear; fix restores exit 0. |

---

### TC-03 — Paired commit rule (`buildScripts.sh` + `fileLists.txt`)

| Item | Detail |
|------|--------|
| **Objective** | Enforce paired commits locally before enabling server-side hooks on canonical HotFix branch. |
| **Preconditions** | Sandbox clone with both files present. |
| **Steps** | 1. Stage only `fileLists.txt`; run `python -m hotfix_validation check-paired --staged --repo .`; expect failure. 2. Amend commit to include `buildScripts.sh`; re-run; expect success. 3. (Optional) Commit with `[skip-hotfix-pair]` in message per policy; verify bypass. |
| **Expected** | Pair check matches design doc §7. |
| **Pass criteria** | Exit codes and messages match team policy. |

---

### TC-04 — Baseline vs release manifest (lab manifest)

| Item | Detail |
|------|--------|
| **Objective** | Verify HotFix baseline fields align with a **mock** `release-build-manifest.json` before using real CI artifacts. |
| **Preconditions** | Sample files from `hotfix_validation/examples/` or lab-generated manifest with `status: success`, matching `commit_sha` / `full_build_id`. |
| **Steps** | 1. `python -m hotfix_validation validate-baseline fileLists.txt sample_release_manifest.json` 2. Change `full_build_id` in YAML to mismatch manifest; re-run; expect failure. |
| **Expected** | Pass on match; fail on mismatch with explicit stderr. |
| **Pass criteria** | Team understands required fields before turning on mandatory CI in production repo. |

---

### TC-05 — `run-all` gate rehearsal (CI dry run)

| Item | Detail |
|------|--------|
| **Objective** | Mirror future CI: one command for file list + paired + optional baseline. |
| **Preconditions** | Valid files; for paired use `--staged` or `--rev` with a single commit containing both metadata files. |
| **Steps** | `python -m hotfix_validation run-all --file-lists fileLists.txt --staged --repo . --manifest path/to/manifest.json` |
| **Expected** | Exit 0 when all sub-validators pass. |
| **Pass criteria** | Same command can be pasted into Jenkins/Bamboo/GitHub Actions for sandbox branch only first. |

---

### TC-06 — Promotion from sandbox to canonical (minimal daily disruption)

| Item | Detail |
|------|--------|
| **Objective** | Changes validated in sandbox are replayed on canonical repo with minimal churn. |
| **Preconditions** | Sandbox PR or patch reviewed; canonical HotFix branch exists; release build for baseline completed in real CI. |
| **Steps** | 1. Export patch or open PR from sandbox fork to canonical `hotfix/*`. 2. Update `baseline` in `fileLists.txt` with **production** `release_commit` / `full_build_id`. 3. Run validators against **production** manifest path/URL. 4. Merge during agreed release window. |
| **Expected** | No forced rebases on unrelated team branches; HotFix metadata lands as small, reviewable commits. |
| **Pass criteria** | Canonical `release` daily merge cadence unchanged except coordinated HotFix window. |

---

## 6. Minimizing impact on “current running branches” — checklist

1. **Never point sandbox CI at production artifact deployment paths** — use dummy buckets or `examples/` only.
2. **Prefix lab branches** (`hotfix-lab/*`) so release managers can filter notifications.
3. **Run full product build only on canonical `release`**; sandbox runs HotFix validators only unless testing integration explicitly.
4. **Document “promotion”** so teams know when lab settings must be replaced with real baseline IDs.
5. **Schedule** heavy HotFix CI experiments outside peak merge hours if sharing one BitbucketCI/Jenkins instance.

---

## 7. Traceability to design document

| Design § | Test cases |
|----------|------------|
| §5 YAML `fileLists` | TC-02 |
| §6 Release baseline | TC-04 |
| §7 Paired commits | TC-03 |
| §8 CI gate | TC-05 |
| Isolation / rollout | TC-01, TC-06 |

---

## 8. Document control

| Version | Date | Notes |
|---------|------|--------|
| 1.0 | 2026-05-10 | Initial test cases and sandbox repo strategy |

---

*This document is a test and procedure guide; adjust branch names and tooling paths to match your Bitbucket Server project and product repository layout.*
