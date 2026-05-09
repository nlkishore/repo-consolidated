"""Default paths and tuning for HotFix validation."""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Tuple


@dataclass(frozen=True)
class HotfixPaths:
    """Tracked HotFix metadata paths relative to repository root."""

    build_script: str = "buildScripts.sh"
    file_list: str = "fileLists.txt"
    file_list_globs: Tuple[str, ...] = ("fileLists.txt", "fileLists.yaml")


@dataclass
class PathRules:
    """Optional path hygiene rules applied after YAML schema passes."""

    forbid_parent_segments: bool = True
    allowed_prefixes: Tuple[str, ...] = field(default_factory=tuple)
    normalize_separators: bool = True


@dataclass
class SkipBypassRules:
    """Commit message bypass for paired-file policy (use sparingly)."""

    token: str = "[skip-hotfix-pair]"


DEFAULT_PATHS = HotfixPaths()
DEFAULT_PATH_RULES = PathRules()
DEFAULT_BYPASS = SkipBypassRules()
