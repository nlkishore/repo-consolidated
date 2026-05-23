@echo off
REM Validate GEB contract fixtures and generate contract matrix report.
cd /d "%~dp0.."
python -m geb_testbed run-all --config config\settings.example.yaml
exit /b %ERRORLEVEL%
