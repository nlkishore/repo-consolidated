# Call Hierarchy Analyzer

Builds a **class list** and **method call hierarchy** from an entry class/method using [JavaParser](https://github.com/javaparser/javaparser) + Symbol Solver.

Designed for modular Java web apps (Spring or plain Java). Supporting classes shipped only in JARs are resolved via `JarTypeSolver` and appear as hierarchy leaves (`origin=JAR`) in P1.

## Build

```bash
cd Java/call-hierarchy-analyzer
mvn -q clean package
```

Fat JAR: `target/call-hierarchy-analyzer-1.0.0-SNAPSHOT.jar`

## Prepare dependency JARs

```bash
# Maven project under analysis
mvn -q dependency:copy-dependencies -DoutputDirectory=./analyzer-libs

# Or classpath file:
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
```

Windows helper: `scripts/prepare-classpath.bat`  
Unix helper: `scripts/prepare-classpath.sh`

## Run

```bash
java -jar target/call-hierarchy-analyzer-1.0.0-SNAPSHOT.jar \
  --entry com.bank.web.OrderController#createOrder \
  --source app-web/src/main/java \
  --source app-service/src/main/java \
  --lib-dir analyzer-libs \
  --format excel,csv,json,mermaid \
  --out out/hierarchy
```

### Key flags

| Flag | Meaning |
|------|---------|
| `--entry` | `TypeFqn#method` or `TypeFqn#method(paramTypes)` |
| `--source` | Source root (repeatable) |
| `--jar` | Explicit supporting JAR (repeatable) |
| `--lib-dir` | Directory of JARs |
| `--classpath-file` | Paths from Maven/Gradle |
| `--format` | `excel,csv,json,mermaid` |
| `--out` | Output base path (no extension) |

## Outputs

- `out/hierarchy.xlsx` — Summary, Classes, Hierarchy, Edges, Unresolved, Classpath  
- `out/hierarchy-*.csv` — same sheets as CSV  
- `out/hierarchy.json` — canonical machine report  
- `out/hierarchy.md` — Mermaid flowchart  

Classes from JARs show `origin=JAR` and `jar_name`.

## Docs

- Plan: `docs/JAVA_CALL_HIERARCHY_PLAN.md`  
- Design: `docs/review/JAVA_CALL_HIERARCHY_DESIGN.md`  

## Tests

```bash
mvn -q test
```
