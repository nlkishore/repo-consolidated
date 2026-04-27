# Automated Build Solution: Base Classes Auto-Copy

## Problem Solved ✅

**Issue**: Base classes deleted by `mvn clean`, requiring manual copying.

**Solution**: Maven resources plugin automatically copies base classes from committed location during build.

## How It Works

### Base Classes Location

Base classes are committed to version control in:
```
torque-orm/src/main/generated-base-classes/com/uob/om/Base*.java
```

### Build Process

During `mvn clean install`:

1. **Clean Phase**: Deletes `target/` directory
2. **Generate Sources Phase**: 
   - Maven resources plugin copies base classes from `src/main/generated-base-classes/` to `target/generated-sources/`
   - Torque plugin runs (doesn't generate base classes)
3. **Compile Phase**: 
   - Compiles base classes (from `target/generated-sources`)
   - Compiles modifiable classes (from `src/main/java`)
4. **Package Phase**: Creates JAR

## One-Time Setup

### Step 1: Copy Base Classes to Committed Location

```powershell
# Copy from original project
cd torque-orm
New-Item -ItemType Directory -Path "src\main\generated-base-classes\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "src\main\generated-base-classes\com\uob\om\" -Force
cd ..
```

### Step 2: Commit to Version Control

```bash
git add torque-orm/src/main/generated-base-classes/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

## Regular Builds

**After setup, use standard Maven commands:**

```bash
# Standard build - base classes auto-copied
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

**No manual steps needed!** ✅

## Configuration

The `torque-orm/pom.xml` includes:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-base-classes</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/generated-sources/com/uob/om</outputDirectory>
                <resources>
                    <resource>
                        <directory>src/main/generated-base-classes/com/uob/om</directory>
                        <includes>
                            <include>Base*.java</include>
                        </includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

This automatically copies base classes during the `generate-sources` phase.

## Build Flow

```
mvn clean install
│
├─ Clean: Delete target/
│
├─ Generate Sources:
│  ├─ Copy base classes: src/main/generated-base-classes → target/generated-sources ✅
│  └─ Torque generation: Runs (doesn't generate base classes)
│
├─ Compile:
│  ├─ Base classes (from target/generated-sources)
│  └─ Modifiable classes (from src/main/java)
│
└─ Package: Create JAR
```

## Benefits

✅ **Automatic**: Base classes copied during build
✅ **Standard Workflow**: Use `mvn clean install` normally
✅ **Version Controlled**: Base classes in Git
✅ **No Manual Steps**: Everything automated
✅ **CI/CD Friendly**: Works with any build system

## When to Regenerate

Only when Torque schema changes:

```bash
cd torque-orm
.\generate-base-classes.ps1
# Copy to committed location
Copy-Item -Path "target\generated-sources\com\uob\om\Base*.java" -Destination "src\main\generated-base-classes\com\uob\om\" -Force
cd ..
git add torque-orm/src/main/generated-base-classes/com/uob/om/Base*.java
git commit -m "Update base classes after schema change"
```

## Summary

**Before**: Manual copy required every build
**After**: Automatic copy during build

**Build Command**:
```bash
mvn clean install -DskipTests
```

**That's it!** 🎉
