"""Load settings from YAML + environment overrides."""

from __future__ import annotations

import os
from pathlib import Path
from typing import Literal

import yaml
from pydantic import BaseModel, Field


class AlertSettings(BaseModel):
    price_drop_threshold_pct: float = -5.0


class DataSettings(BaseModel):
    max_age_hours: float = 24.0
    flex_project_dir: Path = Path("C:/Investment/IBKR-Flex-BuySell")
    report_path: Path = Path(
        "C:/Investment/IBKR-Flex-BuySell/reports/IBKR_BuySell_Since_2020.xlsx"
    )
    flex_python: str = "python"
    refresh_mode: Literal["from_downloads", "download"] = "from_downloads"


class ScheduleSettings(BaseModel):
    run_market_days_only: bool = True
    market_calendar: str = "NYSE"
    timezone: str = "America/New_York"


class WhatsAppSettings(BaseModel):
    provider: Literal["green_api"] = "green_api"
    id_instance: str = ""
    api_token: str = ""
    target_phone: str = ""


class NotifySettings(BaseModel):
    mode: Literal["digest"] = "digest"
    cooldown_hours: float = 24.0
    digest_max_symbols: int = 15


class LlmSettings(BaseModel):
    enabled: bool = False


class AppSettings(BaseModel):
    alert: AlertSettings = Field(default_factory=AlertSettings)
    data: DataSettings = Field(default_factory=DataSettings)
    schedule: ScheduleSettings = Field(default_factory=ScheduleSettings)
    whatsapp: WhatsAppSettings = Field(default_factory=WhatsAppSettings)
    notify: NotifySettings = Field(default_factory=NotifySettings)
    llm: LlmSettings = Field(default_factory=LlmSettings)

    @property
    def project_root(self) -> Path:
        return Path(__file__).resolve().parents[1]

    @property
    def data_dir(self) -> Path:
        d = self.project_root / "data"
        d.mkdir(parents=True, exist_ok=True)
        return d


def _apply_env_overrides(settings: AppSettings) -> AppSettings:
    wa = settings.whatsapp
    id_inst = os.environ.get("GREEN_API_ID_INSTANCE", wa.id_instance)
    token = os.environ.get("GREEN_API_TOKEN", wa.api_token)
    phone = os.environ.get("WHATSAPP_TARGET_PHONE", wa.target_phone)
    if id_inst or token or phone:
        settings.whatsapp = WhatsAppSettings(
            provider=wa.provider,
            id_instance=id_inst,
            api_token=token,
            target_phone=phone,
        )
    return settings


def load_settings(config_path: Path | None = None) -> AppSettings:
    root = Path(__file__).resolve().parents[1]
    path = config_path or (root / "config" / "settings.yaml")
    if not path.is_file():
        example = root / "config" / "settings.example.yaml"
        if example.is_file():
            path = example
        else:
            return _apply_env_overrides(AppSettings())

    raw = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    settings = AppSettings.model_validate(raw)
    return _apply_env_overrides(settings)
