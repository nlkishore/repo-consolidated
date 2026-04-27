# Torque 3.x legacy demo (`gtp_user`)

This module reproduces **legacy Turbine / Torque-era patterns** used before Torque 7:

- **Torque 3.3** runtime (`org.apache.torque:torque-runtime:3.3`)
- **Village** `com.workingdogs.village.Record` traversal over `List` results from `BasePeer.executeQuery`
- **`MapBuilder`**-style metadata registration for the `gtp_user` table (`org.apache.torque.map.MapBuilder`)
- Turbine user bridge via `turbine_user_id` mapping and integration service (`com.uob.portal.torque3.turbine`)

Schema is aligned with the portal security model (`GTP_USER`) described under `C:\mysql81\uob-turbine-portal-mm-oracle\02-create-tables.sql`, expressed as **MySQL** DDL for local demos.

## Prerequisites

- JDK 8+ (module is compiled with `--release 8`)
- MySQL database `uob_portal` and table `gtp_user` (see `src/main/resources/sql/gtp_user-mysql.sql`)

## Configuration

Edit `src/main/resources/torque.properties` for your environment. Do not commit real passwords.

## Build

```text
mvn -q clean compile
```

## Tests (CRUD, MySQL via Testcontainers)

Integration tests live in `src/test/java` (`GtpUserLegacyCrudServiceIT`). They start a **MySQL 8** container and run the same CRUD flow as production code.

**Requirements:** Docker available locally. If Docker is not running, tests are **skipped**.

```text
mvn -q clean test
```

## Runtime usage

1. Call `Torque3RuntimeBootstrap.initFromClasspath()` (loads `/torque.properties` from the classpath).
2. Call `Torque.registerMapBuilder(new GtpUserMapBuilder())` (already done in the bootstrap for this demo).
3. Use `GtpUserLegacyCrudService` for CRUD; reads map `Record` rows to `GtpUserRow`.
4. Use `GtpUserTurbineIntegrationService` to map a Turbine-style user payload into `gtp_user`.

## Turbine 2.x mapping note

`schema.xml` only declares database structure, so Java interfaces (for example Turbine's `User`) are not
defined there. The mapping is done in Java adapter/service classes:

- `Turbine2UserAdapter` maps `org.apache.turbine.om.security.User` to `TurbineUserContract`
- `Turbine2UserAdapter` reflectively maps a Turbine 2.x user object to `TurbineUserContract`
- `GtpUserTurbineIntegrationService.createFromTurbine2User(...)` persists that mapping to `gtp_user`

Required schema bridge is the `turbine_user_id` column (already included), used to correlate Turbine and
portal-local identities.
