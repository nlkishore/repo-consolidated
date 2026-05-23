"""Load settings.yaml from project root."""

from __future__ import annotations

from pathlib import Path

import yaml

from geb_testbed.config.schema import Settings

PROJECT_ROOT = Path(__file__).resolve().parents[3]


def load_settings(config_path: str | Path | None = None) -> Settings:
    path = Path(config_path) if config_path else PROJECT_ROOT / "config" / "settings.yaml"
    if not path.is_file():
        path = PROJECT_ROOT / "config" / "settings.example.yaml"
    if not path.is_file():
        raise FileNotFoundError(f"Config not found: {path}")
    data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    return Settings.model_validate(data)


def project_root() -> Path:
    return PROJECT_ROOT
