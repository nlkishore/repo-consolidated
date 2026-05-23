# Testbed — Corporate Banking Domain Seed & Validation

Self-contained Python tool to seed and validate the GEB/GTP domain (Company, Entity, User, Group, Role, Permission) for the corporate banking web application supporting **Payments**, **Collections**, and **Trade** features.

Design: `repo-consolidated/docs/review/TESTBED_DESIGN.md`

---

## Quick start (Windows + MySQL 8.1)

**Project paths**

| Location | Purpose |
|----------|---------|
| `C:\Python-Cursor\testbed` | Primary working copy |
| `repo-consolidated/Python/python-cursor/modules/python/testbed` | Synced repo copy |

**One-time MySQL setup** (creates `testbed` schema, no-password user, GTP tables):

```powershell
cd C:\Python-Cursor\testbed
pip install -e ".[dev]"
.\scripts\setup-mysql.ps1
```

**Every run** (seed + validate + report):

```powershell
.\scripts\run-testbed.ps1
```

Or manually:

```powershell
. .\scripts\env-testbed.ps1          # sets DB_HOST, DB_NAME, DB_USER, PATH
python -m testbed run-all --config config/settings.local.yaml
```

**MySQL Workbench**

```powershell
. .\scripts\env-testbed.ps1
Start-MySqlWorkbench
```

Connect to: `localhost:3306`, user `testbed`, password *(empty)*, schema `testbed`.

---

## Local MySQL configuration

| Setting | Value |
|---------|-------|
| MySQL home | `C:\Program Files\MySQL\MySQL Server 8.1` |
| Service | `MySQL81` (auto-start) |
| Schema | `testbed` |
| User | `testbed@localhost` |
| Password | *(empty — local dev only)* |
| Config file | `config/settings.local.yaml` |

Scripts:

| Script | Purpose |
|--------|---------|
| `scripts/env-testbed.ps1` | Load env vars + MySQL PATH for current session |
| `scripts/setup-mysql.ps1` | Create DB, user, tables (repeat-safe) |
| `scripts/run-testbed.ps1` | Full `run-all` with env loaded |
| `sql/00-create-testbed-db-user.sql` | Schema + no-password user |
| `sql/01-create-testbed-schema.sql` | GTP tables (GEB column names) |

---

## Commands

| Command | Description |
|---------|-------------|
| `python -m testbed run-all --config config/settings.local.yaml` | Reset + seed all + validate + report |
| `python -m testbed seed --all --config config/settings.local.yaml` | Seed all scenarios (idempotent) |
| `python -m testbed seed --scenario payments` | Seed one scenario |
| `python -m testbed validate` | Post-seed FK integrity + count assertions |
| `python -m testbed reset --yes` | Truncate all testbed tables |
| `python -m testbed report --format html` | Generate HTML/JSON summary |

---

## Scenarios and sample logins

Default password for all personas: **`TestPass1!`** (SHA-256 hashed in DB)

| Scenario | Example login IDs | Roles | Feature |
|----------|-------------------|-------|---------|
| `admin` | `admin-sysadmin-c100`, `audit-officer-c100` | ADMIN, SUPER_USER, AUDITOR | All |
| `payments` | `pay-maker-c101`, `pay-checker-c101` | PAY_MAKER, PAY_CHECKER | Payments |
| `collections` | `coll-officer-c102`, `coll-approver-c102` | COLL_OFFICER, COLL_APPROVER | Collections |
| `trade` | `trade-officer-c103`, `trade-approver-c103` | TRADE_OFFICER, TRADE_APPROVER | Trade |
| `entity_user` | `ent-user-c104` | ENTITY_USER | Read-only |

---

## Tests (no DB required)

```powershell
python -m pytest tests/ -v
```

---

## Report

After `run-all`: `testbed-reports/testbed-summary.html` — persona table, validation results, row counts.

---

## Other environments

For non-local databases, copy `config/settings.example.yaml` to `config/settings.yaml` and set `DB_*` environment variables. Do not commit `settings.yaml`.

For Turbine portal-mm schema (`PERMISSION_NAME`, `LOGIN_NAME`), use the dedicated `testbed` schema instead — the testbed seeder targets the full GEB column set.
