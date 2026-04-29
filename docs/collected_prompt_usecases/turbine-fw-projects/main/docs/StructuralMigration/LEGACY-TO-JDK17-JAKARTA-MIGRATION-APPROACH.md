# Legacy to JDK17/Jakarta Migration Approach and Recommendations

## Context from Prompt
- Source stack: Torque 3.x, Turbine 2.x, Spring 4.x, customized Jetspeed components, Java EE/JDK 8.
- Target stack: Torque 7.x, Turbine 7.x, Spring 6.x, Jetspeed upgrade path aligned to JDK 17 and Jakarta EE.
- Initial idea: collect all Java imports, prepare a dummy target project, use AI/OpenRewrite to migrate imports first, then complete functional upgrades manually.

## Recommended Migration Approach

### 1) Baseline and Risk Discovery (Do first)
1. Freeze a baseline branch and produce a build/test report for the legacy code.
2. Inventory:
   - all Java imports,
   - all XML config namespaces/schemas,
   - all reflection usages (`Class.forName`, string class names),
   - all servlet/filter/listener references (`javax.*`).
3. Classify each dependency as:
   - direct replacement available,
   - replacement with behavior differences,
   - no replacement (requires adapter or redesign).

Why: import rewrite alone does not capture API behavior, lifecycle changes, or framework contracts.

### 2) Create a Target Compatibility Sandbox
1. Build a dedicated migration sandbox (already aligned with your StructuralMigration modules).
2. Include target BOM/versions:
   - JDK 17 toolchain,
   - Spring 6.x,
   - Turbine 7.x,
   - Torque 7.x,
   - Jakarta APIs (`jakarta.*`) where relevant.
3. Add compile-only stubs/adapters for missing legacy classes to unblock iterative migration.

Why: this gives a controlled environment where recipes can be tested repeatedly without destabilizing production code.

### 3) OpenRewrite Phase 1: Safe Mechanical Changes
Apply OpenRewrite for deterministic transformations:
- `javax.*` -> `jakarta.*` package migration (where official recipes exist),
- obvious import/package moves,
- deprecated API replacements with clear one-to-one mapping,
- formatting/modernization required by JDK 17 compilation.

Guardrails:
- run recipes in small batches,
- compile after each batch,
- auto-generate a rewrite report (what changed, what failed).

### 4) OpenRewrite Phase 2: Project-Specific Recipes
Create custom recipes for your codebase conventions:
- legacy utility class replacements,
- Turbine/Jetspeed extension point changes,
- Torque 3 patterns that need Torque 7 API adaptation.

Use import inventory as input, but include method signatures and usage context (not imports only).

### 5) Functional Adapter Layer (Bridge Strategy)
Introduce adapter/facade classes to isolate high-risk legacy contracts:
- MapBuilder metadata expectations,
- Village ResultSet traversal assumptions,
- legacy CRUD service interfaces.

Implement adapters against Torque 7 metadata/runtime so old service contracts can survive temporarily.

Why: reduces big-bang rewrite risk and allows progressive replacement.

### 6) Progressive Functional Migration
Migrate feature slices in order:
1. Read-only queries and list screens,
2. CRUD with transaction boundaries,
3. auth/session/integration flows,
4. admin/operational modules.

For each slice:
- port code,
- run integration tests,
- compare outputs against legacy baseline.

### 7) Verification and Release Hardening
- Add golden-data regression tests for key entities.
- Add SQL-level and API-level parity checks between legacy and migrated implementations.
- Run performance smoke tests (JDK17 + framework changes can alter defaults).
- Plan phased rollout with rollback strategy.

## Recommendations

1. Keep the "import capture + OpenRewrite" idea, but treat it as **accelerator**, not full migration strategy.
2. Prioritize a **compilable intermediate state** over perfect transformations in one pass.
3. Build/maintain **custom OpenRewrite recipes** specific to your Turbine/Jetspeed conventions.
4. Add a **compatibility adapter layer** for MapBuilder/Village-era behavior to minimize functional regressions.
5. Avoid direct production refactor first; migrate in the StructuralMigration sandbox, then port validated patterns.
6. Define done criteria per module: compiles on JDK17, tests pass, behavior parity validated, observability/logging verified.

## Suggested Deliverables
- Dependency and import inventory report (CSV/JSON + severity tags).
- Recipe catalog:
  - official recipes used,
  - custom recipes created,
  - unresolved/manual items.
- Compatibility adapter design note (interfaces, temporary classes, retirement plan).
- Migration wave plan (module order, risk, owners, ETA).
- Cutover checklist (data, rollback, post-deploy validation).

## Practical Next Step
Start with one vertical slice (for example `GtpUser` CRUD path) and execute full cycle:
inventory -> recipe pass -> compile fix -> adapter bridge -> integration test -> parity report.
Then scale the same playbook module-by-module.
