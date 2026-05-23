#!/usr/bin/env bash
# Load testbed environment (Git Bash / WSL-style shell on Windows).
# Usage:  source scripts/env-testbed.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export TESTBED_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

export DB_HOST="${DB_HOST:-localhost}"
export DB_NAME="${DB_NAME:-testbed}"
export DB_USER="${DB_USER:-testbed}"
export DB_PASSWORD="${DB_PASSWORD:-}"

export MYSQL_HOME="${MYSQL_HOME:-/c/Program Files/MySQL/MySQL Server 8.1}"
export MYSQL_BIN="${MYSQL_HOME}/bin"
export MYSQL_EXE="${MYSQL_BIN}/mysql.exe"
export PATH="${MYSQL_BIN}:${PATH}"

echo "Testbed env loaded."
echo "  DB: ${DB_USER}@${DB_HOST}/${DB_NAME} (no password)"
echo "  Root: ${TESTBED_ROOT}"
