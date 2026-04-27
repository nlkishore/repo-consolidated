# IMPORTANT: Base Classes Generation

## Critical Build Requirement

**Base classes MUST be generated before building the project.**

The Torque ORM plugin does NOT automatically generate base classes when modifiable classes (Gtp*.java, Turbine*.java) already exist in `src/main/java`.

## Quick Solution

### Option 1: Copy from Original Project (Fastest)

If you have the original `uob-turbine7-portal` project:

```powershell
# Generate base classes in original project
cd ..\uob-turbine7-portal
mvn clean generate-sources -DskipTests

# Copy to multi-module project
cd ..\uob-turbine7-portal-mm\torque-orm
New-Item -ItemType Directory -Path "target\generated-sources\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "target\generated-sources\com\uob\om\" -Force

# Build
cd ..
mvn clean install -DskipTests
```

### Option 2: Use Generation Script

```powershell
cd torque-orm
.\generate-base-classes.ps1
cd ..
mvn clean install -DskipTests
```

### Option 3: Manual Process

1. Temporarily move modifiable classes
2. Run `mvn clean generate-sources -DskipTests`
3. Restore modifiable classes

## Why This Happens

Torque's design philosophy:
- If modifiable classes exist, assume base classes are already generated
- Prevents overwriting custom code in modifiable classes
- Base classes should be generated once and committed to version control

## Recommended Approach

**Commit base classes to version control** so they're always available:

```bash
# After generating base classes
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

This way, future builds won't require regeneration.

## Verification

After generating base classes, verify they exist:

```bash
ls torque-orm/target/generated-sources/com/uob/om/Base*.java
```

You should see ~52 base class files.

## Build Command

Once base classes are present:

```bash
mvn clean install -DskipTests
```

The build will compile both base classes and modifiable classes together.
