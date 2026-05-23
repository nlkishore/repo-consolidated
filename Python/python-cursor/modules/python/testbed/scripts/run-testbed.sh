#!/usr/bin/env bash
# Run testbed CLI (Git Bash).
# Usage:
#   bash scripts/run-testbed.sh
#   bash scripts/run-testbed.sh seed payments
#   bash scripts/run-testbed.sh validate

set -euo pipefail

COMMAND="${1:-run-all}"
SCENARIO="${2:-}"
CONFIG="config/settings.local.yaml"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/env-testbed.sh"
cd "${TESTBED_ROOT}"

case "${COMMAND}" in
  seed)
    if [[ -z "${SCENARIO}" ]]; then
      python -m testbed seed --all --config "${CONFIG}"
    else
      python -m testbed seed --scenario "${SCENARIO}" --config "${CONFIG}"
    fi
    ;;
  validate)
    python -m testbed validate --config "${CONFIG}"
    ;;
  report)
    python -m testbed report --format html --config "${CONFIG}"
    ;;
  reset)
    python -m testbed reset --yes --config "${CONFIG}"
    ;;
  run-all)
    python -m testbed run-all --config "${CONFIG}"
    ;;
  *)
    echo "Unknown command: ${COMMAND}"
    echo "Use: run-all | seed | validate | report | reset"
    exit 1
    ;;
esac

if [[ -f "${TESTBED_ROOT}/testbed-reports/testbed-summary.html" ]]; then
  echo "Report: ${TESTBED_ROOT}/testbed-reports/testbed-summary.html"
fi
