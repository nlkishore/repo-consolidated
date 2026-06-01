#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

usage() {
  cat <<'EOF'
Usage: trigger-remote-export.sh --since ISO8601 --until ISO8601 [--service NAME]

Runs the ops-approved collector on SIT via SSH (sudo as configured on server).
Requires REMOTE_COLLECTOR_SCRIPT in config/sit.env.

Example:
  ./trigger-remote-export.sh --since 2026-05-30T00:00:00 --until 2026-05-30T23:59:59 --service payments-api
EOF
}

SINCE=""
UNTIL=""
SERVICE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --since) SINCE="$2"; shift 2 ;;
    --until) UNTIL="$2"; shift 2 ;;
    --service) SERVICE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) log_error "Unknown option: $1"; usage; exit 1 ;;
  esac
done

if [[ -z "${SINCE}" || -z "${UNTIL}" ]]; then
  usage
  exit 1
fi

load_config
require_cmd ssh

SERVICE="${SERVICE:-${DEFAULT_SERVICE}}"
COLLECTOR="${REMOTE_COLLECTOR_SCRIPT:-/opt/log-export/collect-api-logs.sh}"
COLLECTOR_USER="${REMOTE_COLLECTOR_USER:-logcollector}"
REMOTE_OUT="${REMOTE_EXPORT:-/tmp/export/latest.tar.gz}"

REMOTE_CMD="sudo -u ${COLLECTOR_USER} ${COLLECTOR} --since '${SINCE}' --until '${UNTIL}' --service '${SERVICE}' --out '${REMOTE_OUT}'"

log_info "Triggering remote export on ${SIT_JUMP} (service=${SERVICE})"
ssh -p "${SIT_PORT}" "${SIT_USER}@${SIT_JUMP}" "${REMOTE_CMD}"
log_info "Remote export finished. Run fetch-sit-logs.sh to download."
