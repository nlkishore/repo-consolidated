# Torque 3.x → 7.x: OpenRewrite role and manual work

This guide complements `TORQUE3-TO-7-STRUCTURAL-COMPARISON.md` and the validated CRUD demos (`torque3-legacy-demo`, `torque7-modern-demo`).

## What was validated

Both demos include **Testcontainers MySQL** integration tests that exercise CRUD against a real database:

- `torque3-legacy-demo`: `GtpUserLegacyCrudServiceIT`
- `torque7-modern-demo`: `GtpUserTorque7CrudFacadeIT`

**Requirement:** Docker available on the machine running `mvn test`. If Docker is absent, the tests are **skipped** via JUnit assumptions.

## OpenRewrite recipe location

| Location | Purpose |
| --- | --- |
| `StructuralMigration/openrewrite-torque3-to-7/rewrite.yml` | Source-of-truth in the workspace tree |
| `C:\openRewrite\torque3-to-7-migration\rewrite.yml` | Copy for teams that standardise on `C:\openRewrite` |

Active recipe name:

- `com.uob.openrewrite.torque.Torque3ToTorque7Inventory`

## What OpenRewrite **can** help with

1. **Inventory / backlog generation (primary value)**  
   The inventory recipe flags:
   - Village (`com.workingdogs.village.*`)
   - `MapBuilder`, `BasePeer`, `org.apache.torque.util.Criteria`
   - Torque 3 runtime property keys (`torque.dsfactory` in `*.properties`)
   - Commons Configuration **1.x** (`org.apache.commons.configuration.PropertiesConfiguration`) used with Torque 3 init
   - Maven coordinates `torque-runtime` / `torque-generator` for version uplift tracking  

2. **Orthogonal modernisation** (separate recipe packs, not in this YAML)  
   - Java language upgrades (8 → 17) via `rewrite-migrate-java`
   - Jakarta namespace migrations where applicable (`javax.*` → `jakarta.*`) — **orthogonal** to Torque ORM regeneration

3. **Repeatable reporting**  
   `rewrite:dryRun` with datatables exports supports PR-sized migration batches.

## What OpenRewrite **cannot** safely automate

These typically require **regeneration from `schema.xml`**, **manual rewiring**, and **tests**:

1. **Peer / Base / OM class regeneration**  
   Torque 7 emits a different class set (e.g. `*PeerImpl`, `*RecordMapper`, `Base*` under `target/generated-sources`). OpenRewrite does not replace the Torque Maven generator.

2. **`org.apache.torque.util.Criteria` → `org.apache.torque.criteria.Criteria`**  
   Package and **API semantics** differ. Blind `ChangePackage` breaks compilation and can change SQL.

3. **Village `Record` removal**  
   Row mapping moves to generated data objects and mappers. This is a **design migration**, not a rename.

4. **`MapBuilder` removal**  
   Torque 7 embeds table metadata in generated peers. Legacy dynamic metadata consumers need **JDBC `DatabaseMetaData`** or explicit metadata (see `LegacyStyleTableMetadataFacade` in `torque7-modern-demo`).

5. **Runtime configuration**  
   Torque 3 uses `commons-configuration` 1.x; Torque 7 uses `commons-configuration2`. Property keys and datasource factories differ (`SharedPoolDataSourceFactory` vs `SharedPool2DataSourceFactory` in the demos).

6. **Transaction and session semantics**  
   Behavioural equivalence requires integration tests, not AST refactors.

## Optional migration lab module

`StructuralMigration/torque-openrewrite-migration-lab/` is a **tiny** Maven project that:

- Compiles **Torque 3.3**-style sample code (`LegacyGtpUserAccess`)
- Configures `rewrite-maven-plugin` to run `Torque3ToTorque7Inventory` against that sample tree

It is **not** a runnable Torque 7 application; it exists to prove the OpenRewrite wiring and produce inventory output on representative imports.

## Suggested developer workflow

1. Run inventory recipe (`Torque3ToTorque7Inventory`) with `rewrite:dryRun` on each legacy module.  
2. Upgrade build tooling: JDK 17, `torque-maven-plugin` 7.x, `torque-runtime` 7.x, regenerate OM.  
3. Re-implement services using Torque 7 peers and `org.apache.torque.criteria.Criteria` with tests (reuse the Testcontainers pattern from `torque7-modern-demo`).  
4. Replace MapBuilder/Village-specific code using the structural guide and the metadata facade pattern.  
5. Apply separate OpenRewrite packs for Java/Jakarta upgrades where they do not collide with Torque codegen output.
