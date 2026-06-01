#!/usr/bin/env bash
# Shared helpers for sit-log-analysis (Git Bash on Windows).

set -euo pipefail

TOOL_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CONFIG_DIR="${TOOL_ROOT}/config"
QUERIES_DIR="${TOOL_ROOT}/queries"
FIXTURES_DIR="${TOOL_ROOT}/fixtures/sample"
PYTHON_DIR="${TOOL_ROOT}/python"

resolve_python() {
  if [[ -n "${PYTHON_CMD:-}" ]] && command -v "${PYTHON_CMD}" >/dev/null 2>&1; then
    printf '%s' "${PYTHON_CMD}"
    return
  fi
  if command -v python3 >/dev/null 2>&1; then
    printf '%s' python3
  elif command -v python >/dev/null 2>&1; then
    printf '%s' python
  else
    log_error "Python not found. Set PYTHON_CMD in config/sit.env"
    exit 1
  fi
}

run_sit_log_python() {
  local py
  py="$(resolve_python)"
  "${py}" "${PYTHON_DIR}/run_cli.py" "$@"
}

load_config() {
  local env_file="${CONFIG_DIR}/sit.env"
  if [[ -f "${env_file}" ]]; then
    # shellcheck disable=SC1090
    source "${env_file}"
  elif [[ -f "${CONFIG_DIR}/sit.env.example" ]]; then
    log_warn "config/sit.env not found; using sit.env.example (copy sit.env for production)."
    # shellcheck disable=SC1090
    source "${CONFIG_DIR}/sit.env.example"
  else
    log_error "No config found under ${CONFIG_DIR}"
    exit 1
  fi
  LOCAL_ROOT="${LOCAL_ROOT:-/c/LogAnalysis/sit}"
  DEFAULT_SERVICE="${DEFAULT_SERVICE:-payments-api}"
  SIT_PORT="${SIT_PORT:-22}"
}

log_info() { printf '[INFO] %s\n' "$*"; }
log_warn() { printf '[WARN] %s\n' "$*" >&2; }
log_error() { printf '[ERROR] %s\n' "$*" >&2; }

require_cmd() {
  local cmd
  for cmd in "$@"; do
    if ! command -v "${cmd}" >/dev/null 2>&1; then
      log_error "Required command not found: ${cmd}"
      exit 1
    fi
  done
}

resolve_work_dir() {
  local work_dir="${1:-}"
  if [[ -n "${work_dir}" ]]; then
    printf '%s' "${work_dir}"
    return
  fi
  local date_part
  date_part="$(date +%F)"
  printf '%s/%s/%s' "${LOCAL_ROOT}" "${date_part}" "${DEFAULT_SERVICE}"
}

ensure_work_layout() {
  local work_dir="$1"
  mkdir -p "${work_dir}/raw" "${work_dir}/normalized" "${work_dir}/out" "${work_dir}/queries"
}

normalized_file() {
  printf '%s/normalized/events.jsonl' "$1"
}

query_out_dir() {
  printf '%s/out/%s' "$1" "$2"
}
