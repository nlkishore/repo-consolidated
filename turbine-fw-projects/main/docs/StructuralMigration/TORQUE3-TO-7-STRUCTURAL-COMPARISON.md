# Torque 3.x to 7.x structural comparison (functional migration guide)

This document compares the two demo modules created from `StructuralMigration/Prompts.txt`:

- `torque3-legacy-demo` — Torque **3.3** runtime, **Village** `Record` result traversal, **`MapBuilder`** metadata registration, **`BasePeer`** SQL execution.
- `torque7-modern-demo` — Torque **7.0** runtime, **Maven-generated** `GtpUser` / `GtpUserPeer`, **`Criteria`** queries, **`LegacyStyleTableMetadataFacade`** as a **MapBuilder** replacement.

Both target the same logical table: **`gtp_user`** in database **`uob_portal`**, aligned with the portal DDL described in `C:\mysql81\uob-turbine-portal-mm-oracle\02-create-tables.sql` (Oracle reference) and expressed as **MySQL** in the demo SQL files.

---

## 1. Dependency and platform stack

| Area | Torque 3 demo | Torque 7 demo |
| --- | --- | --- |
| JDK | 8 (`maven-compiler-plugin` `--release 8`) | 17 (`--release 17`) |
| Torque runtime | `org.apache.torque:torque-runtime:3.3` | `org.apache.torque:torque-runtime:7.0` |
| JDBC driver | `mysql:mysql-connector-java` | `com.mysql:mysql-connector-j` |
| Code generation | Not wired (hand-written services; `schema.xml` uses Torque 3 DTD as reference) | `torque-maven-plugin:7.0` + `torque-templates:7.0` (`mvn generate-sources`) |
| Result-set model | Village `com.workingdogs.village.Record` | Torque `GtpUser` data objects + `RecordMapper` (generated) |

---

## 2. Runtime configuration

| Concern | Torque 3 | Torque 7 |
| --- | --- | --- |
| Config API | `org.apache.commons.configuration.PropertiesConfiguration` passed to `Torque.init(Configuration)` | `Torque.init(String path)` (bootstrap copies classpath `/torque.properties` to a temp file) |
| DataSource factory | `org.apache.torque.dsfactory.SharedPoolDataSourceFactory` | `org.apache.torque.dsfactory.SharedPool2DataSourceFactory` |
| Property keys | `torque.database.default`, `torque.database.<name>.adapter`, `torque.dsfactory.<name>.*` | Same pattern (see each module’s `torque.properties`) |

**Developer task:** align database name (`uob_portal`), adapter (`mysql`), JDBC URL, user, and password with your environment. Do not commit secrets.

---

## 3. Metadata: MapBuilder (3.x) vs facade (7.x)

| Legacy (3.x) | Target (7.x) |
| --- | --- |
| Generated or hand-written `org.apache.torque.map.MapBuilder` builds `DatabaseMap` / `TableMap` / `ColumnMap` at startup | Torque 7 generated peers embed `TableMap` / `ColumnMap` in static initializers (`BaseGtpUserPeer`); **no MapBuilder** |
| Application introspection sometimes read Torque maps for dynamic UI or generic tooling | Use JDBC `DatabaseMetaData` (see `LegacyStyleTableMetadataFacade`) or read Torque `Column` / `ColumnMap` types from generated peers |

**Migration approach:** identify every place legacy code reads `DatabaseMap` / `TableMap` from MapBuilder. Replace with:

1. Generated `*Peer` column constants (`GtpUserPeer.LOGIN_NAME`, …), or  
2. `DatabaseMetaData` for fully dynamic behavior, or  
3. A small internal metadata registry populated once at startup from SQL or YAML.

---

## 4. CRUD structure mapping

| Operation | Torque 3 demo implementation | Torque 7 demo implementation |
| --- | --- | --- |
| **Create** | `PreparedStatement` insert on `Torque.getConnection("uob_portal")`, then re-read via `BasePeer.executeQuery` | `new GtpUser()` + setters + `save()` |
| **Read all** | `BasePeer.executeQuery(sql, "uob_portal")` → `List<Record>` → map to `GtpUserRow` | `GtpUserPeer.doSelect(new Criteria())` |
| **Read by id** | `BasePeer.executeQuery` with `WHERE user_id = <int>` (int is not concatenated from user input in API; keep this pattern) | `Criteria` + `GtpUserPeer.doSelect` |
| **Read by login** | `PreparedStatement` + `ResultSet` → `GtpUserRow` | `Criteria.where(LOGIN_NAME, …)` |
| **Update** | `PreparedStatement` `UPDATE …` | Mutate `GtpUser` + `save()` |
| **Delete** | `PreparedStatement` `DELETE …` | `GtpUserPeer.doDelete(row)` |

**Functional equivalence checklist for a medium-skilled developer:**

1. List public service methods in the legacy service (`GtpUserLegacyCrudService`).
2. For each method, locate the same business rule in `GtpUserTorque7CrudFacade`.
3. Confirm transaction boundaries: Torque 7 `save()` uses peer implementation defaults; legacy JDBC uses one connection per call — align with your app’s transaction strategy.
4. Replace `GtpUserRow` DTO usage with `GtpUser` where the UI layer can consume generated getters, or map `GtpUser` → DTO in an adapter if the API must stay stable.

---

## 5. Village `Record` removal

| Legacy | Replacement |
| --- | --- |
| Loop `List<Record>` and `record.getValue("col").asString()` | Iterate `List<GtpUser>` and use `getLoginName()` / typed getters |
| Handle `DataSetException` | Handle `TorqueException` and SQL exceptions from JDBC metadata facade |

---

## 6. Schema and SQL generation

| Item | Torque 3 module | Torque 7 module |
| --- | --- | --- |
| Authoritative schema file | `src/main/schema/schema.xml` (DTD) | `src/main/schema/uob-portal-mm-schema.xml` (XSD 5.0 namespace) |
| DDL artifact | `src/main/resources/sql/gtp_user-mysql.sql` | `mvn generate-sources` writes `target/generated-sql/uob-portal-mm-schema.sql` (also copied under `src/main/resources/sql/` for convenience) |

---

## 7. Common migration gaps (beyond this demo)

1. **Torque 3 `Criteria` vs Torque 7 `org.apache.torque.criteria.Criteria`** — package and semantics differ; do not assume copy-paste compatibility.
2. **BaseObject / save path** — legacy `BaseObject` hierarchy differs from Torque 7 data objects; review `save()` side effects and primary key assignment (`native` / `AUTO_INCREMENT`).
3. **Peer static maps** — Torque 7 builds `TableMap` in static blocks; startup order and multi-schema apps need review.
4. **Generated code ownership** — never edit `Base*` under `target/`; extend `GtpUser` / `GtpUserPeerImpl` in `src/main/generated-java` (or move custom subclasses to `src/main/java` if you change generator output dirs).
5. **Joins and fillers** — Torque 7 generates join helpers when the schema defines foreign keys; this demo uses a single table only.
6. **Jakarta / servlet stack** — Torque 3.3 runtime still pulls legacy `javax.servlet:servlet-api` transitively; keep it **off the server classpath** in Jakarta EE deployments (scope / exclusion strategy).

---

## 8. Suggested migration order

1. Freeze behavior: unit tests around legacy CRUD for `gtp_user`.
2. Bring up Torque 7 module with generated peers and JDBC settings.
3. Port CRUD method by method, comparing SQL (enable Torque / JDBC logging).
4. Replace MapBuilder metadata consumers with `LegacyStyleTableMetadataFacade` or peer constants.
5. Expand schema to remaining GTP_* tables and repeat.

This sequence keeps each PR small and reviewable for a medium-skilled Java developer.

---

## OpenRewrite inventory

For search-only recipes and a realistic assessment of automation limits, see `TORQUE-OPENREWRITE-MIGRATION-GUIDE.md` and `openrewrite-torque3-to-7/rewrite.yml`.
