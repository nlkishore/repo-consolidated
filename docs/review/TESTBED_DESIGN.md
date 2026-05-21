# Testbed Design — Corporate Banking Domain

**Prompt source:** [Prompts.txt](../collected_prompt_usecases/Prompts.txt) lines 119–122

**Tool:** `C:\Python-Cursor\testbed\` | synced to `repo-consolidated/Python/python-cursor/modules/python/testbed/`  
**Related:** [CR_SLICE_REVIEW_RUNBOOK.md](CR_SLICE_REVIEW_RUNBOOK.md)

---

## What is a Testbed?

A **Testbed** is a controlled, reproducible environment that can be bootstrapped on demand for a specific software application. In the context of this corporate banking web application it means:

| Property | Description |
|----------|-------------|
| **Relationship-complete** | Every domain object (Company, Entity, User, Group, Role, Permission) is present *and linked correctly* — no orphan rows |
| **Feature-aligned scenarios** | Seed data is organized by business feature: Payments, Collections, Trade, Admin |
| **One-command setup** | `testbed run-all` resets, seeds, validates, and produces a report in under 30 seconds |
| **Idempotent** | Running the tool twice produces the same state — safe to re-run in CI or dev |
| **Validated** | Built-in assertions verify FK integrity and count thresholds after every seed |

### Testbed vs plain test data

| Aspect | Plain SQL scripts | Testbed |
|--------|-----------------|---------|
| Setup | Manual `mysql < script.sql` | `testbed run-all` |
| Scope | Individual tables | Full cross-table relationship graph |
| Scenarios | Generic `user1`, `role1` | `pay-maker-c01`, `PAY_MAKER` role with `INITIATE_PAYMENT` permission |
| Reset | Drop + recreate manually | `testbed reset --yes` |
| Validation | None | FK integrity + orphan checks + count assertions |
| Reports | None | HTML persona table + JSON summary |

---

## Domain model

The GEB/GTP schema does **not** have a standalone `GTP_COMPANY` table. Company is represented as `COMPANY_ID` and `COMPANY_ABBV_NAME` fields on both `GTP_USER` and `GTP_ENTITY`. The testbed seeds the full graph through those relationships.

```
GTP_COMPANY (virtual: COMPANY_ID)
     │
     ├──► GTP_ENTITY  (ENTITY_ID, COMPANY_ID, NAME, ABBV_NAME, COUNTRY, ...)
     │         │
     │         ├──► GTP_USER_ENTITY   (USER_ID, ENTITY_ID, DEFAULT_ENTITY)
     │         └──► GTP_ENTITY_ROLE   (ENTITY_ID, ROLE_ID)
     │
     ├──► GTP_USER    (USER_ID, LOGIN_ID, COMPANY_ID, COMPANY_ABBV_NAME, ...)
     │         │
     │         └──► GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
     │
     ├──► GTP_GROUP   (GROUP_ID, GROUPNAME, COMPANY_ID)
     │         │
     │         ├──► GTP_GROUP_ROLE        (GROUP_ID, ROLE_ID)
     │         └──► GTP_USER_GROUP_ROLE   (USER_ID, GROUP_ID, ROLE_ID)
     │
     ├──► GTP_ROLE    (ROLE_ID, ROLENAME, ROLETYPE)
     │         │
     │         ├──► GTP_ROLE_PERMISSION   (ROLE_ID, PERMISSION_ID)
     │         ├──► GTP_COMPANY_ROLE      (COMPANY_ID, ROLE_ID)
     │         └──► GTP_ENTITY_ROLE       (ENTITY_ID, ROLE_ID)
     │
     └──► GTP_PERMISSION (PERMISSION_ID, PERMISSION)
```

---

## Scenarios

| Scenario | Key personas (LOGIN_ID prefix) | Roles | Permissions | Feature |
|----------|-------------------------------|-------|-------------|---------|
| **admin** | `admin-sysadmin`, `audit-officer` | ADMIN, SUPER_USER, AUDITOR | ADMIN_ACCESS, MANAGE_USERS, AUDIT_LOG | All features |
| **payments** | `pay-maker`, `pay-checker`, `pay-viewer` | PAY_MAKER, PAY_CHECKER, PAY_VIEWER | INITIATE_PAYMENT, APPROVE_PAYMENT, VIEW_PAYMENT, REJECT_PAYMENT, AMEND_PAYMENT | Payments |
| **collections** | `coll-officer`, `coll-approver`, `coll-viewer` | COLL_OFFICER, COLL_APPROVER, COLL_VIEWER | MANAGE_COLLECTION, APPROVE_COLLECTION, VIEW_COLLECTION, REJECT_COLLECTION | Collections |
| **trade** | `trade-officer`, `trade-approver`, `trade-viewer` | TRADE_OFFICER, TRADE_APPROVER, TRADE_VIEWER | INITIATE_TRADE, APPROVE_TRADE, VIEW_TRADE, AMEND_TRADE, REJECT_TRADE | Trade |
| **entity_user** | `ent-user` | ENTITY_USER, ENTITY_VIEWER | VIEW_DASHBOARD, VIEW_ACCOUNT, VIEW_STATEMENT | Read-only |

Each scenario ID range is partitioned to prevent collisions when all are seeded together (e.g. permissions 1–5 for admin, 11–15 for payments).

---

## How Claude assists the testbed

Claude plays a **design-time** role only — the tool executes deterministically at runtime with no AI dependency.

### 1. Scenario generation

A developer or BA describes a feature area in plain language, e.g.:

> *"Build a Trade maker-checker workflow for two Singapore entities under company TRADESVC, where the maker can initiate and amend, the checker can approve or reject, and both can view."*

Claude translates this into the exact scenario configuration — roles, permissions, RBAC mappings, `cr-manifest.yaml` or scenario YAML — which the developer commits and the testbed tool executes.

### 2. Validation narrative

After `testbed validate`, Claude can read the JSON report and explain:
- Why a specific user/permission combination is expected for a feature
- Flag incomplete relationship chains (e.g. user has a role but that role has no permissions)
- Surface gaps between testbed scenarios and actual application feature requirements

### 3. Onboarding accelerator

New team members ask: *"What users exist and what can each do?"* — Claude reads `testbed-summary.html` or the JSON report and produces a plain-English persona summary without needing a DB client.

**Human-in-the-loop model:** Claude authors scenarios and interprets results; Python executes and validates without AI involvement.

---

## CLI quick reference

```bash
# One command: reset + seed all + validate + report
testbed run-all --config config/settings.yaml

# Seed a specific scenario
testbed seed --scenario trade --config config/settings.yaml

# Attach to an existing company (don't create new entities)
testbed seed --scenario payments --company-id 101

# Validate without re-seeding
testbed validate --config config/settings.yaml

# Produce HTML report from live DB
testbed report --format html

# Safe teardown
testbed reset --yes
```

---

## Prerequisites

1. Python 3.11+
2. MySQL 8+ / MariaDB 10.6+
3. GTP tables created (run once per DB):

```bash
# Portable GTP security tables (Group, Role, Permission, User, join tables)
mysql -u kishore -p kishore < repo-consolidated/turbine-fw-projects/db-config/apps/uob-turbine7-portal-mm/02-create-tables.sql

# Full GEB schema (adds GTP_ENTITY, GTP_ENTITY_ROLE, GTP_COMPANY_ROLE, GTP_USER_ENTITY)
mysql -u root -p geb_db < "Domain Based Model/corporate-banking-auth-matrix/database-schema.sql"
```

---

## Security

- Passwords: SHA-256 by default (configurable; use `plain` only for legacy ClearCrypt Turbine targets)
- Reports list only login names, role names, counts — no password hashes, no PII values
- `config/settings.yaml` is gitignored; only `settings.example.yaml` is committed

---

## Output artifacts

| File | Description |
|------|-------------|
| `testbed-reports/testbed-summary.html` | Persona table, validation results, row counts |
| `testbed-reports/testbed-summary.json` | Machine-readable; consumable by CI or Claude |

---

## Existing seed assets (reused)

| File | Role |
|------|------|
| [02-create-tables.sql](../../turbine-fw-projects/db-config/apps/uob-turbine7-portal-mm/02-create-tables.sql) | DDL — tables must exist before seeding |
| [03-load-test-data.sql](../../turbine-fw-projects/db-config/apps/uob-turbine7-portal-mm/03-load-test-data.sql) | Reference for base role/group names |
| [database-schema.sql](../../../MyGeneratedProjects/Domain\ Based\ Model/corporate-banking-auth-matrix/database-schema.sql) | GEB table structure (Entity, Company links) |
