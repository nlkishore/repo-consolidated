"""Compose full feature-aligned scenarios.

Each build_* function returns a ScenarioData ready for seeder.seed_scenario().
ID ranges are allocated to avoid collisions across scenarios:

  Admin        perm 1-5,   role 1-5,   group 1-3,   users  u01-u02, entities e1001-e1002
  Payments     perm 11-15, role 11-15, group 11-13,  users  u11-u12, entities e1011-e1012
  Collections  perm 21-25, role 21-25, group 21-23,  users  u21-u22, entities e1021-e1022
  Trade        perm 31-35, role 31-35, group 31-33,  users  u31-u32, entities e1031-e1032
  EntityUser   perm 41-43, role 41-43, group 41,     users  u41,     entities e1041
"""

from __future__ import annotations

from testbed.builders.company_builder import build_company, build_entity
from testbed.builders.rbac_builder import build_rbac_graph
from testbed.builders.user_builder import build_persona_users
from testbed.config.schema import SeedSettings, SecuritySettings
from testbed.domain.models import (
    Group,
    Permission,
    Role,
    ScenarioData,
)

_DEFAULT_COMPANY_ID = 100


# ---------------------------------------------------------------------------
# Admin scenario
# ---------------------------------------------------------------------------

def build_admin_scenario(
    company_id: int = _DEFAULT_COMPANY_ID,
    seed: SeedSettings | None = None,
    security: SecuritySettings | None = None,
) -> ScenarioData:
    seed = seed or SeedSettings()
    security = security or SecuritySettings()

    company = build_company(company_id)
    entities = [
        build_entity(1001, company, entity_index=1),
        build_entity(1002, company, entity_index=2),
    ]

    permissions = [
        Permission(permission_id=1, permission="ADMIN_ACCESS"),
        Permission(permission_id=2, permission="MANAGE_USERS"),
        Permission(permission_id=3, permission="VIEW_DASHBOARD"),
        Permission(permission_id=4, permission="SYSTEM_CONFIG"),
        Permission(permission_id=5, permission="AUDIT_LOG"),
    ]
    roles = [
        Role(role_id=1, rolename="ADMIN", roletype="SYSTEM"),
        Role(role_id=2, rolename="SUPER_USER", roletype="FUNCTIONAL"),
        Role(role_id=3, rolename="AUDITOR", roletype="FUNCTIONAL"),
    ]
    groups = [
        Group(group_id=1, groupname="ADMINISTRATORS", company_id=company_id),
        Group(group_id=2, groupname="SUPER_USERS", company_id=company_id),
        Group(group_id=3, groupname="AUDITORS", company_id=company_id),
    ]
    users = build_persona_users(
        "admin", ["sysadmin", "superuser"], company, security.default_password
    )
    auditor_users = build_persona_users("audit", ["officer"], company, security.default_password)
    all_users = users + auditor_users

    role_to_perms = {
        1: [1, 2, 3, 4, 5],  # ADMIN -> all
        2: [2, 3, 4],          # SUPER_USER
        3: [3, 5],             # AUDITOR -> VIEW_DASHBOARD, AUDIT_LOG
    }
    group_to_roles = {
        1: [1],
        2: [2],
        3: [3],
    }
    entity_ids = [e.entity_id for e in entities if e.entity_id is not None]
    rp, gr, cr, er = build_rbac_graph(
        permissions, roles, groups, role_to_perms, group_to_roles, company_id, entity_ids
    )

    return ScenarioData(
        scenario_name="admin",
        companies=[company],
        entities=entities,
        permissions=permissions,
        roles=roles,
        groups=groups,
        users=all_users,
        role_permissions=rp,
        group_roles=gr,
        company_roles=cr,
        entity_roles=er,
    )


# ---------------------------------------------------------------------------
# Payments scenario
# ---------------------------------------------------------------------------

def build_payments_scenario(
    company_id: int = _DEFAULT_COMPANY_ID,
    seed: SeedSettings | None = None,
    security: SecuritySettings | None = None,
) -> ScenarioData:
    seed = seed or SeedSettings()
    security = security or SecuritySettings()

    company = build_company(company_id)
    entities = [
        build_entity(1011, company, entity_index=1),
        build_entity(1012, company, entity_index=2),
    ]

    permissions = [
        Permission(permission_id=11, permission="INITIATE_PAYMENT"),
        Permission(permission_id=12, permission="APPROVE_PAYMENT"),
        Permission(permission_id=13, permission="VIEW_PAYMENT"),
        Permission(permission_id=14, permission="REJECT_PAYMENT"),
        Permission(permission_id=15, permission="AMEND_PAYMENT"),
    ]
    roles = [
        Role(role_id=11, rolename="PAY_MAKER", roletype="FUNCTIONAL"),
        Role(role_id=12, rolename="PAY_CHECKER", roletype="FUNCTIONAL"),
        Role(role_id=13, rolename="PAY_VIEWER", roletype="FUNCTIONAL"),
    ]
    groups = [
        Group(group_id=11, groupname="PAYMENT_MAKERS", company_id=company_id),
        Group(group_id=12, groupname="PAYMENT_CHECKERS", company_id=company_id),
        Group(group_id=13, groupname="PAYMENT_VIEWERS", company_id=company_id),
    ]
    users = build_persona_users(
        "pay", ["maker", "checker", "viewer"], company, security.default_password
    )

    role_to_perms = {
        11: [11, 13, 15],       # PAY_MAKER: initiate, view, amend
        12: [12, 13, 14],       # PAY_CHECKER: approve, view, reject
        13: [13],               # PAY_VIEWER: view only
    }
    group_to_roles = {11: [11], 12: [12], 13: [13]}
    entity_ids = [e.entity_id for e in entities if e.entity_id is not None]
    rp, gr, cr, er = build_rbac_graph(
        permissions, roles, groups, role_to_perms, group_to_roles, company_id, entity_ids
    )

    return ScenarioData(
        scenario_name="payments",
        companies=[company],
        entities=entities,
        permissions=permissions,
        roles=roles,
        groups=groups,
        users=users,
        role_permissions=rp,
        group_roles=gr,
        company_roles=cr,
        entity_roles=er,
    )


# ---------------------------------------------------------------------------
# Collections scenario
# ---------------------------------------------------------------------------

def build_collections_scenario(
    company_id: int = _DEFAULT_COMPANY_ID,
    seed: SeedSettings | None = None,
    security: SecuritySettings | None = None,
) -> ScenarioData:
    seed = seed or SeedSettings()
    security = security or SecuritySettings()

    company = build_company(company_id)
    entities = [
        build_entity(1021, company, entity_index=1),
        build_entity(1022, company, entity_index=2),
    ]

    permissions = [
        Permission(permission_id=21, permission="MANAGE_COLLECTION"),
        Permission(permission_id=22, permission="VIEW_COLLECTION"),
        Permission(permission_id=23, permission="APPROVE_COLLECTION"),
        Permission(permission_id=24, permission="REJECT_COLLECTION"),
    ]
    roles = [
        Role(role_id=21, rolename="COLL_OFFICER", roletype="FUNCTIONAL"),
        Role(role_id=22, rolename="COLL_VIEWER", roletype="FUNCTIONAL"),
        Role(role_id=23, rolename="COLL_APPROVER", roletype="FUNCTIONAL"),
    ]
    groups = [
        Group(group_id=21, groupname="COLLECTIONS_OFFICERS", company_id=company_id),
        Group(group_id=22, groupname="COLLECTIONS_VIEWERS", company_id=company_id),
        Group(group_id=23, groupname="COLLECTIONS_APPROVERS", company_id=company_id),
    ]
    users = build_persona_users(
        "coll", ["officer", "viewer", "approver"], company, security.default_password
    )

    role_to_perms = {
        21: [21, 22],           # COLL_OFFICER
        22: [22],               # COLL_VIEWER
        23: [22, 23, 24],       # COLL_APPROVER
    }
    group_to_roles = {21: [21], 22: [22], 23: [23]}
    entity_ids = [e.entity_id for e in entities if e.entity_id is not None]
    rp, gr, cr, er = build_rbac_graph(
        permissions, roles, groups, role_to_perms, group_to_roles, company_id, entity_ids
    )

    return ScenarioData(
        scenario_name="collections",
        companies=[company],
        entities=entities,
        permissions=permissions,
        roles=roles,
        groups=groups,
        users=users,
        role_permissions=rp,
        group_roles=gr,
        company_roles=cr,
        entity_roles=er,
    )


# ---------------------------------------------------------------------------
# Trade scenario
# ---------------------------------------------------------------------------

def build_trade_scenario(
    company_id: int = _DEFAULT_COMPANY_ID,
    seed: SeedSettings | None = None,
    security: SecuritySettings | None = None,
) -> ScenarioData:
    seed = seed or SeedSettings()
    security = security or SecuritySettings()

    company = build_company(company_id)
    entities = [
        build_entity(1031, company, entity_index=1),
        build_entity(1032, company, entity_index=2),
    ]

    permissions = [
        Permission(permission_id=31, permission="INITIATE_TRADE"),
        Permission(permission_id=32, permission="APPROVE_TRADE"),
        Permission(permission_id=33, permission="VIEW_TRADE"),
        Permission(permission_id=34, permission="AMEND_TRADE"),
        Permission(permission_id=35, permission="REJECT_TRADE"),
    ]
    roles = [
        Role(role_id=31, rolename="TRADE_OFFICER", roletype="FUNCTIONAL"),
        Role(role_id=32, rolename="TRADE_APPROVER", roletype="FUNCTIONAL"),
        Role(role_id=33, rolename="TRADE_VIEWER", roletype="FUNCTIONAL"),
    ]
    groups = [
        Group(group_id=31, groupname="TRADE_OFFICERS", company_id=company_id),
        Group(group_id=32, groupname="TRADE_APPROVERS", company_id=company_id),
        Group(group_id=33, groupname="TRADE_VIEWERS", company_id=company_id),
    ]
    users = build_persona_users(
        "trade", ["officer", "approver", "viewer"], company, security.default_password
    )

    role_to_perms = {
        31: [31, 33, 34],       # TRADE_OFFICER
        32: [32, 33, 35],       # TRADE_APPROVER
        33: [33],               # TRADE_VIEWER
    }
    group_to_roles = {31: [31], 32: [32], 33: [33]}
    entity_ids = [e.entity_id for e in entities if e.entity_id is not None]
    rp, gr, cr, er = build_rbac_graph(
        permissions, roles, groups, role_to_perms, group_to_roles, company_id, entity_ids
    )

    return ScenarioData(
        scenario_name="trade",
        companies=[company],
        entities=entities,
        permissions=permissions,
        roles=roles,
        groups=groups,
        users=users,
        role_permissions=rp,
        group_roles=gr,
        company_roles=cr,
        entity_roles=er,
    )


# ---------------------------------------------------------------------------
# Entity User scenario (read-only, single entity link)
# ---------------------------------------------------------------------------

def build_entity_user_scenario(
    company_id: int = _DEFAULT_COMPANY_ID,
    seed: SeedSettings | None = None,
    security: SecuritySettings | None = None,
) -> ScenarioData:
    seed = seed or SeedSettings()
    security = security or SecuritySettings()

    company = build_company(company_id)
    entities = [build_entity(1041, company, entity_index=1)]

    permissions = [
        Permission(permission_id=41, permission="VIEW_DASHBOARD"),
        Permission(permission_id=42, permission="VIEW_ACCOUNT"),
        Permission(permission_id=43, permission="VIEW_STATEMENT"),
    ]
    roles = [
        Role(role_id=41, rolename="ENTITY_USER", roletype="FUNCTIONAL"),
        Role(role_id=42, rolename="ENTITY_VIEWER", roletype="FUNCTIONAL"),
    ]
    groups = [
        Group(group_id=41, groupname="ENTITY_USERS", company_id=company_id),
    ]
    users = build_persona_users("ent", ["user"], company, security.default_password)

    role_to_perms = {
        41: [41, 42, 43],
        42: [41],
    }
    group_to_roles = {41: [41, 42]}
    entity_ids = [e.entity_id for e in entities if e.entity_id is not None]
    rp, gr, cr, er = build_rbac_graph(
        permissions, roles, groups, role_to_perms, group_to_roles, company_id, entity_ids
    )

    return ScenarioData(
        scenario_name="entity_user",
        companies=[company],
        entities=entities,
        permissions=permissions,
        roles=roles,
        groups=groups,
        users=users,
        role_permissions=rp,
        group_roles=gr,
        company_roles=cr,
        entity_roles=er,
    )


SCENARIO_BUILDERS = {
    "admin": build_admin_scenario,
    "payments": build_payments_scenario,
    "collections": build_collections_scenario,
    "trade": build_trade_scenario,
    "entity_user": build_entity_user_scenario,
}
