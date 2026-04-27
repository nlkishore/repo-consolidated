# Torque OpenRewrite migration lab

This module contains **minimal Torque 3.x-style sample code** so you can run the inventory recipe:

`com.uob.openrewrite.torque.Torque3ToTorque7Inventory`

defined in `../openrewrite-torque3-to-7/rewrite.yml`.

## Build sample (Torque 3 on classpath only)

```text
mvn -q clean compile
```

## Run OpenRewrite inventory (no source writes; use dryRun for safety)

```text
mvn -q clean compile org.openrewrite.maven:rewrite-maven-plugin:5.42.0:dryRun
```

The plugin is preconfigured in this `pom.xml` with `activeRecipes` and `configLocation`.

See `../TORQUE-OPENREWRITE-MIGRATION-GUIDE.md` for automation limits.
