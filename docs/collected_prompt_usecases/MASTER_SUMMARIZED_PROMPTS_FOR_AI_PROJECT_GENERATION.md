# Master Summarized Prompts for AI Project Generation

This file consolidates and normalizes prompt/usecase patterns from `docs/collected_prompt_usecases` into reusable, high-clarity prompts for AI tools.

Use this as the primary reference when you want AI tools to generate project code, migration plans, automation, or architecture artifacts with less ambiguity.

---

## 1) Universal Prompt Structure (Use for Any Project)

Copy and fill this first:

```text
Role:
You are a senior <tech stack> engineer and solution architect.

Objective:
<what to build/upgrade/fix in one sentence>

Business Context:
<why this is needed, target users, constraints>

Inputs:
- Repo/project path: <path>
- Branch: <branch>
- Existing tech stack: <versions/frameworks>
- Reference files/docs: <list>

Deliverables:
1. <code/output 1>
2. <code/output 2>
3. <documentation/report>
4. <test evidence>

Quality Gates:
- Build must pass
- Unit/integration tests for changed logic
- No hardcoded secrets
- Backward compatibility risks explicitly listed

Execution Rules:
- Analyze first, then propose plan, then implement
- Keep changes small and traceable
- Preserve functional behavior unless explicitly changed
- If uncertain, mark assumptions clearly

Output Format:
1) Summary
2) Files changed
3) Tests run + results
4) Risks and follow-ups
```

---

## 2) Legacy Upgrade / Migration Prompt (Torque, Turbine, JDK, Jakarta)

```text
Review this legacy Java application and prepare a migration implementation for:
- Current: Torque 3.x / Turbine 2.x or 3.x / Spring 4.x / Java EE / JDK 8
- Target: Torque 7.x / Turbine 7.x / Spring 6.x / Jakarta EE / JDK 17

Requirements:
1. Inventory all imports/APIs and identify deterministic vs non-deterministic changes.
2. Generate a migration plan with:
   - mechanical changes (can automate)
   - semantic/runtime-risk changes (manual review needed)
3. Create OpenRewrite strategy:
   - standard recipes for deterministic import changes
   - marker/manual queue for complex MapBuilder/Village/BaseObject replacements
4. Preserve functional parity (especially DB mappings and security model).
5. Produce:
   - migration checklist
   - risk matrix (compile-time + runtime)
   - effort estimate by skill level
   - phased rollout plan with rollback strategy

Implementation constraints:
- No false "fully automated" claim for semantic migrations.
- Explicitly flag runtime unknowns and test strategy for each.
```

---

## 3) Functional Equivalence Prompt (Old vs New ORM Behavior)

```text
Create two comparable modules:
1) Legacy-style implementation (Torque 3-era patterns)
2) Modern implementation (Torque 7-compatible patterns)

Then produce a structural + behavioral comparison:
- CRUD behavior parity
- metadata handling differences
- query/result traversal differences
- performance and maintenance trade-offs

Output:
- side-by-side code map
- migration adapter recommendations
- guide for medium-skilled Java developer to complete migration safely
```

---

## 4) Batch Processing Modernization Prompt

```text
Design a production-ready batch processing solution for host handoff files with Header/Detail/Trailer records.

Requirements:
1. Parse fixed-width or configurable format records.
2. Support dynamic parsing strategy:
   - control-file driven mapping
   - regex/position-based fallback
3. Generate XML representation and persist parsed data to DB.
4. Validate trailer counts and reconcile mismatches.
5. Add high-volume support:
   - multithreading/parallel processing
   - checkpoint/restart
   - failure recovery and audit logs
6. Include test strategy for schema changes (new incoming fields) and data validation.

Deliverables:
- parser framework
- config format examples
- DB schema and insert logic
- reconciliation report format
- unit/integration/performance test plan
```

---

## 5) PR Review Automation Prompt (Python Control Plane)

```text
Build a Python-first PR review automation pipeline for enterprise Java repositories.

Scope:
1. Fetch PR metadata and checkout PR scope (Bitbucket/Git).
2. Produce changed-line mapping report.
3. Compile using Maven/Gradle.
4. Run analyzers (SpotBugs/FindSecBugs, Semgrep, PMD, Dependency-Check, SonarScanner).
5. Merge outputs into unified report with "in_pr_diff" flag.

Deliverables:
- CLI entrypoint
- config schema with env-var substitution
- JSON/HTML summary reports
- compile report with parsable errors
- architecture doc and extension path for agentic triage

Constraints:
- Python orchestrates; Java tools run via subprocess.
- No secrets in code/config files.
- Deterministic outputs for CI usage.
```

---

## 6) Agentic Workflow Prompt (Multi-Agent Use Cases)

```text
Design agentic workflows for enterprise operations with human-in-the-loop approvals.

Use cases:
1) L1 support diagnostics (Unix/JBoss logs, triage, safe restart flow)
2) Batch job failure recovery (decision gates for retry vs escalation)
3) Compliance/code governance (PR policy checks, risk-based blocking)

Requirements:
- persistent state across steps
- explicit pause/approval points for risky actions
- action audit trail for compliance
- fallback/escalation policy

Output:
- workflow graph
- node responsibilities
- tool interface contracts
- control and audit model
```

---

## 7) UI/Wireframe-to-Code Prompt (Turbine/Velocity)

```text
Create <screen name> from attached wireframe for Apache Turbine 7 + Velocity.

Provide:
- exact layout specs (header/sidebar/content/footer)
- component specs (table/forms/actions)
- color/typography/accessibility standards
- responsive behavior requirements

Generate:
1. Velocity template (.vm)
2. CSS file
3. JavaScript file (if needed)
4. Screen/action class integration points

Must-have:
- match legacy visual behavior where required
- list assumptions before implementation
- include smoke test checklist for screen behavior
```

---

## 8) Team Enablement / Awareness Content Prompt

```text
Create a team session deck with speaker notes on AI tools evolution:
- Gen1: assistive copilots
- Gen2: prompt-to-outcome tools (code + test + fix)
- Gen3: agentic orchestration across use cases/features

Include:
- current organization baseline and limits
- role-wise adoption (developers/system analysts/business analysts)
- free tool options for learning
- 90-day upskilling roadmap
- governance and safe-usage checklist

Output:
- presentation file
- presenter notes for each slide
- concise README explaining when to use each deck variant
```

---

## 9) Prompt Quality Checklist (Before Sending to AI Tool)

- Is the objective single and explicit?
- Are input paths/versions/branches defined?
- Are outputs and success criteria measurable?
- Are constraints and non-goals clear?
- Are risks and assumptions required in output?
- Is there a request for test/build evidence?

If any answer is "no", revise prompt before running.

---

## 10) Quick Starter Prompt (Minimal, High Quality)

```text
Analyze the project at <path> and implement <goal> with minimal risk.
First provide a 5-step plan, then execute.
Preserve existing behavior unless changed by requirement.
Run build/tests and report results.
Return:
1) summary
2) files changed
3) test/build output summary
4) risks/next steps
```

---

This file is intentionally tool-agnostic so it can be used with Cursor, Copilot-style assistants, agent frameworks, and other AI development tools.
