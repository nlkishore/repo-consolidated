# Build Solution: Standard Maven Workflow

## The Problem

`mvn clean` deletes the entire `target` directory, including generated base classes, requiring regeneration/copying every build.

## The Solution: Commit Base Classes to Version Control

**This is standard industry practice** for stable generated code.

### Why This Works

1. **Base classes are stable** - Only change when schema changes
2. **Reproducible builds** - Same base classes for all developers
3. **Standard workflow** - Use `mvn clean install` normally
4. **CI/CD friendly** - No special build steps needed

### Implementation

#### Step 1: Generate Base Classes Once

```bash
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

#### Step 2: Commit to Version Control

```bash
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

#### Step 3: Standard Build Workflow

```bash
# Works normally - base classes come from version control
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

## Alternative: Development Build (Skip Clean)

For faster development builds, skip clean:

```bash
# Development (faster, preserves base classes)
mvn install -DskipTests

# Full clean build (when needed)
mvn clean install -DskipTests
```

## Build Workflow Comparison

### ❌ Multiple Manual Steps (Before):
```bash
# Step 1: Copy base classes
cd torque-orm
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" ...

# Step 2: Build (avoid clean)
mvn install -DskipTests

# Step 3: Package
mvn package -DskipTests -Ptomcat
```

### ✅ Standard Maven Workflow (After):
```bash
# Single command - base classes from version control
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

## When to Regenerate

Only regenerate base classes when:
- Torque schema files change
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

## Industry Practices

**Many projects commit generated code**:
- Spring Boot (generates configuration metadata)
- Protocol Buffers (generates Java classes)
- gRPC (generates service stubs)
- Many ORM tools (generated entity classes)

**Rationale**:
- Generated code is stable
- Enables reproducible builds
- Simplifies CI/CD
- No special build knowledge required

## Summary

✅ **Recommended**: Commit base classes to version control
✅ **Result**: Standard Maven workflow
✅ **Command**: `mvn clean install -DskipTests`

**No special steps needed!** 🎉
