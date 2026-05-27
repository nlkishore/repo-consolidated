@echo off
cd /d "%~dp0.."
if not exist ".venv\Scripts\python.exe" (
  echo Create venv first: python -m venv .venv ^&^& .venv\Scripts\pip install -e .
  exit /b 1
)
call .venv\Scripts\activate.bat
local-rag index %*
