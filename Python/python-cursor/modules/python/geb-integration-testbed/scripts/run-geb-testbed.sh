#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python -m geb_testbed run-all --config config/settings.example.yaml
