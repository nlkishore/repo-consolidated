"""Load settings from YAML + environment overrides."""

from __future__ import annotations

import configparser
import os
from pathlib import Path
from typing import Literal

import yaml
from pydantic import BaseModel, Field

# Shared Green API credentials used by AlertApp / AutomatedTrading (Investment folder)
_INVESTMENT_ROOT = Path(__file__).resolve().parents[2]
_WHATSAPP_INI_SOURCES = (
    _INVESTMENT_ROOT / "AutomatedTrading" / "config.ini",
    _INVESTMENT_ROOT / "AlertApp" / "config.ini",
    Path(__file__).resolve().parents[1] / "config" / "whatsapp.ini",
)


class AlertSettings(BaseModel):
    """notify_all_positions: send every completely sold row with prices (no -5% filter)."""
    notify_all_positions: bool = True
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


def _load_whatsapp_from_investment_configs() -> tuple[str, str, str]:
    """Reuse Green API credentials from existing Investment alert apps."""
    id_inst = ""
    token = ""
    phone = ""

    for ini_path in _WHATSAPP_INI_SOURCES:
        if not ini_path.is_file():
            continue
        parser = configparser.ConfigParser()
        parser.read(ini_path, encoding="utf-8")
        for section_name in ("trading", "whatsapp", "green_api"):
            if not parser.has_section(section_name):
                continue
            section = parser[section_name]
            id_inst = id_inst or section.get("whatsapp_id_instance", section.get("id_instance", "")).strip()
            token = token or section.get(
                "whatsapp_api_token_instance",
                section.get("api_token_instance", section.get("api_token", "")),
            ).strip()
            phone = phone or section.get(
                "whatsapp_target_phone",
                section.get("target_phone", ""),
            ).strip()
        if id_inst and token and phone:
            break

    return id_inst, token, phone


def _apply_env_overrides(settings: AppSettings) -> AppSettings:
    wa = settings.whatsapp
    id_inst = os.environ.get("GREEN_API_ID_INSTANCE", wa.id_instance).strip()
    token = os.environ.get("GREEN_API_TOKEN", wa.api_token).strip()
    phone = os.environ.get("WHATSAPP_TARGET_PHONE", wa.target_phone).strip()

    if not (id_inst and token and phone):
        ini_id, ini_token, ini_phone = _load_whatsapp_from_investment_configs()
        id_inst = id_inst or ini_id
        token = token or ini_token
        phone = phone or ini_phone

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
