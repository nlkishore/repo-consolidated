@echo off
REM Run testbed CLI with env loaded.
REM Usage:
REM   scripts\run-testbed.bat
REM   scripts\run-testbed.bat seed payments
REM   scripts\run-testbed.bat validate
REM   scripts\run-testbed.bat report
REM   scripts\run-testbed.bat reset

set "COMMAND=run-all"
set "SCENARIO="
set "EXTRA="

if not "%~1"=="" set "COMMAND=%~1"
if not "%~2"=="" (
    if /i "%~2%"=="--no-reset" (
        set "EXTRA=--no-reset"
    ) else (
        set "SCENARIO=%~2"
    )
)
if /i "%~3%"=="--no-reset" set "EXTRA=--no-reset"

call "%~dp0env-testbed.bat"
cd /d "%TESTBED_ROOT%"

sc query MySQL81 | find "RUNNING" >nul
if errorlevel 1 (
    echo Starting MySQL81 service...
    net start MySQL81
)

set "CONFIG=config\settings.local.yaml"

if /i "%COMMAND%"=="seed" (
    if "%SCENARIO%"=="" (
        python -m testbed seed --all --config %CONFIG%
    ) else (
        python -m testbed seed --scenario %SCENARIO% --config %CONFIG%
    )
    goto :done
)

if /i "%COMMAND%"=="validate" (
    python -m testbed validate --config %CONFIG%
    goto :done
)

if /i "%COMMAND%"=="report" (
    python -m testbed report --format html --config %CONFIG%
    goto :report
)

if /i "%COMMAND%"=="reset" (
    python -m testbed reset --yes --config %CONFIG%
    goto :done
)

python -m testbed run-all --config %CONFIG% %EXTRA%

:report
if exist "%TESTBED_ROOT%\testbed-reports\testbed-summary.html" (
    echo Report: %TESTBED_ROOT%\testbed-reports\testbed-summary.html
)

:done
exit /b %ERRORLEVEL%
