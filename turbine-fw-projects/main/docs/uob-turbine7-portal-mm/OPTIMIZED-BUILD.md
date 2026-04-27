# Optimized Build Strategy

## Problem

The standard `mvn clean install` deletes the `target` directory, including generated base classes, requiring them to be regenerated/copied every time.

## Solutions

### Solution 1: Preserve Base Classes in Clean (RECOMMENDED)

**Status**: ✅ Implemented in `torque-orm/pom.xml`

The `maven-clean-plugin` is configured to preserve base classes:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-clean-plugin</artifactId>
    <configuration>
        <filesets>
            <fileset>
                <directory>${project.build.directory}</directory>
                <excludes>
                    <exclude>generated-sources/com/uob/om/Base*.java</exclude>
                </excludes>
            </fileset>
        </filesets>
    </configuration>
</plugin>
```

**Usage**:
```bash
# One-time: Generate base classes
cd torque-orm
.\generate-base-classes.ps1
cd ..

# Subsequent builds: Use clean install normally
mvn clean install -DskipTests
```

**Benefits**:
- ✅ Base classes preserved across clean builds
- ✅ Standard Maven workflow
- ✅ No manual copying needed
- ✅ Works with all Maven commands

### Solution 2: Commit Base Classes to Version Control

**Best Practice**: Commit generated base classes to Git.

**Rationale**:
- Base classes are stable (only change when schema changes)
- Reduces build complexity
- Enables reproducible builds
- Standard practice for many projects

**Implementation**:
```bash
# After generating base classes once
git add torque-orm/target/generated-sources/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

**Usage**:
```bash
# Standard build - base classes come from version control
mvn clean install -DskipTests
```

**Benefits**:
- ✅ No generation step needed
- ✅ Reproducible builds
- ✅ Works on any machine
- ✅ Standard Maven workflow

### Solution 3: Separate Build Profiles

Create profiles for different build scenarios:

```xml
<profiles>
    <profile>
        <id>generate-base</id>
        <build>
            <plugins>
                <!-- Only generate, don't compile -->
            </plugins>
        </build>
    </profile>
    <profile>
        <id>build-only</id>
        <!-- Skip generation, assume base classes exist -->
    </profile>
</profiles>
```

**Usage**:
```bash
# First time or when schema changes
mvn clean generate-sources -Pgenerate-base

# Regular builds
mvn clean install -Pbuild-only -DskipTests
```

### Solution 4: Build Without Clean

For development, skip clean:

```bash
# Development builds (faster)
mvn install -DskipTests

# Full clean build (when needed)
mvn clean install -DskipTests
```

## Recommended Approach

**For Production/CI/CD**: Use **Solution 1** (Preserve Base Classes) + **Solution 2** (Commit to Git)

1. **Initial Setup**:
   ```bash
   # Generate base classes once
   cd torque-orm
   .\generate-base-classes.ps1
   cd ..
   
   # Commit to version control
   git add torque-orm/target/generated-sources/com/uob/om/Base*.java
   git commit -m "Add generated Torque base classes"
   ```

2. **Regular Builds**:
   ```bash
   # Standard Maven workflow
   mvn clean install -DskipTests
   mvn package -DskipTests -Ptomcat
   ```

3. **When Schema Changes**:
   ```bash
   # Regenerate base classes
   cd torque-orm
   .\generate-base-classes.ps1
   cd ..
   
   # Update in version control
   git add torque-orm/target/generated-sources/com/uob/om/Base*.java
   git commit -m "Update generated Torque base classes"
   ```

## Build Workflow Comparison

### Before (Multiple Steps):
```bash
# Step 1: Copy base classes
cd torque-orm
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" ...

# Step 2: Build (without clean)
mvn install -DskipTests

# Step 3: Package
mvn package -DskipTests -Ptomcat
```

### After (Single Step):
```bash
# Standard Maven workflow
mvn clean install -DskipTests
mvn package -DskipTests -Ptomcat
```

## Is This Normal Practice?

**Yes!** Multiple build steps are common when:
- Generated code needs special handling
- Code generation is expensive
- Generated code is stable and doesn't change often

**Industry Practices**:
1. **Commit generated code** (Spring Boot, many ORM tools)
2. **Preserve generated sources** (configure clean plugin)
3. **Separate generation phase** (protobuf, gRPC projects)
4. **Use build profiles** (different environments)

## Summary

✅ **Solution Implemented**: Clean plugin configured to preserve base classes
✅ **Recommended**: Commit base classes to version control
✅ **Result**: Standard Maven workflow, no manual steps needed

**Build Command**:
```bash
mvn clean install -DskipTests
```

No special steps required! 🎉
