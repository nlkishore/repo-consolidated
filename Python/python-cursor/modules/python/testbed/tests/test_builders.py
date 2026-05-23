"""Unit tests for scenario builders (no DB required)."""

from __future__ import annotations

from testbed.builders.scenario_builder import (
    build_admin_scenario,
    build_collections_scenario,
    build_payments_scenario,
    build_trade_scenario,
    build_entity_user_scenario,
)
from testbed.domain.models import ScenarioData


def _assert_scenario(data: ScenarioData, name: str) -> None:
    assert data.scenario_name == name
    assert data.companies, f"{name}: no companies"
    assert data.entities, f"{name}: no entities"
    assert data.permissions, f"{name}: no permissions"
    assert data.roles, f"{name}: no roles"
    assert data.groups, f"{name}: no groups"
    assert data.users, f"{name}: no users"
    assert data.role_permissions, f"{name}: no role_permissions"
    assert data.group_roles, f"{name}: no group_roles"
    assert data.company_roles, f"{name}: no company_roles"
    assert data.entity_roles, f"{name}: no entity_roles"
    assert data.user_assignments, f"{name}: no user_assignments"
    assert data.user_entity_links, f"{name}: no user_entity_links"


def test_admin_scenario():
    data = build_admin_scenario(company_id=100)
    _assert_scenario(data, "admin")
    assert any(p.permission == "ADMIN_ACCESS" for p in data.permissions)
    assert any(r.rolename == "ADMIN" for r in data.roles)
    assert any(u.login_id.startswith("admin-") for u in data.users)


def test_payments_scenario():
    data = build_payments_scenario(company_id=101)
    _assert_scenario(data, "payments")
    assert any(p.permission == "INITIATE_PAYMENT" for p in data.permissions)
    assert any(r.rolename == "PAY_MAKER" for r in data.roles)
    assert any(r.rolename == "PAY_CHECKER" for r in data.roles)


def test_collections_scenario():
    data = build_collections_scenario(company_id=102)
    _assert_scenario(data, "collections")
    assert any(p.permission == "MANAGE_COLLECTION" for p in data.permissions)
    assert any(r.rolename == "COLL_OFFICER" for r in data.roles)


def test_trade_scenario():
    data = build_trade_scenario(company_id=103)
    _assert_scenario(data, "trade")
    assert any(p.permission == "INITIATE_TRADE" for p in data.permissions)
    assert any(r.rolename == "TRADE_OFFICER" for r in data.roles)
    assert any(r.rolename == "TRADE_APPROVER" for r in data.roles)


def test_entity_user_scenario():
    data = build_entity_user_scenario(company_id=104)
    _assert_scenario(data, "entity_user")
    assert any(r.rolename == "ENTITY_USER" for r in data.roles)


def test_no_entity_id_collision():
    scenarios = [
        build_admin_scenario(100),
        build_payments_scenario(101),
        build_collections_scenario(102),
        build_trade_scenario(103),
        build_entity_user_scenario(104),
    ]
    all_entity_ids = [
        e.entity_id
        for s in scenarios
        for e in s.entities
        if e.entity_id is not None
    ]
    assert len(all_entity_ids) == len(set(all_entity_ids)), "Entity ID collision across scenarios"


def test_role_permission_coverage():
    data = build_payments_scenario(101)
    role_ids = {r.role_id for r in data.roles}
    covered = {rp.role_id for rp in data.role_permissions}
    assert role_ids == covered, f"Roles without permissions: {role_ids - covered}"
