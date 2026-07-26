#!/usr/bin/env bash
set -euo pipefail
PROJ="${1:-.}"
OUT="${PROJ}/analyzer-libs"
echo "Copying Maven dependencies to ${OUT}"
mvn -f "${PROJ}/pom.xml" -q dependency:copy-dependencies -DoutputDirectory="${OUT}"
echo "Done. Use: --lib-dir ${OUT}"
