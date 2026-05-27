from __future__ import annotations

import structlog
from tqdm import tqdm

from local_rag.chunker import chunk_text
from local_rag.config import CHROMA_DIR, SQLITE_PATH, PathsConfig, RagConfig, load_paths_config, load_rag_config
from local_rag.embedder import OllamaClient
from local_rag.metadata_db import MetadataDB, file_sha256
from local_rag.parsers import parse_file_safe
from local_rag.store import VectorStore
from local_rag.walker import iter_files

log = structlog.get_logger()


def _chunk_id(source_path: str, chunk_index: int) -> str:
    return f"{source_path}::{chunk_index}"


def run_index(*, force: bool = False) -> dict[str, int]:
    paths_cfg = load_paths_config()
    rag_cfg = load_rag_config()
    ollama = OllamaClient(rag_cfg)
    if not ollama.health():
        raise RuntimeError(
            f"Ollama not reachable at {rag_cfg.ollama_base_url}. "
            "Install Ollama and run: ollama pull nomic-embed-text"
        )

    db = MetadataDB(SQLITE_PATH)
    store = VectorStore(CHROMA_DIR)
    run_id = db.start_run()

    files = iter_files(paths_cfg)
    indexed = 0
    skipped = 0
    chunks_added = 0

    try:
        for path in tqdm(files, desc="Indexing files"):
            path_str = str(path)
            stat = path.stat()
            mtime = stat.st_mtime
            size = stat.st_size

            try:
                digest = file_sha256(path)
            except OSError as exc:
                log.warning("hash_failed", path=path_str, error=str(exc))
                continue

            prev = db.get_file(path_str)
            if (
                not force
                and prev
                and prev["sha256"] == digest
                and prev["status"] == "ok"
            ):
                skipped += 1
                continue

            parsed = parse_file_safe(path, excel_max_rows=rag_cfg.excel_max_rows_per_chunk)
            if parsed is None or not parsed.text.strip():
                db.upsert_file(
                    path_str,
                    digest,
                    mtime,
                    size,
                    0,
                    status="error",
                    error="parse_failed_or_empty",
                )
                store.delete_by_source(path_str)
                skipped += 1
                continue

            chunks = chunk_text(
                parsed.text,
                chunk_size=rag_cfg.chunk_size,
                chunk_overlap=rag_cfg.chunk_overlap,
                base_metadata=parsed.metadata,
            )
            if not chunks:
                skipped += 1
                continue

            store.delete_by_source(path_str)

            ids: list[str] = []
            docs: list[str] = []
            metas: list[dict[str, str]] = []

            for ch in chunks:
                cid = _chunk_id(path_str, ch.chunk_index)
                ids.append(cid)
                docs.append(ch.text)
                metas.append(ch.metadata)

            embeddings = ollama.embed(docs)
            store.upsert_chunks(
                ids=ids,
                documents=docs,
                embeddings=embeddings,
                metadatas=metas,
            )

            db.upsert_file(path_str, digest, mtime, size, len(chunks))
            indexed += 1
            chunks_added += len(chunks)

        db.finish_run(
            run_id,
            files_seen=len(files),
            files_indexed=indexed,
            files_skipped=skipped,
            chunks_added=chunks_added,
        )
        stats = db.stats()
        stats["vectors"] = store.count()
        return {
            "files_seen": len(files),
            "files_indexed": indexed,
            "files_skipped": skipped,
            "chunks_added": chunks_added,
            **stats,
        }
    finally:
        ollama.close()
        db.close()
