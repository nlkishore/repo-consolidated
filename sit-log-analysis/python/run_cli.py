#!/usr/bin/env python3
"""Entry point for Git Bash / Windows (avoids PYTHONPATH issues)."""

from __future__ import annotations

import sys
from pathlib import Path

_ROOT = Path(__file__).resolve().parent
if str(_ROOT) not in sys.path:
    sys.path.insert(0, str(_ROOT))

from sit_log_tool.__main__ import main

if __name__ == "__main__":
    raise SystemExit(main())
