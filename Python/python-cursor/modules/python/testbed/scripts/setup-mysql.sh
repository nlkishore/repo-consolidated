#!/usr/bin/env bash
# One-time MySQL setup (Git Bash).
# Usage:  bash scripts/setup-mysql.sh
#
#   export MYSQL_ADMIN_USER=kishore
#   export MYSQL_ADMIN_PASSWORD=your_admin_password
#   bash scripts/setup-mysql.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/env-testbed.sh"
cd "${TESTBED_ROOT}"

MYSQL_ADMIN_USER="${MYSQL_ADMIN_USER:-kishore}"
if [[ -z "${MYSQL_ADMIN_PASSWORD:-}" ]]; then
  echo "ERROR: Set MYSQL_ADMIN_PASSWORD before running setup."
  echo "  export MYSQL_ADMIN_PASSWORD=your_password"
  exit 1
fi

echo "Creating testbed database and user..."
"${MYSQL_EXE}" -u "${MYSQL_ADMIN_USER}" -p"${MYSQL_ADMIN_PASSWORD}" -e "source ${TESTBED_ROOT}/sql/00-create-testbed-db-user.sql"

echo "Creating GTP tables..."
"${MYSQL_EXE}" -u testbed testbed -e "source ${TESTBED_ROOT}/sql/01-create-testbed-schema.sql"

echo "Verifying tables..."
"${MYSQL_EXE}" -u testbed testbed -e "SHOW TABLES LIKE 'GTP_%';"

echo ""
echo "MySQL testbed setup complete."
