from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

import yaml

PROJECT_ROOT = Path(__file__).resolve().parents[2]
CONFIG_DIR = PROJECT_ROOT / "config"
DATA_DIR = PROJECT_ROOT / "data"
CHROMA_DIR = DATA_DIR / "chroma"
SQLITE_PATH = DATA_DIR / "sqlite" / "index.db"


@dataclass(frozen=True)
class PathsConfig:
    include_roots: list[Path]
    exclude_globs: list[str]
    extensions: set[str]
    max_file_mb: int


@dataclass(frozen=True)
class RagConfig:
    ollama_base_url: str
    embed_model: str
    chat_model: str
    chunk_size: int
    chunk_overlap: int
    top_k: int
    excel_max_rows_per_chunk: int


def _load_yaml(path: Path) -> dict:
    with path.open(encoding="utf-8") as f:
        return yaml.safe_load(f) or {}


def load_paths_config(path: Path | None = None) -> PathsConfig:
    raw = _load_yaml(path or CONFIG_DIR / "paths.yaml")
    roots = [Path(p).resolve() for p in raw.get("include_roots", [])]
    missing = [str(p) for p in roots if not p.is_dir()]
    if missing:
        raise FileNotFoundError(f"Include roots not found: {', '.join(missing)}")

    exts = {e.lower() if e.startswith(".") else f".{e.lower()}" for e in raw.get("extensions", [])}
    return PathsConfig(
        include_roots=roots,
        exclude_globs=list(raw.get("exclude_globs", [])),
        extensions=exts,
        max_file_mb=int(raw.get("max_file_mb", 50)),
    )


def load_rag_config(path: Path | None = None) -> RagConfig:
    raw = _load_yaml(path or CONFIG_DIR / "rag.yaml")
    ollama = raw.get("ollama", {})
    chunking = raw.get("chunking", {})
    retrieval = raw.get("retrieval", {})
    excel = raw.get("excel", {})

    return RagConfig(
        ollama_base_url=os.getenv(
            "OLLAMA_BASE_URL", ollama.get("base_url", "http://localhost:11434")
        ).rstrip("/"),
        embed_model=os.getenv("OLLAMA_EMBED_MODEL", ollama.get("embed_model", "nomic-embed-text")),
        chat_model=os.getenv("OLLAMA_CHAT_MODEL", ollama.get("chat_model", "llama3.1:8b")),
        chunk_size=int(chunking.get("chunk_size", 1200)),
        chunk_overlap=int(chunking.get("chunk_overlap", 150)),
        top_k=int(retrieval.get("top_k", 8)),
        excel_max_rows_per_chunk=int(excel.get("max_rows_per_chunk", 80)),
    )
