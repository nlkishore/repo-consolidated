# Banking Incremental Build & HotFix Deployment — Plan

**Purpose:** Plan the banking web-application build/deploy approach (Release + HotFix packaging) and its **Development-team replica** for pre-merge binary testing, plus an **agentic orchestration** model for four parallel activities.

**Status:** Draft for review — design ready; implementation after approval  
**Traceability:** [Prompts.txt](collected_prompt_usecases/Prompts.txt) (lines 181–200)  
**Related existing work:**
- [../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md](../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md) — HotFix `fileLists` / paired-commit validation  
- [../git-tools/main/modules/python/HOTFIX_PYTHON_README.md](../git-tools/main/modules/python/HOTFIX_PYTHON_README.md) — validator CLI  

**Design (detailed):** [review/BANKING_HOTFIX_AGENTIC_DEPLOY_DESIGN.md](review/BANKING_HOTFIX_AGENTIC_DEPLOY_DESIGN.md)

---

## 1. Problem statement

### Point 1 — Control / bank deployment path (production-style)

| Step | Current intent |
|------|----------------|
| 1 | Developers merge to **Release** via **Pull Request** (reviewers approve). |
| 2 | Merge to Release triggers a **complete (full) build**. |
| 3 | HotFix branch holds `filesList.txt` (and `buildScripts.sh`) listing files to pick from the Release full binary. |
| 4 | HotFix packaging produces an **incremental zip**. |
| 5 | Bank deployment team runs a **Java utility** to apply the HotFix zip onto a baseline EAR/binary. |
| 6 | Updated binary is deployed to the **control environment**. |

### Point 2 — Development replica (pre-merge binary testing)

Same packaging idea, but for **local / Dev-team verification before merging to Release**:

| Step | Intent |
|------|--------|
| 1 | Team commits/pushes on a **Development** branch → **full build** → full binary available. |
| 2 | Local **HotFix generator** uses Dev full binary + `filesList.txt` to build HotFix zip. |
| 3 | Download **previously deployed EAR** binaries from **Bitbucket** (artifact store) to local. |
| 4 | Local **HotFix update EAR** utility merges HotFix zip into the downloaded EAR. |

### Gap (agentic need)

Four activities must run in a coordinated, failure-aware way (often overlapping):

1. Track Dev branch build completion and **full binary readiness**.  
2. Ensure `filesList.txt` is present/valid, then **trigger HotFix generator**.  
3. **Download** previously deployed EAR(s) from Bitbucket.  
4. **Run EAR update utility** (HotFix zip → updated EAR).  

Manual sequencing is error-prone (race on binary not ready, missing file list, wrong EAR version, silent tool failure).

---

## 2. Goals and non-goals

### Goals

| # | Goal |
|---|------|
| G1 | Document a clear **Release → full build → HotFix zip → EAR patch → control deploy** pipeline. |
| G2 | Document a **Dev-side replica** for binary testing before Release merge. |
| G3 | Propose an **agentic / multi-agent orchestration** that runs the four parallel activities with gates. |
| G4 | Reuse existing HotFix **fileLists validation** and paired `buildScripts.sh` rules where applicable. |
| G5 | Define interfaces to Bitbucket (PR, artifacts), CI (build status), and the two Java utilities (generate / update). |
| G6 | Produce audit-friendly logs (who, which commit, which build ID, which EAR baseline, which HotFix zip). |

### Non-goals (v1)

- Replacing bank deployment team’s production deployment console.  
- Full CD into customer-facing production without human approval.  
- Rewriting the Java HotFix generator / EAR updater (wrap and orchestrate only).  
- Guaranteeing Bitbucket Cloud vs Server API parity in one release (target **Bitbucket Server** first, per existing HotFix docs).

---

## 3. Recommended approach

### Option A — Orchestrator agent + four specialist workers (recommended)

```text
                    ┌──────────────────────────────┐
                    │  Orchestrator Agent            │
                    │  (state machine / LangGraph)   │
                    └───────────────┬────────────────┘
           ┌────────────┬───────────┼───────────┬────────────┐
           ▼            ▼           ▼           ▼            │
    BuildWatcher   FilesListGate  EarDownloader  EarPatcher  │
    (Dev CI ready) (validate+HF)  (Bitbucket)   (Java util)  │
           └────────────┴───────────┴───────────┴────────────┘
                              │
                              ▼
                    Local/Dev test EAR ready
```

| Pros | Cons |
|------|------|
| Matches the four parallel activities in the prompt | Needs clear contracts for CI + Bitbucket + Java CLIs |
| Parallel download while waiting for HotFix zip when safe | Requires secrets/config for Bitbucket artifact access |
| Natural fit for LangGraph / agentic tooling already used in Investment | Overkill if only one developer runs a single bat file |

### Option B — Linear shell/Jenkins pipeline only

| Pros | Cons |
|------|------|
| Familiar to bank CI teams | Poor parallelism; harder to retry one failed leg |
| Less new code | Does not satisfy “agentic model” ask |

**Decision for this plan:** Option A for Dev replica (Point 2). Point 1 remains bank CI + HotFix branch process, with the same conceptual stages and shared validation rules.

---

## 4. High-level flows

### 4.1 Point 1 — Release / control environment

```text
feature → PR → reviewers approve → merge to release
        → CI full build → publish full binary + build manifest
        → HotFix branch: filesList.txt (+ buildScripts.sh) via PR
        → HotFix CI: validate list → pick files from release binary → HotFix.zip
        → Bank Java utility: baseline EAR + HotFix.zip → patched EAR
        → Deploy patched EAR to control environment
```

### 4.2 Point 2 — Dev pre-merge binary test (agentic)

```text
dev push → CI full build → [1] BuildWatcher: wait until binary READY
filesList on HotFix/dev path → [2] FilesListGate: validate → HotFix generator → HotFix.zip
[3] EarDownloader: fetch prior EAR from Bitbucket (can start in parallel once env known)
[4] EarPatcher: when HotFix.zip + EAR ready → Java update utility → local test EAR
```

**Parallelism rules (summary):**

| Activity | May start when |
|----------|----------------|
| 1 BuildWatcher | Dev push / build triggered |
| 3 EarDownloader | Target environment / EAR coordinates known (does **not** need new build) |
| 2 FilesListGate + HotFix gen | Build **READY** **and** valid `filesList.txt` |
| 4 EarPatcher | Activities 2 and 3 both **SUCCESS** |

---

## 5. Deliverables (documentation + later code)

| Deliverable | Description |
|-------------|-------------|
| This plan | Scope, goals, options, flow |
| Design & implementation doc | Agents, state, APIs, CLI, folder layout, phased build |
| (Later) Tooling package | e.g. `C:\Python-Cursor\banking-hotfix-agent\` or under `git-tools` |
| Reuse | Existing `hotfix_validation` for filesList / paired scripts |

---

## 6. Phased rollout

| Phase | Scope |
|-------|--------|
| **P0** | Design + implementation doc (this package) — review |
| **P1** | Dev replica CLI: watch build status + validate filesList + invoke HotFix generator (sequential) |
| **P2** | Parallel EarDownloader + EarPatcher; structured state/checkpointing |
| **P3** | Wire to Release HotFix CI gates; optional LangGraph orchestrator; dashboards/notifications |

---

## 7. Open questions for review

1. Exact CI product (Jenkins / Bamboo / other) and API for “full build READY” + artifact URL.  
2. Exact Bitbucket path/API for “previously deployed EAR” per environment.  
3. Names/CLIs of the two Java utilities (HotFix generator vs EAR updater) and exit codes.  
4. Filename convention: `filesList.txt` vs `fileLists.txt` (existing docs use `fileLists.txt` — align naming).  
5. Whether Dev HotFix list lives on a dedicated HotFix branch or beside the Dev feature branch.

---

## 8. Next step

Review this plan, then use [review/BANKING_HOTFIX_AGENTIC_DEPLOY_DESIGN.md](review/BANKING_HOTFIX_AGENTIC_DEPLOY_DESIGN.md) as the implementation blueprint. Do **not** start agent code until P0 review sign-off.
