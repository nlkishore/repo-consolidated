from __future__ import annotations

from dataclasses import dataclass


@dataclass
class TextChunk:
    text: str
    chunk_index: int
    metadata: dict[str, str]


def chunk_text(
    text: str,
    *,
    chunk_size: int,
    chunk_overlap: int,
    base_metadata: dict[str, str],
) -> list[TextChunk]:
    text = text.strip()
    if not text:
        return []

    if len(text) <= chunk_size:
        return [TextChunk(text=text, chunk_index=0, metadata=dict(base_metadata))]

    chunks: list[TextChunk] = []
    start = 0
    idx = 0
    step = max(1, chunk_size - chunk_overlap)

    while start < len(text):
        end = min(start + chunk_size, len(text))
        piece = text[start:end].strip()
        if piece:
            meta = dict(base_metadata)
            meta["chunk_index"] = str(idx)
            chunks.append(TextChunk(text=piece, chunk_index=idx, metadata=meta))
            idx += 1
        if end >= len(text):
            break
        start += step

    return chunks
