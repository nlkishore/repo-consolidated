# Java Entry-Class Call Hierarchy — Plan

**Purpose:** Plan a reusable utility that, given an **entry class + method**, discovers the **dependent class list** and **method call hierarchy** across a modular Java web application (Spring or plain Java).

**Status:** P1 implemented — see `Java/call-hierarchy-analyzer/`  
**Traceability:** [Prompts.txt](collected_prompt_usecases/Prompts.txt) (lines 206–210)  
**Primary open-source stack:** [JavaParser](https://github.com/javaparser/javaparser) + **JavaSymbolSolver**  
**Related existing spikes:**
- `Java/master/modules/java/ReverseHierarchy/CalleeToCaller.java` — name-only reverse map (no type resolution)
- `eclipse-workspace/SpringJDBC/modules/java/PrintCallHierarchy/` — single-file method-call visitor spike

**Design (detailed):** [review/JAVA_CALL_HIERARCHY_DESIGN.md](review/JAVA_CALL_HIERARCHY_DESIGN.md)

---

## 1. Problem statement

In large modular web apps, impact analysis and TDD / review often start from one **entry point** (for example a Spring `@RestController` / `@Controller` handler method). Teams need:

1. **Which classes** are reachable from that entry method (direct and transitive).  
2. **Method hierarchy** — the call tree / call graph from that method into services, repositories, helpers, etc.  
3. Behavior that works for **any Java type**, not only Spring stereotypes.

Manual “follow the code” across modules is slow and error-prone. Name-only AST visitors (existing spikes) mis-resolve overloaded methods and same-named methods in different classes.

---

## 2. Goals and non-goals

### Goals

| # | Goal |
|---|------|
| G1 | Accept **entry FQN class + method signature** (and optional source roots / modules). |
| G2 | Produce **class list** and **method call hierarchy** (tree + flat edges). |
| G3 | Use **JavaParser + Symbol Solver** so calls resolve to declaring types (not method-name string match alone). |
| G4 | Support **multi-module / multi-source-root** projects (**source roots + dependency JARs** on the analyzer TypeSolver classpath). |
| G5 | Framework-agnostic core; optional Spring helpers (detect `@RequestMapping` methods as entry candidates). |
| G6 | Export for **easy reading and quick dependency understanding**: primary **CSV / Excel**, plus JSON (automation), Markdown/Mermaid, Graphviz DOT, and optional HTML. |
| G7 | Make **supporting classes from JARs** visible: resolve types/methods in dependency JARs; optionally expand call hierarchy **into** JAR bytecode (phase P2+). |

### Non-goals (v1)

| # | Non-goal |
|---|----------|
| N1 | Full runtime / dynamic call analysis (reflection, proxies, `Method.invoke`). |
| N2 | Guaranteed Spring bean implementation binding for every `@Autowired` interface (heuristic / config optional later). |
| N3 | Decompiling JARs back to editable source — bytecode walk is enough for hierarchy expansion. |
| N4 | Replacing IDE “Call Hierarchy” UI — this is a **CLI / library** for automation and docs. |
| N5 | Downloading artifacts from remote Nexus/Artifactory inside the analyzer — user/CI supplies local JAR paths (or a prep script). |

---

## 3. Approach summary

```text
Entry: com.bank.app.web.OrderController#createOrder(OrderRequest)
                    │
                    ▼
        Index sources (JavaParser)
        + TypeSolver: sources + JAR libs + JDK reflection
                    │
                    ▼
        Resolve entry MethodDeclaration (from sources)
                    │
                    ▼
        Walk calls; resolve callees (source OR jar types)
                    │
                    ▼
        Recurse into source method bodies;
        for JAR callees: leaf (P1) or bytecode expand (P2+)
                    │
                    ▼
        Emit Excel/CSV (mark origin: SOURCE | JAR | JDK)
```

**Why Symbol Solver:** Existing `CalleeToCaller` maps `"methodC" → callers` by **simple name**. That fails for overloads and cross-class collisions. Symbol Solver binds each call to `DeclaringType#method(params)`.

**Why JARs must be on the analyzer path:** Modular banking apps call shared frameworks and sibling modules shipped as JARs (not always as source roots). Without `JarTypeSolver`, those calls stay **unresolved** and disappear from the hierarchy.

---

## 3.0 Supporting classes inside JARs

| Capability | P1 (MVP) | P2+ |
|------------|----------|-----|
| Resolve call to a class/method **defined in a JAR** | Yes — show as hierarchy node / class list row | Yes |
| Expand **into** that JAR method’s further calls | No — treat as **leaf** (origin=`JAR`) | Yes — bytecode via ASM / ClassFile API |
| Prefer source over JAR for same FQN | Yes — `JavaParserTypeSolver` before `JarTypeSolver` | Same |

**How users include JARs** (analyzer must see them — details in design §7.1):

1. `--jar path/to/lib.jar` (repeatable) and/or `--lib-dir path/to/libs`  
2. `--classpath` file from Maven/Gradle (`dependency:build-classpath`)  
3. Exploded EAR/WAR: `--lib-dir .../WEB-INF/lib` plus module JARs  
4. Config file `jars:` / `libDirs:` (no hardcoded machine paths in code)

Prep recipes (run **before** the analyzer):

```text
# Maven — write compile classpath to a file the analyzer reads
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
# then: --classpath-file cp.txt

# Maven — copy deps into a folder
mvn -q dependency:copy-dependencies -DoutputDirectory=./analyzer-libs
# then: --lib-dir ./analyzer-libs

# Gradle
./gradlew -q printCompileClasspath   # or copyTo analyzer-libs
```

Bank / shared internal JARs not in Maven: place under a known `lib/` or point `--jar` at the corporate shared folder used by the app build.

---

## 3.1 Emission — quick dependency understanding

Reviewers should open one file and see dependencies without reading JSON trees.

| Format | When to use | What it shows |
|--------|-------------|----------------|
| **Excel (.xlsx)** — default human output | Business / BA / reviewer walkthrough | Multi-sheet workbook (Summary, Classes, Hierarchy, Edges, Unresolved) |
| **CSV** | Diff in Git, Excel Lite, scripts | Same columns as Excel sheets (one file per sheet or zip of CSVs) |
| JSON | Automation / agents / further tooling | Canonical full report |
| **Mermaid** (`.md`) | PR comments, Confluence, GitHub preview | Flowchart of caller→callee |
| Graphviz **DOT** | Large graphs (render offline) | Full digraph |
| **HTML** (optional P2) | Browser filter/search | Collapsible tree + class filter |

**Excel / CSV sheet intent (see design for columns):**

1. **Summary** — entry, class count, edge count, max depth, unresolved count  
2. **Classes** — unique FQNs + first-seen depth + package + **origin (SOURCE/JAR/JDK)** + **jar_name** when applicable  
3. **Hierarchy** — indented / path-style rows (depth, caller, callee, full path) for top-down reading  
4. **Edges** — flat caller→callee list (filter/sort/pivot in Excel)  
5. **Unresolved** — calls that need manual follow-up (often missing JAR on `--lib-dir`)  
6. **Classpath** (optional sheet) — which JARs/source roots were loaded (auditability)

---

## 4. Deliverables (phased)

| Phase | Deliverable | Outcome |
|-------|-------------|---------|
| P0 | Plan + design docs (this + design) | Review gate |
| P1 | CLI + library MVP | Sources + **JarTypeSolver**; JAR callees as **leaves**; Excel/CSV/JSON |
| P2 | Filters, Mermaid/DOT/HTML, **JAR bytecode expand** (ASM) | Hierarchy continues inside selected JARs |
| P3 | Optional Spring entry discovery | List candidate controller methods |
| P4 | Interface→impl hints (Spring stereotype scan / config map) | Better service-layer accuracy |

---

## 5. Success criteria

- Given a known sample app entry method, output includes expected service/repository methods in the tree.  
- Same tool works on a **non-Spring** plain Java entry class.  
- Call into a **supporting class shipped only as a JAR** resolves and appears in Classes/Hierarchy with `origin=JAR` (when that JAR is supplied).  
- Without the JAR on the path, the same call is listed under **Unresolved** with a clear “type not found / add --jar” hint.  
- **Excel and CSV** are readable: Summary, Classes, Hierarchy (with path), and Edges sheets/files match the fixture.  
- Cycles and missing symbols are reported without crashing.  
- Config and secrets stay out of source (paths via CLI/env/config file).

---

## 6. Open questions for review

1. Default project location: new module under `Java/` vs `C:\Python-Cursor` sibling Java tool under `repo-consolidated/git-tools`?  
   **Recommendation:** `Java/call-hierarchy-analyzer/` (Maven) under repo-consolidated.  
2. Max recursion depth / include JDK / include third-party jars?  
   **Recommendation:** default depth 20; exclude `java.*` / `javax.*` / `jakarta.*` unless `--include-jdk`; include **app and bank shared JARs** by default when listed on `--lib-dir`.  
3. Should constructors and field initializers count as edges?  
   **Recommendation:** yes for constructors called from the entry path; field initializers as optional flag.  
4. Default human format: Excel-only, CSV-only, or both?  
   **Recommendation:** both (`excel,csv`); JSON opt-in or always alongside for automation.  
5. Which JARs to expand beyond leaf in P2?  
   **Recommendation:** only packages matching `--include-package` / `--expand-jar-package` (e.g. `com.bank.`); never expand all of Spring by default.

---

## 7. Next step

Review this plan, then approve [JAVA_CALL_HIERARCHY_DESIGN.md](review/JAVA_CALL_HIERARCHY_DESIGN.md) before implementation (P1).
