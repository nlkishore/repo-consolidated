# Torque 7.x modern demo (`gtp_user`)

This module demonstrates **Apache Torque 7.0** on **JDK 17** with the official **torque-maven-plugin** code generation workflow.

It targets the same logical table as the Torque 3.x demo (`gtp_user`), derived from the portal schema described in:

`C:\mysql81\uob-turbine-portal-mm-oracle\02-create-tables.sql`

## Prerequisites

- JDK 17+
- MySQL database `uob_portal` with table `gtp_user` (see generated SQL after `generate-sources`, or `src/main/resources/sql/uob-portal-mm-schema.sql`)

## Build

```text
mvn -q clean generate-sources compile
```

## Tests (CRUD, MySQL via Testcontainers)

Integration tests live in `src/test/java` (`GtpUserTorque7CrudFacadeIT`). They start a **MySQL 8** container, create `gtp_user`, and exercise CRUD through Torque 7 peers.

**Requirements:** Docker available locally. If Docker is not running, tests are **skipped**.

```text
mvn -q clean test
```

Generated **Base\*** peers live under `target/generated-sources` and must not be edited. Skeleton classes under `src/main/generated-java` are safe to extend.

## Runtime configuration

Edit `src/main/resources/torque.properties` for your JDBC settings.

## MapBuilder replacement

Torque 7 no longer uses Torque 3 `MapBuilder` classes. This demo provides `LegacyStyleTableMetadataFacade`, which rebuilds schema/table/column metadata using JDBC `DatabaseMetaData` so application code that depended on MapBuilder-driven introspection can be migrated incrementally.
