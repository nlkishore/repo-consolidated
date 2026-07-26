# Banking Incremental Build & HotFix — Design & Implementation

**Purpose:** Technical design and implementation blueprint for (1) Release/HotFix/control-environment packaging and (2) Development-team pre-merge binary testing with an **agentic, four-worker orchestration**.

**Parent plan:** [../BANKING_HOTFIX_AGENTIC_DEPLOY_PLAN.md](../BANKING_HOTFIX_AGENTIC_DEPLOY_PLAN.md)  
**Traceability:** [Prompts.txt](../collected_prompt_usecases/Prompts.txt) (lines 181–200)  
**Status:** Design for review — implementation after plan approval  
**Related:** [../../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md](../../git-tools/HOTFIX_BRANCH_VALIDATION_DESIGN.md)

---

## 1. Context and actors

| Actor | Role |
|-------|------|
| Application developer | Commits on Dev / feature branch; may run local HotFix test before Release PR. |
| Reviewer | Approves PR into **Release**. |
| CI system | Full build on Release (and Dev); publishes binary + build manifest. |
| HotFix metadata owner | Maintains `filesList.txt` / `fileLists.txt` + `buildScripts.sh` on HotFix branch. |
| Bank deployment team | Runs Java EAR update utility; deploys to **control** environment. |
| Orchestrator (new) | Coordinates four parallel Dev-test activities. |

### Naming alignment

Existing HotFix validation docs use **`fileLists.txt`**. The new prompt uses **`filesList.txt`**. Implementation MUST support a configurable filename (default prefer existing `fileLists.txt` unless bank standard is `filesList.txt`).

---

## 2. Point 1 — Release / control deployment (canonical pipeline)

### 2.1 Sequence diagram (logical)

```text
Dev ──PR──► Release branch
               │
               ▼
         CI Full Build ──► Full Binary + release-build-manifest.json
               │
               ▼
         HotFix branch PR (filesList + buildScripts)
               │
               ▼
         Validate list / baseline / paired files
               │
               ▼
         HotFix Generator ──► HotFix.zip (incremental)
               │
               ▼
         Java EAR Updater (bank) ──► Patched EAR
               │
               ▼
         Deploy to Control Environment
```

### 2.2 Stage contracts

| Stage | Input | Output | Gate |
|-------|-------|--------|------|
| PR merge to Release | Approved PR | Commit on `release` | Required reviewers |
| Full build | Release commit | EAR/WAR/ZIP + **build manifest** (build id, commit SHA, artifact URI) | CI green |
| HotFix metadata | `fileLists` + `buildScripts.sh` | Validated metadata on HotFix branch | Schema + release build READY + paired commit |
| HotFix package | Manifest + list + full binary | `HotFix-<buildId>-<timestamp>.zip` | Generator exit 0; zip non-empty |
| EAR update | Baseline EAR + HotFix zip | Patched EAR | Java util exit 0; checksum logged |
| Control deploy | Patched EAR | Deployed control env | Bank process (out of scope for agent v1) |

### 2.3 Build manifest (required artifact)

Publish beside every successful Release (and Dev) full build:

```json
{
  "version": 1,
  "branch": "release",
  "commit": "abc123...",
  "build_id": "REL-1042",
  "status": "SUCCESS",
  "artifact_uri": "https://bitbucket.../artifacts/app-REL-1042.ear",
  "completed_at": "2026-07-26T07:00:00Z"
}
```

HotFix packaging **must** reference this manifest (reuse `validate-baseline` from existing hotfix_validation).

---

## 3. Point 2 — Development replica (pre-merge binary test)

### 3.1 Intent

Allow the application team to:

1. Push Dev branch → wait for **full build binary**.  
2. Generate HotFix zip from that binary using local/Dev HotFix generator + `filesList`.  
3. Download **previously deployed** EAR from Bitbucket.  
4. Apply HotFix zip via local EAR updater → testable EAR **before** Release merge.

### 3.2 Four parallel activities (prompt mapping)

| # | Activity | Worker name | Parallelism |
|---|----------|-------------|-------------|
| 1 | Track Dev build completed + full binary readiness | `BuildWatcher` | Starts immediately on trigger |
| 2 | Validate `filesList` available → trigger HotFix generator | `HotFixGeneratorAgent` | After (1) SUCCESS **and** filesList valid |
| 3 | Download previously deployed EAR from Bitbucket | `EarDownloader` | Parallel with (1)/(2) once EAR coordinates known |
| 4 | Run utility: HotFix zip + EAR → updated binary | `EarPatcher` | After (2) and (3) SUCCESS |

```text
Trigger (dev push / CLI run)
        │
        ├──────────────► BuildWatcher ──────────────┐
        │                     │ READY                │
        │                     ▼                      │
        │              HotFixGeneratorAgent ─────────┤──► both OK
        │                     │ HotFix.zip           │
        ├──────────────► EarDownloader ──────────────┤
        │                     │ baseline.ear         │
        │                     ▼                      ▼
        └────────────────────────────────────► EarPatcher → patched.ear
```

---

## 4. Agentic architecture

### 4.1 Why agents (not only a shell script)

- **Different readiness times** (CI vs download vs local Java tools).  
- **Retries** with backoff on network/CI flakes.  
- **Clear failure isolation** (e.g. download OK but HotFix gen failed).  
- Fits existing org pattern (LangGraph-style orchestration used elsewhere).

### 4.2 Orchestrator state (canonical)

```python
class DeployTestState(TypedDict, total=False):
    run_id: str
    branch: str
    commit: str
    environment: str                 # e.g. SIT-control, local-dev
    build_status: str                # PENDING|RUNNING|READY|FAILED
    build_manifest_path: str
    full_binary_uri: str
    files_list_path: str
    files_list_valid: bool
    hotfix_zip_path: str
    baseline_ear_path: str
    patched_ear_path: str
    errors: list[str]
    phase: str                       # INIT|WATCH|PACKAGE|DOWNLOAD|PATCH|DONE|FAILED
```

### 4.3 Worker responsibilities

#### 4.3.1 BuildWatcher

- Poll CI API / watch webhook for Dev branch build.  
- On SUCCESS, resolve `release-build-manifest.json` (or Dev equivalent) and artifact URI.  
- Emit event: `BUILD_READY`.  
- Timeout policy: configurable (e.g. 60–120 min); then `BUILD_FAILED`.

#### 4.3.2 HotFixGeneratorAgent

- Preconditions: `filesList` exists; pass `hotfix_validation` (YAML schema + optional baseline check against Dev manifest).  
- Invoke HotFix generator CLI/Java with: full binary path/URI, filesList, output dir.  
- Emit: `HOTFIX_ZIP_READY` or `HOTFIX_FAILED`.

#### 4.3.3 EarDownloader

- Input: Bitbucket project/repo/path or artifact coordinates for **previously deployed** EAR.  
- Auth: token from env/secret store (never commit).  
- Download to workspace `artifacts/baseline/`.  
- Emit: `EAR_DOWNLOADED`.  
- May run in parallel with BuildWatcher.

#### 4.3.4 EarPatcher

- Preconditions: HotFix zip + baseline EAR present.  
- Invoke Java EAR update utility.  
- Verify exit code + optional size/checksum.  
- Emit: `EAR_PATCHED` → phase `DONE`.

### 4.4 Orchestration engine options

| Option | Use when |
|--------|----------|
| **LangGraph** state machine | Preferred if aligning with CompletelySoldAlert-style agents |
| Lightweight asyncio supervisor | Prefer minimal deps; same state machine semantics |
| CI parallel stages only | Bank Release path (Point 1); fewer “agents”, same gates |

**v1 recommendation:** asyncio supervisor with explicit state file (`run-state.json`) + optional LangGraph in P3.

---

## 5. Implementation blueprint

### 5.1 Proposed project location

```text
C:\Python-Cursor\banking-hotfix-agent\
  README.md
  docs/  (link back to these design docs)
  config/
    settings.example.yaml
  src/banking_hotfix_agent/
    __main__.py
    config.py
    state.py
    orchestrator.py
    workers/
      build_watcher.py
      hotfix_generator.py
      ear_downloader.py
      ear_patcher.py
    adapters/
      ci_client.py          # Jenkins/Bamboo — pluggable
      bitbucket_artifacts.py
      java_tool_runner.py
  workspace/                # gitignored run artifacts
  tests/
```

Alternatively nest under `git-tools/main/modules/python/` next to `hotfix_validation` if the bank prefers one SCM tools monorepo.

### 5.2 Config sketch (`settings.example.yaml`)

```yaml
scm:
  bitbucket_base_url: "https://bitbucket.example.com"
  project: "BANKAPP"
  repo: "webapp"
ci:
  provider: "jenkins"          # jenkins|bamboo|generic_http
  status_url_template: "..."
  poll_seconds: 30
  build_timeout_minutes: 90
hotfix:
  files_list_name: "fileLists.txt"
  build_scripts_name: "buildScripts.sh"
  generator_cmd: "java -jar tools/HotFixGenerator.jar"
  ear_updater_cmd: "java -jar tools/HotFixEarUpdater.jar"
artifacts:
  baseline_ear_bitbucket_path: "deployed/control/app.ear"
  local_workspace: "workspace"
validation:
  run_hotfix_validation: true
  hotfix_validation_module: "hotfix_validation"
```

### 5.3 CLI (planned)

```bat
python -m banking_hotfix_agent run --branch develop --env local-dev
python -m banking_hotfix_agent status --run-id <id>
python -m banking_hotfix_agent retry --run-id <id> --from-phase PATCH
```

### 5.4 Adapter contracts

**CI client**

```text
get_build_status(branch, commit) -> {status, build_id, manifest_uri, artifact_uri}
wait_until_ready(branch, commit, timeout) -> manifest
```

**Bitbucket artifacts**

```text
download_file(project, repo, path_or_download_url, dest_path) -> Path
```

**Java tool runner**

```text
run(cmd_template, args_map, timeout) -> {exit_code, stdout_path, stderr_path}
```

### 5.5 Integration with existing HotFix validators

Before invoking HotFix generator:

```bat
python -m hotfix_validation run-all --file-lists <path> --manifest <dev-or-release-manifest.json> --repo <repo>
```

Reject orchestration if validation fails (do not generate zip).

---

## 6. Failure handling and audit

| Failure | Behavior |
|---------|----------|
| Build timeout / CI red | Stop packaging; keep download result optional for reuse |
| Invalid filesList | Do not call generator; surface schema errors |
| Download 401/404 | Retry with backoff; then FAIL |
| EAR updater non-zero | FAIL; retain zip + baseline for manual rerun |
| Partial success | Persist `run-state.json`; `retry --from-phase` |

Every run logs: `run_id`, branch, commit, build_id, filesList hash, baseline EAR checksum, HotFix zip checksum, patched EAR checksum, timestamps.

---

## 7. Security (banking)

- No passwords/tokens in git; env or vault only.  
- Least privilege Bitbucket download token.  
- Workspace artifacts local-only; clean policy after N days.  
- Do not auto-deploy patched EAR to production from the agent.

---

## 8. Implementation phases (code)

| Phase | Deliverable | Exit criteria |
|-------|-------------|---------------|
| **P0** | Plan + this design | Reviewed/approved |
| **P1** | Project scaffold + config + BuildWatcher + sequential HotFix gen | Dry-run against mock CI |
| **P2** | EarDownloader + EarPatcher + parallel orchestrator | End-to-end local patched EAR |
| **P3** | LangGraph optional front-end; Release CI hooks; notifications | Bank pilot on Dev/SIT |

---

## 9. Test plan (summary)

| Case | Expected |
|------|----------|
| Dev build not ready | Orchestrator stays in WATCH; no HotFix gen |
| filesList missing | HotFixGeneratorAgent fails fast |
| filesList invalid YAML | Validation fails; no zip |
| EAR download OK, build pending | Download completes; patch waits |
| Both zip + EAR ready | EarPatcher produces patched EAR |
| Updater fails | Phase FAILED; artifacts retained |
| Retry from PATCH | Skips watch/download/gen if state valid |

---

## 10. Traceability matrix (prompt → design)

| Prompt item | Section |
|-------------|---------|
| Release branch + PR for incremental package baseline | §2 |
| Full build after merge; then selected packaging | §2.2 |
| HotFix branch + filesList → zip | §2, §3 |
| Java util updates binary; control deploy | §2.2 (deploy out of agent scope) |
| Dev full binary + local HotFix generator | §3 |
| Download prior EAR from Bitbucket | §4.3.3 |
| HotFix zip + update EAR utility | §4.3.4 |
| Four parallel activities / agentic model | §3.2, §4 |

---

## 11. Review checklist

- [ ] Confirm CI provider and build-ready API.  
- [ ] Confirm Bitbucket artifact coordinates for “previously deployed” EAR.  
- [ ] Confirm Java jar names/CLIs and working directories.  
- [ ] Align `filesList.txt` vs `fileLists.txt` naming.  
- [ ] Approve project home: `C:\Python-Cursor\banking-hotfix-agent\` vs `git-tools`.  
- [ ] Sign-off P0 → start P1 implementation.
