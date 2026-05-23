"""Pydantic configuration models — mirror config/settings.example.yaml."""

from __future__ import annotations

from typing import Literal

from pydantic import BaseModel, Field


class DatabaseSettings(BaseModel):
    host: str = "localhost"
    port: int = 3306
    name: str = Field(..., description="Database/schema name")
    username: str = Field(..., description="DB user")
    password: str = Field(default="", description="DB password")
    connect_timeout: int = 10


class SeedSettings(BaseModel):
    reset_before_seed: bool = False
    idempotent: bool = True
    company_count: int = 3
    entities_per_company: int = 2
    users_per_scenario: int = 2


class SecuritySettings(BaseModel):
    password_hasher: Literal["sha256", "plain"] = "sha256"
    default_password: str = "TestPass1!"


class ReportSettings(BaseModel):
    output_dir: str = "testbed-reports"


class Settings(BaseModel):
    database: DatabaseSettings
    scenarios: list[str] = Field(
        default_factory=lambda: ["admin", "payments", "collections", "trade", "entity_user"]
    )
    seed: SeedSettings = Field(default_factory=SeedSettings)
    security: SecuritySettings = Field(default_factory=SecuritySettings)
    reports: ReportSettings = Field(default_factory=ReportSettings)
