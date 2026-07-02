@echo off
setlocal
cd /d "%~dp0"
if not exist ".venv\Scripts\python.exe" (
  echo Create venv first: python -m venv .venv
  echo Then: .venv\Scripts\pip install -r requirements.txt
  exit /b 1
)
call .venv\Scripts\activate.bat
set "SUBCMD=run"
if /I "%~1"=="status" (set "SUBCMD=status" & shift)
if /I "%~1"=="refresh-only" (set "SUBCMD=refresh-only" & shift)
if /I "%~1"=="run" (set "SUBCMD=run" & shift)
python -m completely_sold_alert %SUBCMD% %*