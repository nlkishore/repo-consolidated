# Testbed — Corporate Banking Domain Seed & Validation

Self-contained Python tool to seed and validate the GEB/GTP domain (Company, Entity, User, Group, Role, Permission) for the corporate banking web application supporting **Payments**, **Collections**, and **Trade** features.

Design: `repo-consolidated/docs/review/TESTBED_DESIGN.md`

---

## Quick start — CMD (no PowerShell)

Open **Command Prompt** (`cmd.exe`):

```cmd
cd C:\MyGeneratedProjects\GitRepoPlan\repo-consolidated\Python\python-cursor\modules\python\testbed

pip install -e ".[dev]"

set MYSQL_ADMIN_USER=kishore
set MYSQL_ADMIN_PASSWORD=your_admin_password
scripts\setup-mysql.bat

scripts\run-testbed.bat
```

**Plain CMD commands (copy/paste, no scripts):**

```cmd
cd C:\MyGeneratedProjects\GitRepoPlan\repo-consolidated\Python\python-cursor\modules\python\testbed

set DB_HOST=localhost
set DB_NAME=testbed
set DB_USER=testbed
set DB_PASSWORD=
set PATH=C:\Program Files\MySQL\MySQL Server 8.1\bin;%PATH%

net start MySQL81

pip install -e ".[dev]"

python -m testbed run-all --config config\settings.local.yaml
```

---

## Quick start — Git Bash

```bash
cd /c/MyGeneratedProjects/GitRepoPlan/repo-consolidated/Python/python-cursor/modules/python/testbed

pip install -e ".[dev]"

export MYSQL_ADMIN_USER=kishore
export MYSQL_ADMIN_PASSWORD=your_admin_password
bash scripts/setup-mysql.sh

bash scripts/run-testbed.sh
```

**Plain Git Bash commands (no scripts):**

```bash
cd /c/MyGeneratedProjects/GitRepoPlan/repo-consolidated/Python/python-cursor/modules/python/testbed

export DB_HOST=localhost DB_NAME=testbed DB_USER=testbed DB_PASSWORD=
export PATH="/c/Program Files/MySQL/MySQL Server 8.1/bin:$PATH"

pip install -e ".[dev]"

python -m testbed run-all --config config/settings.local.yaml
```

---

## Scripts reference

| CMD (`.bat`) | Git Bash (`.sh`) | Purpose |
|--------------|------------------|---------|
| `scripts\env-testbed.bat` | `source scripts/env-testbed.sh` | Set `DB_*` env vars + MySQL PATH |
| `scripts\setup-mysql.bat` | `bash scripts/setup-mysql.sh` | Create DB, user, tables |
| `scripts\run-testbed.bat` | `bash scripts/run-testbed.sh` | Full run-all |
| `scripts\start-workbench.bat` | — | Open MySQL Workbench |

PowerShell scripts (`.ps1`) are also available if your environment allows them.

---

## CMD examples

```cmd
call scripts\env-testbed.bat

scripts\run-testbed.bat validate

scripts\run-testbed.bat seed payments

scripts\run-testbed.bat report

scripts\start-workbench.bat
```

---

## Git Bash examples

```bash
source scripts/env-testbed.sh

bash scripts/run-testbed.sh validate

bash scripts/run-testbed.sh seed payments
```

---

## Local MySQL configuration

| Setting | Value |
|---------|-------|
| MySQL home | `C:\Program Files\MySQL\MySQL Server 8.1` |
| Service | `MySQL81` |
| Schema | `testbed` |
| User | `testbed@localhost` |
| Password | *(empty — local dev only)* |
| Config file | `config/settings.local.yaml` |

**MySQL Workbench:** `localhost:3306`, user `testbed`, password *(empty)*, schema `testbed`.

---

## Python CLI commands

| Command | Description |
|---------|-------------|
| `python -m testbed run-all --config config\settings.local.yaml` | Reset + seed all + validate + report |
| `python -m testbed seed --all --config config\settings.local.yaml` | Seed all scenarios |
| `python -m testbed seed --scenario payments --config config\settings.local.yaml` | One scenario |
| `python -m testbed validate --config config\settings.local.yaml` | Integrity checks |
| `python -m testbed reset --yes --config config\settings.local.yaml` | Truncate tables |
| `python -m testbed report --format html --config config\settings.local.yaml` | HTML report |

---

## Scenarios and sample logins

Default password for all personas: **`TestPass1!`**

| Scenario | Example login IDs | Roles |
|----------|-------------------|-------|
| `admin` | `admin-sysadmin-c100` | ADMIN |
| `payments` | `pay-maker-c101` | PAY_MAKER |
| `collections` | `coll-officer-c102` | COLL_OFFICER |
| `trade` | `trade-officer-c103` | TRADE_OFFICER |
| `entity_user` | `ent-user-c104` | ENTITY_USER |

---

## Tests (no DB)

```cmd
python -m pytest tests\ -v
```

---

## Report

After `run-all`: `testbed-reports\testbed-summary.html`

---

## Other environments

Copy `config/settings.example.yaml` to `config/settings.yaml` and set `DB_*` variables. Do not commit `settings.yaml`.
