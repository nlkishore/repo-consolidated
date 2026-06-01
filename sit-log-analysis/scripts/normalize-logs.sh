#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

usage() {
  echo "Usage: normalize-logs.sh [--work-dir DIR]"
}

WORK_DIR=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --work-dir) WORK_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) log_error "Unknown option: $1"; usage; exit 1 ;;
  esac
done

load_config
require_cmd awk
WORK_DIR="$(resolve_work_dir "${WORK_DIR}")"
ensure_work_layout "${WORK_DIR}"

RAW_DIR="${WORK_DIR}/raw"
OUT_FILE="$(normalized_file "${WORK_DIR}")"
TMP_FILE="${OUT_FILE}.tmp"

: >"${TMP_FILE}"

while IFS= read -r f; do
  log_info "JSONL pass-through: ${f}"
  run_sit_log_python append-valid-jsonl "${f}" "${TMP_FILE}" || log_warn "Skipped invalid lines in ${f}"
done < <(find "${RAW_DIR}" -maxdepth 3 -type f -name '*.jsonl' 2>/dev/null)

while IFS= read -r f; do
  log_info "Log4j2 pattern parse: ${f}"
  awk -f "${SCRIPT_DIR}/lib/parse-log4j-api.awk" "${f}" >>"${TMP_FILE}"
done < <(find "${RAW_DIR}" -maxdepth 3 -type f -name '*.log' 2>/dev/null)

if [[ ! -s "${TMP_FILE}" ]]; then
  log_warn "No events normalized; check raw/ contents"
fi

mv -f "${TMP_FILE}" "${OUT_FILE}"
LINE_COUNT="$(wc -l <"${OUT_FILE}" | tr -d ' ')"
log_info "Wrote ${LINE_COUNT} events to ${OUT_FILE}"
