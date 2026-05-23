"""Tests for config loader."""

from __future__ import annotations

import tempfile
from pathlib import Path

import pytest

from testbed.config.loader import load_settings


def test_load_settings_basic():
    yaml_text = """
database:
  host: localhost
  name: testdb
  username: testuser
  password: testpass
scenarios:
  - admin
"""
    with tempfile.NamedTemporaryFile(suffix=".yaml", mode="w", delete=False, encoding="utf-8") as f:
        f.write(yaml_text)
        path = f.name
    settings = load_settings(path)
    assert settings.database.host == "localhost"
    assert settings.database.name == "testdb"
    assert "admin" in settings.scenarios


def test_load_settings_env_substitution(monkeypatch):
    monkeypatch.setenv("TEST_DB_NAME", "mydb")
    yaml_text = """
database:
  host: localhost
  name: "${TEST_DB_NAME}"
  username: user
  password: pass
"""
    with tempfile.NamedTemporaryFile(suffix=".yaml", mode="w", delete=False, encoding="utf-8") as f:
        f.write(yaml_text)
        path = f.name
    settings = load_settings(path)
    assert settings.database.name == "mydb"


def test_load_settings_missing_file():
    with pytest.raises(FileNotFoundError):
        load_settings("/nonexistent/settings.yaml")
