"""Build Company and Entity rows with Faker."""

from __future__ import annotations

from faker import Faker

from testbed.domain.models import Company, Entity

_fake = Faker("en_GB")

_BANKS = [
    ("SGPAYGRP", "Singapore Payment Group Pte Ltd"),
    ("TRADESVC", "Trade Services Corporation Ltd"),
    ("COLLMGMT", "Collections Management Pte Ltd"),
]

_COUNTRIES = ["SG", "MY", "HK", "AU"]


def build_company(
    company_id: int,
    abbv_name: str | None = None,
    name: str | None = None,
) -> Company:
    idx = (company_id - 1) % len(_BANKS)
    abbv = abbv_name or f"{_BANKS[idx][0]}{company_id:02d}"
    full_name = name or f"{_BANKS[idx][1]} (#{company_id})"
    return Company(company_id=company_id, abbv_name=abbv, name=full_name)


def build_entity(
    entity_id: int | None,
    company: Company,
    entity_index: int = 1,
    country: str = "SG",
) -> Entity:
    abbv = f"{company.abbv_name}-E{entity_index:02d}"
    return Entity(
        entity_id=entity_id,
        name=f"{company.name} Entity {entity_index}",
        abbv_name=abbv,
        company_id=company.company_id,
        country=country,
        contact_email=_fake.company_email(),
        contact_person=_fake.name(),
        brch_code=f"BR{company.company_id:03d}{entity_index:02d}",
        subscription_code=f"SUB{company.company_id:03d}",
        dom="CORPORATE",
    )


def build_companies_and_entities(
    company_count: int,
    entities_per_company: int,
    start_company_id: int = 100,
    start_entity_id: int = 1000,
) -> tuple[list[Company], list[Entity]]:
    companies: list[Company] = []
    entities: list[Entity] = []
    e_id = start_entity_id
    for i in range(company_count):
        c = build_company(start_company_id + i)
        companies.append(c)
        for j in range(1, entities_per_company + 1):
            entities.append(build_entity(e_id, c, entity_index=j))
            e_id += 1
    return companies, entities
