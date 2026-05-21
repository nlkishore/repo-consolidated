"""Truncate GTP testbed tables in FK-safe order and reset AUTO_INCREMENT."""

from __future__ import annotations

from mysql.connector.cursor import MySQLCursor

# Order: child tables before parent tables
_TRUNCATE_ORDER = [
    "GTP_USER_GROUP_ROLE",
    "GTP_USER_ENTITY",
    "GTP_ENTITY_ROLE",
    "GTP_COMPANY_ROLE",
    "GTP_ROLE_PERMISSION",
    "GTP_GROUP_ROLE",
    "GTP_USER",
    "GTP_GROUP",
    "GTP_ENTITY",
    "GTP_ROLE",
    "GTP_PERMISSION",
]

_AUTO_INC_TABLES = ["GTP_USER", "GTP_GROUP", "GTP_ENTITY", "GTP_ROLE", "GTP_PERMISSION"]


def reset_tables(cur: MySQLCursor) -> list[str]:
    """Truncate all testbed tables; return list of tables cleared."""
    cur.execute("SET FOREIGN_KEY_CHECKS = 0")
    cleared = []
    for table in _TRUNCATE_ORDER:
        try:
            cur.execute(f"TRUNCATE TABLE `{table}`")
            cleared.append(table)
        except Exception as exc:
            cleared.append(f"{table} (SKIP: {exc})")
    for table in _AUTO_INC_TABLES:
        try:
            cur.execute(f"ALTER TABLE `{table}` AUTO_INCREMENT = 1")
        except Exception:
            pass
    cur.execute("SET FOREIGN_KEY_CHECKS = 1")
    return cleared
