@echo off
REM One-time MySQL setup: schema, testbed user, GTP tables.
REM Usage:  scripts\setup-mysql.bat
REM
REM Set admin credentials first (CMD):
REM   set MYSQL_ADMIN_USER=kishore
REM   set MYSQL_ADMIN_PASSWORD=your_admin_password

call "%~dp0env-testbed.bat"
cd /d "%TESTBED_ROOT%"

if not exist "%MYSQL_EXE%" (
    echo ERROR: mysql.exe not found at %MYSQL_EXE%
    exit /b 1
)

if "%MYSQL_ADMIN_USER%"=="" set "MYSQL_ADMIN_USER=kishore"
if "%MYSQL_ADMIN_PASSWORD%"=="" (
    echo ERROR: Set MYSQL_ADMIN_PASSWORD before running setup.
    echo   set MYSQL_ADMIN_PASSWORD=your_password
    exit /b 1
)

sc query MySQL81 | find "RUNNING" >nul
if errorlevel 1 (
    echo Starting MySQL81 service...
    net start MySQL81
)

echo Creating testbed database and user...
"%MYSQL_EXE%" -u %MYSQL_ADMIN_USER% -p%MYSQL_ADMIN_PASSWORD% -e "source %TESTBED_ROOT%\sql\00-create-testbed-db-user.sql"
if errorlevel 1 exit /b 1

echo Creating GTP tables...
"%MYSQL_EXE%" -u testbed testbed -e "source %TESTBED_ROOT%\sql\01-create-testbed-schema.sql"
if errorlevel 1 exit /b 1

echo Verifying tables...
"%MYSQL_EXE%" -u testbed testbed -e "SHOW TABLES LIKE 'GTP_%%';"

echo.
echo MySQL testbed setup complete.
echo Next: pip install -e ".[dev]"
echo       python -m testbed run-all --config config\settings.local.yaml
