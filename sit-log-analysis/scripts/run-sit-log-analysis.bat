@echo off
REM Run from Git Bash: bash scripts/sit-log-analysis.sh pipeline --from-fixtures
setlocal
cd /d "%~dp0.."
where bash >nul 2>&1 || (
  echo Install Git for Windows and ensure bash is on PATH.
  exit /b 1
)
bash scripts/sit-log-analysis.sh %*
