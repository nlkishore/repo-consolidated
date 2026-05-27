@echo off
cd /d "%~dp0.."
call .venv\Scripts\activate.bat 2>nul
local-rag status
