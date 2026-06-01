#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../scripts/lib/common.sh
source "${SCRIPT_DIR}/../scripts/lib/common.sh"
: "${EVENTS:?}" "${OUT_DIR:?}"
: "${TRACE_ID:?TRACE_ID required}"
run_sit_log_python run-query requests-by-trace-id --events "${EVENTS}" --out-dir "${OUT_DIR}"
