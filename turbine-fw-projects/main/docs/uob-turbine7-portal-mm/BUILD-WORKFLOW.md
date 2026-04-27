# Optimized Build Workflow

## Problem Solved ✅

**Issue**: Base classes were deleted by `mvn clean`, requiring manual regeneration/copying.

**Solution**: Configured `maven-clean-plugin` to preserve base classes during clean operations.

## Standard Build Workflow (Recommended)

### Initial Setup (One-Time)

1. **Generate base classes**:
```bash
cd torque-orm
.\generate-base-classes.ps1
cd ..
```

2. **Commit base classes to version control** (Best Practice):
```bash
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

### Regular Builds

**Single Command** - No special steps needed:
```bash
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

The clean plugin will preserve base classes automatically!

## Build Workflow Comparison

### ❌ Before (Multiple Manual Steps):
```bash
# Step 1: Copy base classes manually
cd torque-orm
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" ...

# Step 2: Build (must avoid clean)
mvn install -DskipTests

# Step 3: Package
mvn package -DskipTests -Ptomcat
```

### ✅ After (Standard Maven Workflow):
```bash
# Single command - clean preserves base classes
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

## Configuration Details

The `torque-orm/pom.xml` includes:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-clean-plugin</artifactId>
    <configuration>
        <filesets>
            <fileset>
                <directory>${project.build.directory}</directory>
                <excludes>
                    <!-- Preserve base classes during clean -->
                    <exclude>generated-sources/com/uob/om/Base*.java</exclude>
                </excludes>
            </fileset>
        </filesets>
    </configuration>
</plugin>
```

## When to Regenerate Base Classes

Base classes only need regeneration when:
- Torque schema files change (`gtp-security-schema.xml`, etc.)
- New tables/columns are added
- Database structure changes

**Regeneration Process**:
```bash
cd torque-orm
.\generate-base-classes.ps1
cd ..
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Update generated base classes after schema change"
```

## CI/CD Integration

### GitHub Actions / GitLab CI Example:

```yaml
# Base classes are in version control, so standard build works
- name: Build
  run: mvn clean install -DskipTests

- name: Package WAR
  run: mvn package -DskipTests -Ptomcat
```

No special steps needed!

## Benefits

✅ **Standard Maven Workflow**: Use `mvn clean install` normally
✅ **No Manual Steps**: Base classes preserved automatically
✅ **Reproducible Builds**: Base classes in version control
✅ **CI/CD Friendly**: Works with any build system
✅ **Developer Friendly**: No special knowledge required

## Best Practices

1. **Commit Base Classes**: They're stable and enable reproducible builds
2. **Use Standard Maven Commands**: `mvn clean install` works as expected
3. **Regenerate Only When Needed**: Schema changes trigger regeneration
4. **Document Schema Changes**: Update base classes when schema changes

## Summary

**Before**: Multiple manual steps, special build commands
**After**: Standard Maven workflow, single command builds

**Build Command**:
```bash
mvn clean install -DskipTests
```

That's it! 🎉
