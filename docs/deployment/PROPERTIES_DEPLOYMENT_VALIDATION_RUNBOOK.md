# Production vs UAT properties validation (multi-instance)

Use this runbook before promoting Java application configuration to production. Compare each **instance-specific** UAT (or release-candidate) export against the **same instance’s** production baseline.

## Goals

| Check | Meaning |
|-------|---------|
| **New keys** | Keys in candidate not present in production for the same relative file path |
| **Value overrides** | Keys in both, but candidate value differs from production for that instance |
| **Removed keys** (optional) | Keys in production missing in candidate (`--include-removed`) |

## Folder layout (server exports)

Use a consistent tree so relative paths match across environments:

```text
exports/
  <INSTANCE>/
    prod/          # production snapshot for this host
      conf/
        application.properties
        ...
    uat/           # UAT or release candidate for the same host
      conf/
        application.properties
        ...
```

**Rule:** Compare `prod/` vs `uat/` under the **same** `<INSTANCE>` folder. Do not mix instances (e.g. do not compare `GEBCUSG01A` UAT to `GEBCUSG02A` production).

### Singapore reference instances

| Instance | Role |
|----------|------|
| GEBCUSG01A, GEBCUSG02A, GEBCUSG01F, GEBCUSG01V | Customer web application |
| GEBBASG01A, GEBBASG02A, GEBBASG01F | Batch / daemon JVMs |

Run one tree comparison per instance before go-live.

## Tool location

Python package: `git-tools/main/modules/python/properties_diff/`

From that directory:

```bash
python -m properties_diff compare-trees \
  --baseline-root exports/GEBCUSG01A/prod \
  --candidate-root exports/GEBCUSG01A/uat
```

Single file:

```bash
python -m properties_diff compare-files \
  --baseline exports/GEBCUSG01A/prod/conf/application.properties \
  --candidate exports/GEBCUSG01A/uat/conf/application.properties \
  --relative-path conf/application.properties
```

Wrapper (from same directory):

```bash
python run-tool.py properties_diff compare-trees --baseline-root ... --candidate-root ...
```

## Git-based inputs

When configuration lives in Git instead of a server zip:

1. Check out or export the **production tag/branch** into `exports/<INSTANCE>/prod/`.
2. Check out or export the **UAT/release branch** into `exports/<INSTANCE>/uat/`.
3. Run the same `compare-trees` command.

Examples:

```bash
git archive --format=tar production-tag -- path/to/config | tar -x -C exports/GEBCUSG01A/prod
git archive --format=tar uat-branch -- path/to/config | tar -x -C exports/GEBCUSG01A/uat
```

Or per file:

```bash
git show production-tag:conf/application.properties > exports/GEBCUSG01A/prod/conf/application.properties
git show uat-branch:conf/application.properties > exports/GEBCUSG01A/uat/conf/application.properties
```

Preserve **relative paths** inside the config tree (not only basenames) when multiple `*.properties` exist in an EAR.

## Reading the report

- **NEW keys** — require production approval; confirm defaults and downstream impact.
- **CHANGED values** — confirm intentional per-instance overrides (URLs, pool sizes, feature flags).
- **Candidate-only files** — entire file exists only in UAT; treat as new configuration surface.
- **Baseline-only files** — missing in UAT; use `--include-removed` to list dropped keys if needed.

### Known limitations (v1)

- **Literal diff only** — `${placeholder}` values may show as changed even when resolved values would match.
- **No merge of overlay chains** — compare mirrored directory trees or explicit file pairs; layered `default.properties` + `override.properties` are not merged automatically.
- **Duplicate keys** — parser follows Java `Properties` (last wins) and emits a warning.

## Security

- Do not attach full property dumps with secrets to tickets or email.
- Use `--redact` with patterns for sensitive keys, e.g. `--redact password --redact secret --redact api[._]key`.
- Prefer JSON output (`--format json`) for pipelines; scrub before publishing artifacts.
- Store exports on access-controlled shares; delete after sign-off.

## Pre-production checklist

1. Export production and UAT trees per instance (or materialize from Git).
2. Run `compare-trees` for each instance in the deployment matrix.
3. Review NEW and CHANGED keys with application owner.
4. Document approved overrides in the change record.
5. Re-run after any last-minute UAT fix; exit code `1` means differences remain (`--fail-on-diff` default).

## Example (bundled sample data)

```bash
cd git-tools/main/modules/python
python -m properties_diff compare-trees \
  --baseline-root properties_diff/examples/sg/GEBCUSG01A/prod \
  --candidate-root properties_diff/examples/sg/GEBCUSG01A/uat
```

See also [PROPERTIES_DIFF_README.md](../../git-tools/main/modules/python/PROPERTIES_DIFF_README.md).
