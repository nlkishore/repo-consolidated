#!/usr/bin/env bash
# Portable launcher for My-First-Local-Server (Linux / macOS)
set -euo pipefail

cd "$(dirname "$0")"

if ! command -v python3 >/dev/null 2>&1 && ! command -v python >/dev/null 2>&1; then
  echo "ERROR: Python was not found on PATH."
  echo "Install Python 3.10+ and try again."
  exit 1
fi

if command -v python3 >/dev/null 2>&1; then
  PYTHON_CMD="python3"
else
  PYTHON_CMD="python"
fi

if [ ! -x ".venv/bin/python" ]; then
  echo "Creating virtual environment..."
  "$PYTHON_CMD" -m venv .venv
fi

echo "Installing / updating dependencies from requirements.txt..."
.venv/bin/python -m pip install --upgrade pip >/dev/null
.venv/bin/python -m pip install -r requirements.txt

echo "Starting MCP server (stdio)..."
exec .venv/bin/python server.py
