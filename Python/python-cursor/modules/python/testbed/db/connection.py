"""MySQL/MariaDB connection helper."""

from __future__ import annotations

from contextlib import contextmanager
from typing import Generator

import mysql.connector
from mysql.connector import MySQLConnection

from testbed.config.schema import Settings


def get_connection(settings: Settings) -> MySQLConnection:
    db = settings.database
    return mysql.connector.connect(
        host=db.host,
        port=db.port,
        database=db.name,
        user=db.username,
        password=db.password,
        connection_timeout=db.connect_timeout,
        autocommit=False,
    )


@contextmanager
def db_cursor(settings: Settings) -> Generator:
    conn = get_connection(settings)
    try:
        with conn.cursor(dictionary=True) as cur:
            yield cur, conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()
