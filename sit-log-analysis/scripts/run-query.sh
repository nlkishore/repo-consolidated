#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

usage() {
  echo "Usage: run-query.sh <query-id> [--work-dir DIR]"
  echo "Env: TRACE_ID (for requests-by-trace-id), LATENCY_MS_MIN (for slow-requests, default 1000)"
}

QUERY_ID="${1:-}"
QUERY_ID="${QUERY_ID//$'\r'/}"
shift || true
WORK_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --work-dir) WORK_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) log_error "Unknown option: $1"; usage; exit 1 ;;
  esac
done

if [[ -z "${QUERY_ID}" ]]; then
  usage
  exit 1
fi

load_config
WORK_DIR="$(resolve_work_dir "${WORK_DIR}")"
EVENTS="$(normalized_file "${WORK_DIR}")"

if [[ ! -f "${EVENTS}" ]]; then
  log_error "Normalized file missing: ${EVENTS}. Run normalize-logs.sh first."
  exit 1
fi

OUT_DIR="$(query_out_dir "${WORK_DIR}" "${QUERY_ID}")"
mkdir -p "${OUT_DIR}"

export WORK_DIR EVENTS OUT_DIR QUERY_ID
log_info "Running query ${QUERY_ID} (Python) -> ${OUT_DIR}"
run_sit_log_python run-query "${QUERY_ID}" --events "${EVENTS}" --out-dir "${OUT_DIR}"
