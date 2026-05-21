"""Domain dataclasses aligned to GEB/GTP MySQL schema.

Column names mirror the actual table columns in:
  - GTP_USER, GTP_ROLE, GTP_PERMISSION, GTP_GROUP
  - GTP_ENTITY, GTP_COMPANY_ROLE, GTP_ENTITY_ROLE
  - GTP_USER_ENTITY, GTP_USER_GROUP_ROLE, GTP_ROLE_PERMISSION, GTP_GROUP_ROLE
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Company:
    """Virtual — no standalone GTP_COMPANY table.
    Represented by COMPANY_ID + COMPANY_ABBV_NAME on GTP_USER and GTP_ENTITY."""
    company_id: int
    abbv_name: str
    name: str = ""


@dataclass
class Entity:
    """Maps to GTP_ENTITY."""
    entity_id: Optional[int]
    name: str
    abbv_name: str
    company_id: int
    country: str = "SG"
    contact_email: str = ""
    contact_person: str = ""
    brch_code: str = ""
    subscription_code: str = ""
    dom: str = "CORPORATE"


@dataclass
class Permission:
    """Maps to GTP_PERMISSION."""
    permission_id: Optional[int]
    permission: str          # GTP_PERMISSION.PERMISSION column


@dataclass
class Role:
    """Maps to GTP_ROLE."""
    role_id: Optional[int]
    rolename: str
    roletype: str = "FUNCTIONAL"
    owner_id: Optional[int] = None
    roleassigner: str = ""
    roledest: str = ""


@dataclass
class Group:
    """Maps to GTP_GROUP."""
    group_id: Optional[int]
    groupname: str
    company_id: Optional[int] = None


@dataclass
class User:
    """Maps to GTP_USER (extended GEB version)."""
    user_id: Optional[int]
    login_id: str
    password_value: str
    first_name: str
    last_name: str
    email: str
    company_id: Optional[int] = None
    company_abbv_name: str = ""
    actv_flag: str = "Y"
    country: str = "SG"
    dom: str = "CORPORATE"
    phone: str = ""
    fax: str = ""
    time_zone: str = "Asia/Singapore"
    language: str = "en"
    reference: str = ""


@dataclass
class RolePermission:
    """Maps to GTP_ROLE_PERMISSION."""
    role_id: int
    permission_id: int


@dataclass
class GroupRole:
    """Maps to GTP_GROUP_ROLE."""
    group_id: int
    role_id: int


@dataclass
class UserGroupRole:
    """Maps to GTP_USER_GROUP_ROLE."""
    user_id: int
    group_id: int
    role_id: int


@dataclass
class UserEntity:
    """Maps to GTP_USER_ENTITY."""
    user_id: int
    entity_id: int
    default_entity: str = "Y"
    abbv_name: str = ""
    user_abbv_name: str = ""


@dataclass
class CompanyRole:
    """Maps to GTP_COMPANY_ROLE."""
    company_id: int
    role_id: int
    role_description: str = ""


@dataclass
class EntityRole:
    """Maps to GTP_ENTITY_ROLE."""
    entity_id: int
    role_id: int
    role_description: str = ""


@dataclass
class ScenarioData:
    """Full seed graph for one scenario."""
    scenario_name: str
    companies: list[Company] = field(default_factory=list)
    entities: list[Entity] = field(default_factory=list)
    permissions: list[Permission] = field(default_factory=list)
    roles: list[Role] = field(default_factory=list)
    groups: list[Group] = field(default_factory=list)
    users: list[User] = field(default_factory=list)
    role_permissions: list[RolePermission] = field(default_factory=list)
    group_roles: list[GroupRole] = field(default_factory=list)
    user_group_roles: list[UserGroupRole] = field(default_factory=list)
    user_entities: list[UserEntity] = field(default_factory=list)
    company_roles: list[CompanyRole] = field(default_factory=list)
    entity_roles: list[EntityRole] = field(default_factory=list)
