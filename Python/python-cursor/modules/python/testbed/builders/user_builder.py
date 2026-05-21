"""Build User rows for each persona."""

from __future__ import annotations

from faker import Faker

from testbed.domain.models import Company, User

_fake = Faker("en_GB")


def build_user(
    login_id: str,
    first_name: str,
    last_name: str,
    company: Company,
    default_password: str = "TestPass1!",
    email: str | None = None,
    reference: str = "",
) -> User:
    return User(
        user_id=None,
        login_id=login_id,
        password_value=default_password,
        first_name=first_name,
        last_name=last_name,
        email=email or f"{login_id}@{company.abbv_name.lower()}.example.com",
        company_id=company.company_id,
        company_abbv_name=company.abbv_name,
        actv_flag="Y",
        country="SG",
        dom="CORPORATE",
        time_zone="Asia/Singapore",
        language="en",
        reference=reference,
    )


def build_persona_users(
    prefix: str,
    personas: list[str],
    company: Company,
    default_password: str = "TestPass1!",
) -> list[User]:
    """Build one user per persona, e.g. prefix='pay', personas=['maker','checker']."""
    users = []
    for persona in personas:
        login_id = f"{prefix}-{persona}-c{company.company_id:02d}"
        fname = persona.capitalize()
        lname = f"{prefix.capitalize()}User"
        users.append(
            build_user(
                login_id=login_id,
                first_name=fname,
                last_name=lname,
                company=company,
                default_password=default_password,
                reference=f"{prefix.upper()}_{persona.upper()}",
            )
        )
    return users
