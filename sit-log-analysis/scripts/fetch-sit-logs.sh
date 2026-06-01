#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=lib/common.sh
source "${SCRIPT_DIR}/lib/common.sh"

usage() {
  cat <<'EOF'
Usage: fetch-sit-logs.sh [--work-dir DIR] [--from-fixtures]

  --from-fixtures   Copy fixtures/sample into WORK_DIR/raw (offline demo; no SSH)
  --work-dir DIR    Override work directory (default LOCAL_ROOT/date/service)
EOF
}

FROM_FIXTURES=0
WORK_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --from-fixtures) FROM_FIXTURES=1; shift ;;
    --work-dir) WORK_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) log_error "Unknown option: $1"; usage; exit 1 ;;
  esac
done

load_config
WORK_DIR="$(resolve_work_dir "${WORK_DIR}")"
ensure_work_layout "${WORK_DIR}"

if [[ "${FROM_FIXTURES}" -eq 1 ]]; then
  log_info "Copying fixtures to ${WORK_DIR}/raw"
  cp -f "${FIXTURES_DIR}"/* "${WORK_DIR}/raw/" 2>/dev/null || true
  if [[ ! -f "${WORK_DIR}/raw/export.manifest.json" ]]; then
    cat >"${WORK_DIR}/raw/export.manifest.json" <<EOF
{
  "source": "fixtures",
  "exportedAt": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "files": ["api-access.jsonl", "application.log"]
}
EOF
  fi
  log_info "Fixture fetch complete: ${WORK_DIR}/raw"
  exit 0
fi

require_cmd scp ssh

if [[ -z "${SIT_USER:-}" || -z "${SIT_JUMP:-}" ]]; then
  log_error "Set SIT_USER and SIT_JUMP in config/sit.env"
  exit 1
fi

REMOTE="${REMOTE_EXPORT:-/tmp/export/latest.tar.gz}"
ARCHIVE_NAME="$(basename "${REMOTE}")"
LOCAL_ARCHIVE="${WORK_DIR}/raw/${ARCHIVE_NAME}"

log_info "Downloading ${SIT_USER}@${SIT_JUMP}:${REMOTE} -> ${LOCAL_ARCHIVE}"
scp -P "${SIT_PORT}" "${SIT_USER}@${SIT_JUMP}:${REMOTE}" "${LOCAL_ARCHIVE}"

log_info "Extracting archive"
tar -xzf "${LOCAL_ARCHIVE}" -C "${WORK_DIR}/raw" 2>/dev/null || unzip -o "${LOCAL_ARCHIVE}" -d "${WORK_DIR}/raw" 2>/dev/null || {
  log_warn "Could not extract as tar/zip; leaving archive in raw/"
}

log_info "Fetch complete: ${WORK_DIR}/raw"
