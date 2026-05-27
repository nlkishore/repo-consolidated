@echo off
cd /d "%~dp0.."
if not exist ".venv\Scripts\python.exe" (
  echo Create venv first: python -m venv .venv ^&^& .venv\Scripts\pip install -e .
  exit /b 1
)
call .venv\Scripts\activate.bat
if "%~1"=="" (
  echo Usage: query.bat "Your question here"
  exit /b 1
)
local-rag query %*
