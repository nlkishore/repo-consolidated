"""Production vs UAT Java .properties comparison."""

__version__ = "1.0.0"

from properties_diff.compare import (
    FileDiffResult,
    TreeDiffResult,
    compare_files,
    compare_trees,
)
from properties_diff.properties_parser import ParseResult, load_properties, parse_properties_text

__all__ = [
    "__version__",
    "ParseResult",
    "load_properties",
    "parse_properties_text",
    "FileDiffResult",
    "TreeDiffResult",
    "compare_files",
    "compare_trees",
]
