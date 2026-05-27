from __future__ import annotations

import hashlib
import sqlite3
from datetime import datetime, timezone
from pathlib import Path


def _utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def file_sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for block in iter(lambda: f.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


class MetadataDB:
    def __init__(self, db_path: Path) -> None:
        db_path.parent.mkdir(parents=True, exist_ok=True)
        self._conn = sqlite3.connect(str(db_path))
        self._conn.row_factory = sqlite3.Row
        self._init_schema()

    def _init_schema(self) -> None:
        self._conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS files (
                path TEXT PRIMARY KEY,
                sha256 TEXT NOT NULL,
                mtime REAL NOT NULL,
                size_bytes INTEGER NOT NULL,
                chunk_count INTEGER NOT NULL DEFAULT 0,
                indexed_at TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'ok',
                error TEXT
            );
            CREATE TABLE IF NOT EXISTS index_runs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                started_at TEXT NOT NULL,
                finished_at TEXT,
                files_seen INTEGER,
                files_indexed INTEGER,
                files_skipped INTEGER,
                chunks_added INTEGER
            );
            """
        )
        self._conn.commit()

    def close(self) -> None:
        self._conn.close()

    def get_file(self, path: str) -> sqlite3.Row | None:
        cur = self._conn.execute("SELECT * FROM files WHERE path = ?", (path,))
        return cur.fetchone()

    def upsert_file(
        self,
        path: str,
        sha256: str,
        mtime: float,
        size_bytes: int,
        chunk_count: int,
        *,
        status: str = "ok",
        error: str | None = None,
    ) -> None:
        self._conn.execute(
            """
            INSERT INTO files (path, sha256, mtime, size_bytes, chunk_count, indexed_at, status, error)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(path) DO UPDATE SET
                sha256=excluded.sha256,
                mtime=excluded.mtime,
                size_bytes=excluded.size_bytes,
                chunk_count=excluded.chunk_count,
                indexed_at=excluded.indexed_at,
                status=excluded.status,
                error=excluded.error
            """,
            (path, sha256, mtime, size_bytes, chunk_count, _utc_now(), status, error),
        )
        self._conn.commit()

    def delete_file(self, path: str) -> None:
        self._conn.execute("DELETE FROM files WHERE path = ?", (path,))
        self._conn.commit()

    def start_run(self) -> int:
        cur = self._conn.execute(
            "INSERT INTO index_runs (started_at) VALUES (?)",
            (_utc_now(),),
        )
        self._conn.commit()
        return int(cur.lastrowid)

    def finish_run(
        self,
        run_id: int,
        *,
        files_seen: int,
        files_indexed: int,
        files_skipped: int,
        chunks_added: int,
    ) -> None:
        self._conn.execute(
            """
            UPDATE index_runs SET
                finished_at = ?,
                files_seen = ?,
                files_indexed = ?,
                files_skipped = ?,
                chunks_added = ?
            WHERE id = ?
            """,
            (_utc_now(), files_seen, files_indexed, files_skipped, chunks_added, run_id),
        )
        self._conn.commit()

    def stats(self) -> dict[str, int]:
        cur = self._conn.execute(
            "SELECT COUNT(*) AS n, COALESCE(SUM(chunk_count),0) AS chunks FROM files WHERE status='ok'"
        )
        row = cur.fetchone()
        return {"files": int(row["n"]), "chunks": int(row["chunks"])}
