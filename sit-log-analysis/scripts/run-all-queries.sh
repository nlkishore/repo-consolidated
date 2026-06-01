#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

WORK_DIR=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --work-dir) WORK_DIR="$2"; shift 2 ;;
    -h|--help) echo "Usage: run-all-queries.sh [--work-dir DIR]"; exit 0 ;;
    *) log_error "Unknown option: $1"; exit 1 ;;
  esac
done

load_config
CATALOG="${CONFIG_DIR}/query-catalog.yaml"

if [[ ! -f "${CATALOG}" ]]; then
  log_error "Missing ${CATALOG}"
  exit 1
fi

mapfile -t QUERY_IDS < <(run_sit_log_python list-queries --catalog "${CATALOG}")

for qid in "${QUERY_IDS[@]}"; do
  qid="${qid//$'\r'/}"
  if [[ "${qid}" == "requests-by-trace-id" && -z "${TRACE_ID:-}" ]]; then
    log_warn "Skipping ${qid} (set TRACE_ID to run)"
    continue
  fi
  "${SCRIPT_DIR}/run-query.sh" "${qid}" ${WORK_DIR:+--work-dir "${WORK_DIR}"}
done

log_info "All applicable queries finished"
