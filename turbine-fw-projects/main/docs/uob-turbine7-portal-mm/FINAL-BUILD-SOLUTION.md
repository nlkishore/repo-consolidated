# Final Build Solution: One-Time Setup, Then Standard Workflow

## The Answer to Your Questions

### Q: Is there a way to isolate Torque-ORM module so it's not recompiled and base classes aren't deleted?

**A: Yes - Commit base classes to version control (Standard Practice)**

This is the **industry-standard solution** used by:
- Spring Boot (commits generated configuration metadata)
- Protocol Buffers projects (commits generated Java classes)
- gRPC projects (commits generated service stubs)
- Many ORM tools (commits generated entity classes)

### Q: Is it always practice to follow multiple build steps?

**A: No - After one-time setup, use standard Maven workflow**

**Before Setup**: Multiple manual steps required
**After Setup**: Single command `mvn clean install -DskipTests`

## One-Time Setup (Do This Once)

### Step 1: Generate/Copy Base Classes

```powershell
# Option A: Copy from original project (if available)
cd torque-orm
New-Item -ItemType Directory -Path "target\generated-sources\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "target\generated-sources\com\uob\om\" -Force
cd ..

# Option B: Generate using script
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

### Step 2: Commit to Version Control

```bash
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

**That's it!** Setup complete.

## Standard Build Workflow (After Setup)

### Regular Builds

```bash
# Standard Maven command - no special steps!
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

### Development Builds (Faster)

```bash
# Skip clean for faster builds
mvn install -DskipTests
```

## Why This Works

1. **Base classes are stable** - Only change when schema changes
2. **Version control provides them** - Available to all developers
3. **Standard Maven workflow** - No special knowledge needed
4. **CI/CD friendly** - Works with any build system

## When to Regenerate

Only regenerate when:
- Torque schema files change (`gtp-security-schema.xml`, etc.)
- New tables/columns added
- Database structure changes

**Process**:
```bash
cd torque-orm
.\generate-base-classes.ps1
cd ..
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Update base classes after schema change"
```

## Build Workflow Comparison

### ❌ Before Setup (Multiple Steps):
```bash
# Step 1: Copy base classes manually
cd torque-orm
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" ...

# Step 2: Build (avoid clean)
mvn install -DskipTests

# Step 3: Package
mvn package -DskipTests -Ptomcat
```

### ✅ After Setup (Single Command):
```bash
# Standard Maven workflow
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

## Module Isolation

The Torque-ORM module is already isolated:
- ✅ Separate Maven module
- ✅ Independent build lifecycle
- ✅ Can be built separately: `mvn clean install -pl torque-orm -am`

**But**: Base classes are still needed for compilation, so they must be:
1. Generated (one-time or when schema changes)
2. Committed to version control (recommended)
3. Available during build (from version control)

## Summary

**Question**: Multiple build steps always required?
**Answer**: **No** - Only one-time setup, then standard workflow

**Setup** (One-time):
1. Generate/copy base classes
2. Commit to version control

**Regular Builds**:
```bash
mvn clean install -DskipTests
```

**No special steps needed!** 🎉

## Quick Reference

```bash
# One-time setup
cd torque-orm
New-Item -ItemType Directory -Path "target\generated-sources\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "target\generated-sources\com\uob\om\" -Force
cd ..
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"

# Regular builds (after setup)
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```
