"""HotFix branch metadata validation per HOTFIX_BRANCH_VALIDATION_DESIGN.md."""

__version__ = "1.0.0"

from hotfix_validation.file_lists_validator import FileListsValidator, ValidationResult
from hotfix_validation.paired_commit import PairedCommitChecker

__all__ = [
    "__version__",
    "FileListsValidator",
    "ValidationResult",
    "PairedCommitChecker",
]
