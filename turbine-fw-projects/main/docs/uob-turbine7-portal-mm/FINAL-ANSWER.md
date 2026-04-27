# Final Answer: Build Workflow

## Your Questions Answered

### Q1: Does this mean torque-ORM should be excluded in subsequent builds?

**A: NO** - Torque-ORM builds **every time** like all other modules.

### Q2: Has this taken care not to build torque-ORM when mvn clean install executed?

**A: NO** - Torque-ORM **IS built** when `mvn clean install` is executed. This is correct and necessary.

## What Actually Happens

### During `mvn clean install`

**All 4 modules build in order:**

1. ✅ **Parent Module** (pom)
2. ✅ **torque-orm Module** - **BUILDS EVERY TIME**
3. ✅ **turbine-model-controller Module**
4. ✅ **spring-rest-api Module**
5. ✅ **webapp Module**

### Torque-ORM Build Process

```
mvn clean install
│
└─ torque-orm Module
   ├─ Clean: Delete target/
   ├─ Generate Sources:
   │  ├─ Copy base classes: src/main/generated-base-classes → target/generated-sources ✅ (AUTOMATIC)
   │  └─ Torque generation: Runs
   ├─ Compile:
   │  ├─ Base classes (52 files from target/generated-sources)
   │  └─ Modifiable classes (52 files from src/main/java)
   │  └─ Total: 104 source files compiled ✅
   └─ Package: Create torque-orm-1.0-SNAPSHOT.jar
```

## Why Torque-ORM Must Build

1. **Modifiable classes may change** - Need recompilation
2. **Other modules depend on it** - Need the JAR file
3. **Standard Maven workflow** - All modules build

## The Solution We Implemented

### Automated Base Class Copying

**Base classes location**:
- **Committed**: `torque-orm/src/main/generated-base-classes/com/uob/om/Base*.java`
- **Build target**: `torque-orm/target/generated-sources/com/uob/om/Base*.java`

**Maven resources plugin** automatically copies base classes during `generate-sources` phase.

### Build Workflow

**One-time setup**:
```bash
# Copy base classes to committed location
cd torque-orm
New-Item -ItemType Directory -Path "src\main\generated-base-classes\com\uob\om" -Force
Copy-Item -Path "..\..\uob-turbine7-portal\target\generated-sources\com\uob\om\Base*.java" -Destination "src\main\generated-base-classes\com\uob\om\" -Force
cd ..
git add torque-orm/src/main/generated-base-classes/com/uob/om/Base*.java
git commit -m "Add generated Torque base classes"
```

**Regular builds**:
```bash
# Standard Maven command - base classes auto-copied
mvn clean install -DskipTests
```

## Summary Table

| Question | Answer |
|----------|--------|
| Is torque-orm excluded from builds? | **NO** - Builds every time |
| Does it skip compilation? | **NO** - Compiles all classes (104 files) |
| Are base classes regenerated? | **NO** - Copied from committed location |
| Are multiple build steps required? | **NO** - Standard `mvn clean install` |
| Is standard Maven workflow used? | **YES** - No special steps |

## Verification

Run `mvn clean install -DskipTests` and you'll see:

```
[INFO] Reactor Build Order:
[INFO] 
[INFO] UOB Turbine 7 Portal Multi-Module          [pom]
[INFO] Torque ORM Module                          [jar]  ← Built every time ✅
[INFO] Compiling 104 source files                 ← Base + modifiable classes ✅
[INFO] Turbine Model Controller Module            [jar]
[INFO] Spring REST API Module                     [jar]
[INFO] Web Application Module                     [war]
```

## Final Answer

**Q: Is torque-orm excluded from builds?**
**A: NO** - It builds every time, which is correct.

**Q: Are multiple build steps required?**
**A: NO** - After one-time setup, use standard `mvn clean install -DskipTests`

**Build Command**:
```bash
mvn clean install -DskipTests
```

All modules build, including torque-orm! ✅
