# Torque Base Classes Generation Guide

## Understanding the Issue

Torque ORM has a limitation: **When modifiable classes (Gtp*.java, Turbine*.java) already exist in `src/main/java`, Torque does not automatically generate base classes (Base*.java)**.

This is because Torque assumes:
- If modifiable classes exist, they were already generated
- Base classes should already exist or will be generated separately

## Solution: Generate Base Classes First

### Method 1: Use the Provided Script (Recommended)

#### Windows (PowerShell):
```powershell
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

#### Linux/Mac (Bash):
```bash
cd torque-orm
chmod +x generate-base-classes.sh
./generate-base-classes.sh
cd ..
```

### Method 2: Manual Process

1. **Backup modifiable classes**:
```bash
cd torque-orm
mkdir -p src/main/java-backup
cp src/main/java/com/uob/om/Gtp*.java src/main/java-backup/
cp src/main/java/com/uob/om/Turbine*.java src/main/java-backup/
```

2. **Remove modifiable classes temporarily**:
```bash
rm src/main/java/com/uob/om/Gtp*.java
rm src/main/java/com/uob/om/Turbine*.java
```

3. **Generate base classes**:
```bash
mvn clean generate-sources -DskipTests
```

4. **Restore modifiable classes**:
```bash
cp src/main/java-backup/*.java src/main/java/com/uob/om/
rm -rf src/main/java-backup
cd ..
```

### Method 3: Copy from Working Project (One-time Setup)

If you have the original `uob-turbine7-portal` project with generated base classes:

```powershell
# Generate base classes in original project first
cd ..\uob-turbine7-portal
mvn clean generate-sources -DskipTests

# Copy to multi-module project
cd ..\uob-turbine7-portal-mm\torque-orm
New-Item -ItemType Directory -Path "target\generated-sources\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "target\generated-sources\com\uob\om\" -Force
```

## Verification

After generating base classes, verify they exist:

```bash
ls torque-orm/target/generated-sources/com/uob/om/Base*.java
```

You should see files like:
- `BaseGtpUser.java`
- `BaseGtpRole.java`
- `BaseGtpGroup.java`
- `BaseGtpPermission.java`
- etc.

## Build After Generation

Once base classes are generated:

```bash
mvn clean install -DskipTests
```

## Why This Happens

Torque's generation logic:
1. Checks if modifiable classes exist in `src/main/java`
2. If they exist, assumes base classes are already generated
3. Only generates base classes if modifiable classes don't exist

This is a design decision to prevent overwriting custom code in modifiable classes.

## Permanent Solution

Once base classes are generated and committed to version control, they will be available for future builds. However, if you run `mvn clean`, they will be deleted and need to be regenerated.

**Recommendation**: Commit the generated base classes to version control so they're always available.

## Troubleshooting

### Base classes still not generated after running script

1. Check if modifiable classes were actually removed:
```bash
ls torque-orm/src/main/java/com/uob/om/Gtp*.java
```

2. Check Torque generation output for errors

3. Verify Torque schema files exist:
```bash
ls torque-orm/src/main/torque-schema/*.xml
```

### Base classes generated but compilation still fails

1. Verify build-helper plugin added the source directory:
```bash
mvn clean generate-sources process-sources -pl torque-orm -X | grep "add-source"
```

2. Check if base classes are in the correct package:
```bash
head -5 torque-orm/target/generated-sources/com/uob/om/BaseGtpUser.java
```

Should show: `package com.uob.om;`
