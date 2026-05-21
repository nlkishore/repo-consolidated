"""Post-seed validation: row counts, FK integrity, orphan checks."""

from __future__ import annotations

from dataclasses import dataclass, field

from mysql.connector.cursor import MySQLCursor


@dataclass
class ValidationResult:
    ok: bool
    checks: list[dict] = field(default_factory=list)
    errors: list[str] = field(default_factory=list)


def validate(cur: MySQLCursor) -> ValidationResult:
    checks: list[dict] = []
    errors: list[str] = []

    _count_check(cur, "GTP_PERMISSION", checks, errors)
    _count_check(cur, "GTP_ROLE", checks, errors)
    _count_check(cur, "GTP_GROUP", checks, errors)
    _count_check(cur, "GTP_ENTITY", checks, errors)
    _count_check(cur, "GTP_USER", checks, errors)
    _count_check(cur, "GTP_ROLE_PERMISSION", checks, errors)
    _count_check(cur, "GTP_GROUP_ROLE", checks, errors)
    _count_check(cur, "GTP_USER_GROUP_ROLE", checks, errors)
    _count_check(cur, "GTP_USER_ENTITY", checks, errors)

    _orphan_check(
        cur,
        "Orphan GTP_USER_GROUP_ROLE (bad USER_ID)",
        """
        SELECT COUNT(*) AS cnt FROM GTP_USER_GROUP_ROLE ugr
        LEFT JOIN GTP_USER u ON ugr.USER_ID = u.USER_ID
        WHERE u.USER_ID IS NULL
        """,
        checks,
        errors,
    )
    _orphan_check(
        cur,
        "Orphan GTP_USER_GROUP_ROLE (bad GROUP_ID)",
        """
        SELECT COUNT(*) AS cnt FROM GTP_USER_GROUP_ROLE ugr
        LEFT JOIN GTP_GROUP g ON ugr.GROUP_ID = g.GROUP_ID
        WHERE g.GROUP_ID IS NULL
        """,
        checks,
        errors,
    )
    _orphan_check(
        cur,
        "Orphan GTP_USER_GROUP_ROLE (bad ROLE_ID)",
        """
        SELECT COUNT(*) AS cnt FROM GTP_USER_GROUP_ROLE ugr
        LEFT JOIN GTP_ROLE r ON ugr.ROLE_ID = r.ROLE_ID
        WHERE r.ROLE_ID IS NULL
        """,
        checks,
        errors,
    )
    _orphan_check(
        cur,
        "Orphan GTP_ROLE_PERMISSION (bad PERMISSION_ID)",
        """
        SELECT COUNT(*) AS cnt FROM GTP_ROLE_PERMISSION rp
        LEFT JOIN GTP_PERMISSION p ON rp.PERMISSION_ID = p.PERMISSION_ID
        WHERE p.PERMISSION_ID IS NULL
        """,
        checks,
        errors,
    )
    _orphan_check(
        cur,
        "Orphan GTP_USER_ENTITY (bad ENTITY_ID)",
        """
        SELECT COUNT(*) AS cnt FROM GTP_USER_ENTITY ue
        LEFT JOIN GTP_ENTITY e ON ue.ENTITY_ID = e.ENTITY_ID
        WHERE e.ENTITY_ID IS NULL
        """,
        checks,
        errors,
    )
    _orphan_check(
        cur,
        "Users without any group/role assignment",
        """
        SELECT COUNT(*) AS cnt FROM GTP_USER u
        LEFT JOIN GTP_USER_GROUP_ROLE ugr ON u.USER_ID = ugr.USER_ID
        WHERE ugr.USER_ID IS NULL
        """,
        checks,
        errors,
        warn_only=True,
    )

    return ValidationResult(ok=len(errors) == 0, checks=checks, errors=errors)


def _count_check(
    cur: MySQLCursor,
    table: str,
    checks: list[dict],
    errors: list[str],
) -> None:
    try:
        cur.execute(f"SELECT COUNT(*) AS cnt FROM `{table}`")
        row = cur.fetchone()
        count = row["cnt"] if row else 0
        status = "PASS" if count > 0 else "WARN"
        checks.append({"check": f"{table} row count", "value": count, "status": status})
        if count == 0:
            errors.append(f"{table} is empty — no seed data loaded")
    except Exception as exc:
        checks.append({"check": f"{table} row count", "value": "ERROR", "status": "FAIL"})
        errors.append(f"{table}: {exc}")


def _orphan_check(
    cur: MySQLCursor,
    label: str,
    sql: str,
    checks: list[dict],
    errors: list[str],
    warn_only: bool = False,
) -> None:
    try:
        cur.execute(sql)
        row = cur.fetchone()
        count = row["cnt"] if row else 0
        if count == 0:
            checks.append({"check": label, "value": 0, "status": "PASS"})
        else:
            status = "WARN" if warn_only else "FAIL"
            checks.append({"check": label, "value": count, "status": status})
            if not warn_only:
                errors.append(f"{label}: {count} orphan rows")
    except Exception as exc:
        checks.append({"check": label, "value": "ERROR", "status": "FAIL"})
        errors.append(f"{label}: {exc}")
