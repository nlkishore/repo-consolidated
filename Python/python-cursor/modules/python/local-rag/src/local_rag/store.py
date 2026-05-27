from __future__ import annotations

from pathlib import Path

import chromadb
from chromadb.config import Settings

COLLECTION_NAME = "local_documents"


class VectorStore:
    def __init__(self, persist_dir: Path) -> None:
        persist_dir.mkdir(parents=True, exist_ok=True)
        self._client = chromadb.PersistentClient(
            path=str(persist_dir),
            settings=Settings(anonymized_telemetry=False),
        )
        self._collection = self._client.get_or_create_collection(
            name=COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"},
        )

    def delete_by_source(self, source_path: str) -> None:
        existing = self._collection.get(where={"source_path": source_path})
        ids = existing.get("ids") or []
        if ids:
            self._collection.delete(ids=ids)

    def upsert_chunks(
        self,
        *,
        ids: list[str],
        documents: list[str],
        embeddings: list[list[float]],
        metadatas: list[dict[str, str]],
    ) -> None:
        self._collection.upsert(
            ids=ids,
            documents=documents,
            embeddings=embeddings,
            metadatas=metadatas,
        )

    def query(self, embedding: list[float], top_k: int) -> dict:
        return self._collection.query(
            query_embeddings=[embedding],
            n_results=top_k,
            include=["documents", "metadatas", "distances"],
        )

    def count(self) -> int:
        return self._collection.count()
