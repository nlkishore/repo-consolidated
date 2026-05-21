"""Idempotent INSERT helpers for all GTP tables."""

from __future__ import annotations

import hashlib
from datetime import datetime

from mysql.connector.cursor import MySQLCursor

from testbed.config.schema import SecuritySettings
from testbed.domain.models import (
    CompanyRole,
    Entity,
    EntityRole,
    Group,
    GroupRole,
    Permission,
    Role,
    RolePermission,
    ScenarioData,
    User,
    UserEntity,
    UserGroupRole,
)


def _hash_password(password: str, hasher: str) -> str:
    if hasher == "sha256":
        return hashlib.sha256(password.encode("utf-8")).hexdigest()
    return password


def seed_scenario(
    cur: MySQLCursor,
    data: ScenarioData,
    security: SecuritySettings,
) -> dict[str, int]:
    """Seed one scenario graph; return inserted/updated counts per table."""
    counts: dict[str, int] = {}
    counts["permissions"] = _seed_permissions(cur, data.permissions)
    counts["roles"] = _seed_roles(cur, data.roles)
    counts["groups"] = _seed_groups(cur, data.groups)
    counts["entities"] = _seed_entities(cur, data.entities)
    counts["users"] = _seed_users(cur, data.users, security)
    counts["role_permissions"] = _seed_role_permissions(cur, data.role_permissions)
    counts["group_roles"] = _seed_group_roles(cur, data.group_roles)
    counts["user_group_roles"] = _seed_user_group_roles(cur, data.user_group_roles)
    counts["user_entities"] = _seed_user_entities(cur, data.user_entities)
    counts["company_roles"] = _seed_company_roles(cur, data.company_roles)
    counts["entity_roles"] = _seed_entity_roles(cur, data.entity_roles)
    return counts


def _seed_permissions(cur: MySQLCursor, permissions: list[Permission]) -> int:
    count = 0
    for p in permissions:
        cur.execute(
            """
            INSERT INTO GTP_PERMISSION (PERMISSION_ID, PERMISSION)
            VALUES (%s, %s)
            ON DUPLICATE KEY UPDATE PERMISSION = VALUES(PERMISSION)
            """,
            (p.permission_id, p.permission),
        )
        count += 1
    return count


def _seed_roles(cur: MySQLCursor, roles: list[Role]) -> int:
    count = 0
    for r in roles:
        cur.execute(
            """
            INSERT INTO GTP_ROLE (ROLE_ID, ROLENAME, ROLETYPE, ROLEASSIGNER, ROLEDEST)
            VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
              ROLENAME = VALUES(ROLENAME),
              ROLETYPE = VALUES(ROLETYPE)
            """,
            (r.role_id, r.rolename, r.roletype, r.roleassigner, r.roledest),
        )
        count += 1
    return count


def _seed_groups(cur: MySQLCursor, groups: list[Group]) -> int:
    count = 0
    for g in groups:
        cur.execute(
            """
            INSERT INTO GTP_GROUP (GROUP_ID, GROUPNAME, COMPANY_ID)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE
              GROUPNAME = VALUES(GROUPNAME),
              COMPANY_ID = VALUES(COMPANY_ID)
            """,
            (g.group_id, g.groupname, g.company_id),
        )
        count += 1
    return count


def _seed_entities(cur: MySQLCursor, entities: list[Entity]) -> int:
    count = 0
    for e in entities:
        cur.execute(
            """
            INSERT INTO GTP_ENTITY (
                ENTITY_ID, NAME, ABBV_NAME, COMPANY_ID, COUNTRY,
                CONTACT_EMAIL, CONTACT_PERSON, BRCH_CODE, SUBSCRIPTION_CODE, DOM
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
              NAME = VALUES(NAME),
              ABBV_NAME = VALUES(ABBV_NAME),
              COMPANY_ID = VALUES(COMPANY_ID)
            """,
            (
                e.entity_id, e.name, e.abbv_name, e.company_id, e.country,
                e.contact_email, e.contact_person, e.brch_code,
                e.subscription_code, e.dom,
            ),
        )
        count += 1
    return count


def _seed_users(
    cur: MySQLCursor, users: list[User], security: SecuritySettings
) -> int:
    count = 0
    now = datetime.utcnow()
    for u in users:
        hashed = _hash_password(u.password_value, security.password_hasher)
        cur.execute(
            """
            INSERT INTO GTP_USER (
                LOGIN_ID, PASSWORD_VALUE, FIRST_NAME, LAST_NAME, EMAIL,
                COMPANY_ID, COMPANY_ABBV_NAME, ACTV_FLAG, COUNTRY, DOM,
                PHONE, FAX, TIME_ZONE, LANGUAGE, REFERENCE, CREATED
            ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
              FIRST_NAME = VALUES(FIRST_NAME),
              LAST_NAME = VALUES(LAST_NAME),
              EMAIL = VALUES(EMAIL),
              ACTV_FLAG = VALUES(ACTV_FLAG),
              COMPANY_ID = VALUES(COMPANY_ID)
            """,
            (
                u.login_id, hashed, u.first_name, u.last_name, u.email,
                u.company_id, u.company_abbv_name, u.actv_flag, u.country, u.dom,
                u.phone, u.fax, u.time_zone, u.language, u.reference, now,
            ),
        )
        count += 1
    return count


def _seed_role_permissions(cur: MySQLCursor, rps: list[RolePermission]) -> int:
    count = 0
    for rp in rps:
        cur.execute(
            """
            INSERT IGNORE INTO GTP_ROLE_PERMISSION (ROLE_ID, PERMISSION_ID)
            VALUES (%s, %s)
            """,
            (rp.role_id, rp.permission_id),
        )
        count += 1
    return count


def _seed_group_roles(cur: MySQLCursor, grs: list[GroupRole]) -> int:
    count = 0
    for gr in grs:
        cur.execute(
            """
            INSERT IGNORE INTO GTP_GROUP_ROLE (GROUP_ID, ROLE_ID)
            VALUES (%s, %s)
            """,
            (gr.group_id, gr.role_id),
        )
        count += 1
    return count


def _seed_user_group_roles(cur: MySQLCursor, ugrs: list[UserGroupRole]) -> int:
    count = 0
    for ugr in ugrs:
        cur.execute(
            """
            INSERT IGNORE INTO GTP_USER_GROUP_ROLE (USER_ID, GROUP_ID, ROLE_ID)
            VALUES (%s, %s, %s)
            """,
            (ugr.user_id, ugr.group_id, ugr.role_id),
        )
        count += 1
    return count


def _seed_user_entities(cur: MySQLCursor, ues: list[UserEntity]) -> int:
    count = 0
    for ue in ues:
        cur.execute(
            """
            INSERT INTO GTP_USER_ENTITY (
                USER_ID, ENTITY_ID, DEFAULT_ENTITY, ABBV_NAME, USER_ABBV_NAME
            ) VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
              DEFAULT_ENTITY = VALUES(DEFAULT_ENTITY)
            """,
            (ue.user_id, ue.entity_id, ue.default_entity, ue.abbv_name, ue.user_abbv_name),
        )
        count += 1
    return count


def _seed_company_roles(cur: MySQLCursor, crs: list[CompanyRole]) -> int:
    count = 0
    for cr in crs:
        cur.execute(
            """
            INSERT INTO GTP_COMPANY_ROLE (COMPANY_ID, ROLE_ID, ROLE_DESCRIPTION)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE ROLE_DESCRIPTION = VALUES(ROLE_DESCRIPTION)
            """,
            (cr.company_id, cr.role_id, cr.role_description),
        )
        count += 1
    return count


def _seed_entity_roles(cur: MySQLCursor, ers: list[EntityRole]) -> int:
    count = 0
    for er in ers:
        cur.execute(
            """
            INSERT INTO GTP_ENTITY_ROLE (ENTITY_ID, ROLE_ID, ROLE_DESCRIPTION)
            VALUES (%s, %s, %s)
            ON DUPLICATE KEY UPDATE ROLE_DESCRIPTION = VALUES(ROLE_DESCRIPTION)
            """,
            (er.entity_id, er.role_id, er.role_description),
        )
        count += 1
    return count


def resolve_user_ids(
    cur: MySQLCursor, users: list[User]
) -> dict[str, int]:
    """Fetch DB-assigned USER_IDs by LOGIN_ID after insert."""
    if not users:
        return {}
    login_ids = [u.login_id for u in users]
    placeholders = ",".join(["%s"] * len(login_ids))
    cur.execute(
        f"SELECT USER_ID, LOGIN_ID FROM GTP_USER WHERE LOGIN_ID IN ({placeholders})",
        login_ids,
    )
    return {row["LOGIN_ID"]: row["USER_ID"] for row in cur.fetchall()}


def resolve_entity_ids(
    cur: MySQLCursor, entities: list[Entity]
) -> dict[str, int]:
    """Fetch DB-assigned ENTITY_IDs by ABBV_NAME after insert."""
    if not entities:
        return {}
    names = [e.abbv_name for e in entities]
    placeholders = ",".join(["%s"] * len(names))
    cur.execute(
        f"SELECT ENTITY_ID, ABBV_NAME FROM GTP_ENTITY WHERE ABBV_NAME IN ({placeholders})",
        names,
    )
    return {row["ABBV_NAME"]: row["ENTITY_ID"] for row in cur.fetchall()}
