#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

usage() {
  cat <<'EOF'
SIT log analysis toolkit

Usage:
  sit-log-analysis.sh fetch [--from-fixtures] [--work-dir DIR]
  sit-log-analysis.sh export --since T --until T [--service NAME]
  sit-log-analysis.sh normalize [--work-dir DIR]
  sit-log-analysis.sh query <query-id> [--work-dir DIR]
  sit-log-analysis.sh run-all [--work-dir DIR]
  sit-log-analysis.sh pipeline [--from-fixtures] [--work-dir DIR]

pipeline = fetch + normalize + run-all
EOF
}

CMD="${1:-}"
shift || true

case "${CMD}" in
  fetch)
    exec "${SCRIPT_DIR}/fetch-sit-logs.sh" "$@"
    ;;
  export)
    exec "${SCRIPT_DIR}/trigger-remote-export.sh" "$@"
    ;;
  normalize)
    exec "${SCRIPT_DIR}/normalize-logs.sh" "$@"
    ;;
  query)
    exec "${SCRIPT_DIR}/run-query.sh" "$@"
    ;;
  run-all)
    exec "${SCRIPT_DIR}/run-all-queries.sh" "$@"
    ;;
  pipeline)
    FROM_FIX=()
    WORK=()
    while [[ $# -gt 0 ]]; do
      case "$1" in
        --from-fixtures) FROM_FIX=(--from-fixtures); shift ;;
        --work-dir) WORK=(--work-dir "$2"); shift 2 ;;
        *) echo "Unknown: $1"; usage; exit 1 ;;
      esac
    done
    "${SCRIPT_DIR}/fetch-sit-logs.sh" "${FROM_FIX[@]}" "${WORK[@]}"
    "${SCRIPT_DIR}/normalize-logs.sh" "${WORK[@]}"
    "${SCRIPT_DIR}/run-all-queries.sh" "${WORK[@]}"
    ;;
  ""|-h|--help)
    usage
    ;;
  *)
    echo "Unknown command: ${CMD}"
    usage
    exit 1
    ;;
esac
