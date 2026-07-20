@echo off
setlocal EnableExtensions

REM Portable launcher for My-First-Local-Server (Windows)
cd /d "%~dp0"

where python >nul 2>&1
if errorlevel 1 (
  echo ERROR: Python was not found on PATH.
  echo Install Python 3.10+ from https://www.python.org/downloads/
  echo and ensure "Add Python to PATH" is checked.
  exit /b 1
)

if not exist ".venv\Scripts\python.exe" (
  echo Creating virtual environment...
  python -m venv .venv
  if errorlevel 1 (
    echo ERROR: Failed to create virtual environment.
    exit /b 1
  )
)

echo Installing / updating dependencies from requirements.txt...
".venv\Scripts\python.exe" -m pip install --upgrade pip >nul
".venv\Scripts\python.exe" -m pip install -r requirements.txt
if errorlevel 1 (
  echo ERROR: Failed to install dependencies.
  exit /b 1
)

echo Starting MCP server (stdio)...
".venv\Scripts\python.exe" server.py
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
