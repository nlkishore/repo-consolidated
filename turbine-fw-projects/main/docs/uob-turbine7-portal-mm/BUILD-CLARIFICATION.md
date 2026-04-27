# Build Clarification: Torque-ORM Module

## Important Answer

**NO** - Torque-ORM module is **NOT excluded** from builds. It builds **every time** like all other modules.

## What Happens During `mvn clean install`

### All Modules Build (Including Torque-ORM)

```
mvn clean install
│
├─ ✅ Parent Module (pom)
│
├─ ✅ torque-orm Module (BUILDS EVERY TIME)
│  ├─ Clean: Deletes target/ directory
│  ├─ Generate Sources: Torque runs (doesn't generate base classes)
│  ├─ Process Sources: Adds generated-sources to classpath
│  ├─ Compile: 
│  │  ├─ Base classes (from Git → restored to target/generated-sources)
│  │  └─ Modifiable classes (from src/main/java)
│  └─ Package: Creates torque-orm-1.0-SNAPSHOT.jar
│
├─ ✅ turbine-model-controller Module
│  └─ Depends on: torque-orm JAR
│
├─ ✅ spring-rest-api Module
│  └─ Depends on: torque-orm JAR, turbine-model-controller JAR
│
└─ ✅ webapp Module
   └─ Depends on: All modules
```

## What We're Solving

### The Problem
- Base classes deleted by `mvn clean`
- Need to regenerate/copy base classes every build
- Requires multiple manual steps

### The Solution
- ✅ **Commit base classes to version control** (one-time setup)
- ✅ **Torque-ORM builds normally** (compiles all classes)
- ✅ **Base classes restored from Git** (after clean)

## Why Torque-ORM Must Build Every Time

Torque-ORM module contains:
1. **Modifiable classes** (GtpUser.java, GtpRole.java, etc.)
   - May change during development
   - Must be compiled every build

2. **Custom code and overrides**
   - Business logic in OM classes
   - Must be compiled

3. **Dependencies**
   - Other modules depend on torque-orm JAR
   - JAR must be rebuilt if classes change

## Build Configuration

### Parent POM (`pom.xml`)
```xml
<modules>
    <module>torque-orm</module>          <!-- ✅ Built every time -->
    <module>turbine-model-controller</module>
    <module>spring-rest-api</module>
    <module>webapp</module>
</modules>
```

**No exclusions** - All modules build in order.

### Torque-ORM Build Process

1. **Clean Phase**: 
   - Deletes `target/` directory
   - Base classes from Git are restored (if committed)

2. **Generate Sources Phase**:
   - Torque plugin runs
   - Doesn't generate base classes (modifiable classes exist)
   - Creates directory structure

3. **Process Sources Phase**:
   - Build helper adds `target/generated-sources` to classpath
   - Base classes available from Git

4. **Compile Phase**:
   - Compiles base classes (from `target/generated-sources`)
   - Compiles modifiable classes (from `src/main/java`)
   - Creates `target/classes`

5. **Package Phase**:
   - Creates `torque-orm-1.0-SNAPSHOT.jar`
   - Installs to local Maven repository

## What We're NOT Doing

❌ **NOT excluding torque-orm from builds**
❌ **NOT skipping compilation**
❌ **NOT preventing module from being built**
❌ **NOT using build profiles to skip it**

## What We ARE Doing

✅ **Preserving base classes** (via version control)
✅ **Building torque-orm normally** (compiles all classes)
✅ **Using standard Maven workflow** (no special steps)
✅ **All modules build in dependency order**

## Verification

Run `mvn clean install -DskipTests` and you'll see:

```
[INFO] Reactor Build Order:
[INFO] 
[INFO] UOB Turbine 7 Portal Multi-Module          [pom]
[INFO] Torque ORM Module                          [jar]  ← Built every time
[INFO] Turbine Model Controller Module            [jar]
[INFO] Spring REST API Module                     [jar]
[INFO] Web Application Module                     [war]
```

All modules build, including torque-orm! ✅

## Summary Table

| Question | Answer |
|----------|--------|
| Is torque-orm excluded from builds? | **NO** - It builds every time |
| Does it skip compilation? | **NO** - All classes are compiled |
| Are base classes regenerated? | **NO** - They come from version control |
| Is standard Maven workflow used? | **YES** - `mvn clean install` works normally |
| Do other modules depend on it? | **YES** - They need the JAR file |

## The Real Solution

**Base classes in version control** = Available after `mvn clean`
**Torque-ORM builds normally** = Compiles all classes every build
**Standard workflow** = No special steps needed

**Build Command**:
```bash
mvn clean install -DskipTests
```

**Result**: All modules build, including torque-orm! ✅
