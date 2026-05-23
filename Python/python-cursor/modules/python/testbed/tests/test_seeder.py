"""Tests for seeder helpers (no live DB — logic only)."""

from __future__ import annotations

import hashlib

from testbed.db.seeder import _hash_password


def test_sha256_hash():
    result = _hash_password("TestPass1!", "sha256")
    expected = hashlib.sha256("TestPass1!".encode("utf-8")).hexdigest()
    assert result == expected


def test_plain_hash():
    result = _hash_password("TestPass1!", "plain")
    assert result == "TestPass1!"
