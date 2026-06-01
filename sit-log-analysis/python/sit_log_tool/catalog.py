"""Load query catalog from YAML without external dependencies."""

from __future__ import annotations

import re
from pathlib import Path


def list_query_ids(catalog_path: Path) -> list[str]:
    text = catalog_path.read_text(encoding="utf-8")
    ids = re.findall(r"^\s+-\s+id:\s*(\S+)\s*$", text, flags=re.MULTILINE)
    return [qid.strip().rstrip("\r") for qid in ids]
