# Testbed — Corporate Banking Domain Seed & Validation

Self-contained Python tool to seed and validate the GEB/GTP domain (Company, Entity, User, Group, Role, Permission) for the corporate banking web application supporting **Payments**, **Collections**, and **Trade** features.

Design: `repo-consolidated/docs/review/TESTBED_DESIGN.md`

---

## Quick start

```bash
pip install -e ".[dev]"

# Copy and fill in your DB credentials
cp config/settings.example.yaml config/settings.yaml

# Full run: reset + seed all scenarios + validate + report
testbed run-all --config config/settings.yaml
```

---

## Commands

| Command | Description |
|---------|-------------|
| `testbed seed --all` | Seed all scenarios (idempotent) |
| `testbed seed --scenario payments` | Seed one scenario |
| `testbed seed --scenario trade --company-id 103` | Attach to specific company id |
| `testbed validate` | Post-seed FK integrity + count assertions |
| `testbed reset --yes` | Truncate all testbed tables |
| `testbed report --format html` | Generate HTML/JSON summary |
| `testbed run-all` | reset + seed --all + validate + report |

---

## Scenarios

| Scenario | Key personas | Roles | Feature |
|----------|-------------|-------|---------|
| `admin` | admin-sysadmin, audit-officer | ADMIN, SUPER_USER, AUDITOR | All features |
| `payments` | pay-maker, pay-checker, pay-viewer | PAY_MAKER, PAY_CHECKER, PAY_VIEWER | Payments |
| `collections` | coll-officer, coll-approver, coll-viewer | COLL_OFFICER, COLL_APPROVER, COLL_VIEWER | Collections |
| `trade` | trade-officer, trade-approver, trade-viewer | TRADE_OFFICER, TRADE_APPROVER, TRADE_VIEWER | Trade |
| `entity_user` | ent-user | ENTITY_USER, ENTITY_VIEWER | Read-only |

---

## Prerequisites

- Python 3.11+
- MySQL 8+ / MariaDB 10.6+ with GTP tables created:  
  `mysql ... < repo-consolidated/turbine-fw-projects/db-config/apps/uob-turbine7-portal-mm/02-create-tables.sql`
- For **GEB** full schema (Entity, Company links):  
  `mysql ... < "Domain Based Model/corporate-banking-auth-matrix/database-schema.sql"`

---

## Environment variables

| Variable | Description |
|----------|-------------|
| `DB_HOST` | MySQL host |
| `DB_NAME` | Schema/database name |
| `DB_USER` | MySQL username |
| `DB_PASSWORD` | MySQL password |

---

## Tests

```bash
pytest tests/ -v
```

Tests run without a live DB (builders + config only).

---

## Report

`testbed-reports/testbed-summary.html` — persona table, validation results, row counts per scenario.
