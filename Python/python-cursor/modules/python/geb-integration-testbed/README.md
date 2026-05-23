# GEB Integration Testbed

Contract-driven validation for **IDB → GEB → EAI** flows (Maker, Checker, Single-user).

Design: `repo-consolidated/docs/review/GEB_INTEGRATION_TESTBED_DESIGN.md`  
Data layer (users/RBAC): `../testbed/` — run `testbed run-all` first.

---

## Quick start (CMD — no PowerShell)

```cmd
cd C:\MyGeneratedProjects\GitRepoPlan\repo-consolidated\Python\python-cursor\modules\python\geb-integration-testbed

pip install -e ".[dev]"

scripts\run-geb-testbed.bat
```

**Git Bash**

```bash
pip install -e ".[dev]"
bash scripts/run-geb-testbed.sh
```

**Plain command**

```cmd
python -m geb_testbed run-all --config config\settings.example.yaml
```

---

## What it validates (sample data included)

| Scenario | Persona | Fixtures | Contracts |
|----------|---------|----------|-----------|
| `maker_payments` | Maker | `fixtures/json/maker_payment_valid.json` (+ negative) | IDB submit |
| `checker_payments` | Checker | JSON approve + XML outbound + EAI response | IDB + EAI |
| `single_user_payments` | Single user | JSON + XML | IDB + EAI |

Reports: `geb-testbed-reports/geb-contract-matrix.html`

---

## Commands

```cmd
python -m geb_testbed list-scenarios
python -m geb_testbed validate --scenario maker_payments
python -m geb_testbed run-all
```

---

## Project layout

```text
contracts/idb/          IDB JSON element tables (from Word contract)
contracts/eai/            EAI XML XPath element tables
contracts/mappings/       IDB element -> EAI element traceability
fixtures/json/            Sample IDB requests
fixtures/xml/             Sample EAI outbound/response messages
src/geb_testbed/          Validators, scenarios, CLI, reports
scripts/                  run-geb-testbed.bat / .sh
```

---

## Extend for your GEB APIs

1. Add rows to `contracts/idb/your_api_v1.yaml` (mirror Word table).
2. Add sample JSON under `fixtures/json/`.
3. Register scenario in `src/geb_testbed/scenarios/registry.py`.
4. Run `python -m geb_testbed validate --scenario your_scenario`.

---

## Tests

```cmd
python -m pytest tests\ -v
```
