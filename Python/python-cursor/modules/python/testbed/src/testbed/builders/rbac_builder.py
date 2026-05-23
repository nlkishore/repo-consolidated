"""Build RBAC graph: Permission → Role → Group + join tables."""

from __future__ import annotations

from testbed.domain.models import (
    CompanyRole,
    EntityRole,
    Group,
    GroupRole,
    Permission,
    Role,
    RolePermission,
    UserGroupRole,
)


def build_rbac_graph(
    permissions: list[Permission],
    roles: list[Role],
    groups: list[Group],
    role_to_permissions: dict[int, list[int]],
    group_to_roles: dict[int, list[int]],
    company_id: int | None = None,
    entity_ids: list[int] | None = None,
) -> tuple[
    list[RolePermission],
    list[GroupRole],
    list[CompanyRole],
    list[EntityRole],
]:
    role_permissions = [
        RolePermission(role_id=r_id, permission_id=p_id)
        for r_id, p_ids in role_to_permissions.items()
        for p_id in p_ids
    ]
    group_roles = [
        GroupRole(group_id=g_id, role_id=r_id)
        for g_id, r_ids in group_to_roles.items()
        for r_id in r_ids
    ]
    company_roles: list[CompanyRole] = []
    if company_id:
        for role in roles:
            if role.role_id is not None:
                company_roles.append(
                    CompanyRole(
                        company_id=company_id,
                        role_id=role.role_id,
                        role_description=role.rolename,
                    )
                )
    entity_roles: list[EntityRole] = []
    if entity_ids:
        for eid in entity_ids:
            for role in roles:
                if role.role_id is not None:
                    entity_roles.append(
                        EntityRole(
                            entity_id=eid,
                            role_id=role.role_id,
                            role_description=role.rolename,
                        )
                    )
    return role_permissions, group_roles, company_roles, entity_roles


def build_user_group_roles(
    user_login_ids: list[str],
    user_id_map: dict[str, int],
    group_id: int,
    role_ids: list[int],
) -> list[UserGroupRole]:
    ugrs = []
    for login_id in user_login_ids:
        uid = user_id_map.get(login_id)
        if uid is None:
            continue
        for rid in role_ids:
            ugrs.append(UserGroupRole(user_id=uid, group_id=group_id, role_id=rid))
    return ugrs
