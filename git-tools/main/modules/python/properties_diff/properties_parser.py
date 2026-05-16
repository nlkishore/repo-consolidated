"""Parse Java .properties files (JDK Properties.load semantics, simplified)."""

from __future__ import annotations

import re
from collections import OrderedDict
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Tuple

_UNICODE_ESCAPE = re.compile(r"\\u([0-9a-fA-F]{4})")


@dataclass
class ParseResult:
    """Parsed key/value pairs and non-fatal warnings."""

    properties: "OrderedDict[str, str]"
    warnings: List[str] = field(default_factory=list)
    source: Optional[str] = None


def load_properties(
    path: Path,
    *,
    encoding: str = "utf-8",
) -> ParseResult:
    """Load a .properties file from disk."""
    text = path.read_text(encoding=encoding)
    return parse_properties_text(text, source=str(path))


def parse_properties_text(text: str, *, source: Optional[str] = None) -> ParseResult:
    """Parse properties from a string."""
    logical_lines = _read_logical_lines(text)
    properties: OrderedDict[str, str] = OrderedDict()
    warnings: List[str] = []
    label = source or "<string>"

    for line_no, line in enumerate(logical_lines, start=1):
        stripped = line.strip()
        if not stripped:
            continue
        if stripped[0] in "#!":
            continue

        key_part, sep, value_part = _split_key_value(line)
        if sep is None:
            warnings.append(f"{label}:{line_no}: no key/value separator, skipped")
            continue

        key = _unescape(key_part.strip())
        value = _unescape(value_part.strip()) if value_part is not None else ""

        if key in properties:
            warnings.append(
                f"{label}:{line_no}: duplicate key '{key}' (last value wins)"
            )
        properties[key] = value

    return ParseResult(properties=properties, warnings=warnings, source=source)


def _read_logical_lines(text: str) -> List[str]:
    """Join physical lines ending with backslash (line continuation)."""
    physical = text.splitlines()
    logical: List[str] = []
    buf: Optional[str] = None

    for raw in physical:
        line = raw.rstrip("\r\n")
        if buf is None:
            buf = line
        else:
            buf += line

        if buf.endswith("\\"):
            buf = buf[:-1]
            continue

        logical.append(buf)
        buf = None

    if buf is not None:
        logical.append(buf)

    return logical


def _split_key_value(line: str) -> Tuple[str, Optional[str], Optional[str]]:
    """
  Split into key, separator char, value.
  Separator is first unescaped '=', ':', or whitespace run.
  """
    key_chars: List[str] = []
    i = 0
    n = len(line)
    escaped = False

    while i < n:
        ch = line[i]
        if escaped:
            key_chars.append(ch)
            escaped = False
            i += 1
            continue
        if ch == "\\":
            key_chars.append(ch)
            escaped = True
            i += 1
            continue
        if ch in "=:":
            key = "".join(key_chars)
            value = line[i + 1 :]
            return key, ch, value
        if ch in (" ", "\t", "\f"):
            key = "".join(key_chars)
            j = i
            while j < n and line[j] in (" ", "\t", "\f"):
                j += 1
            return key, " ", line[j:]
        key_chars.append(ch)
        i += 1

    return "".join(key_chars), None, None


def _unescape(value: str) -> str:
    """Unescape Java properties escape sequences."""
    out: List[str] = []
    i = 0
    n = len(value)

    while i < n:
        ch = value[i]
        if ch != "\\" or i + 1 >= n:
            out.append(ch)
            i += 1
            continue

        nxt = value[i + 1]
        if nxt == "u" and i + 5 < n:
            hex_digits = value[i + 2 : i + 6]
            try:
                out.append(chr(int(hex_digits, 16)))
            except ValueError:
                out.append("\\u")
                out.append(hex_digits)
            i += 6
            continue

        mapping = {"n": "\n", "r": "\r", "t": "\t", "f": "\f"}
        if nxt in mapping:
            out.append(mapping[nxt])
            i += 2
            continue

        out.append(nxt)
        i += 2

    result = "".join(out)
    return _UNICODE_ESCAPE.sub(lambda m: chr(int(m.group(1), 16)), result)


def properties_to_dict(result: ParseResult) -> Dict[str, str]:
    return dict(result.properties)
